package org.example.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.example.dao.RawMaterialRepository;
import org.example.dao.RecipeRepository;
import org.example.dao.StockRepository;
import org.example.domain.RawMaterial;
import org.example.domain.Recipeingredients;
import org.example.domain.Stock;
import org.example.domain.recipe;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;

import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:banana-assistant;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.open-in-view=false",
        "spring.jpa.show-sql=false",
        "app.db-init=false",
        "spring.ai.model.chat=openai",
        "spring.ai.openai.api-key=test-key-not-a-real-secret",
        "spring.ai.openai.base-url=${openai.base.url}",
        "spring.ai.openai.chat.base-url=${openai.base.url}",
        "spring.ai.openai.chat.options.model=gpt-4o-mini"
})
@AutoConfigureMockMvc
@EnableWireMock(@ConfigureWireMock(baseUrlProperties = "openai.base.url"))
class BananaAssistantIntegrationTest {

    private static final String COMPLETIONS = "/v1/chat/completions";
    private static final String QUESTION = "Is er voldoende voorraad voor 10 eenheden?";

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired RecipeRepository recipes;
    @Autowired StockRepository stocks;
    @Autowired RawMaterialRepository materials;
    @InjectWireMock WireMockServer openAi;

    private int recipeId;
    private int bananaStockId;
    private int eggStockId;
    private String expectedAnswer;

    @BeforeEach
    void setUp() throws Exception {
        openAi.resetAll();
        recipes.deleteAll();
        stocks.deleteAll();
        materials.deleteAll();

        var bananas = stock("Bananas", "KG", 2.4);
        var eggs = stock("Eggs", "PCS", 20);
        bananaStockId = bananas.getId();
        eggStockId = eggs.getId();
        var bread = new recipe();
        bread.setTitle("Banana Bread");
        ingredient(bread, bananas.getRawMaterial(), 0.3);
        ingredient(bread, eggs.getRawMaterial(), 2);
        recipeId = recipes.save(bread).getId();

        var fixture = new ClassPathResource("canned/raw-openai-response.json")
                .getContentAsString(StandardCharsets.UTF_8);
        expectedAnswer = objectMapper.readTree(fixture).at("/choices/0/message/content").asText();
        openAi.stubFor(WireMock.post(urlEqualTo(COMPLETIONS)).willReturn(okJson(fixture)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"ROLE_USER", "ROLE_ADMIN"})
    void asksOpenAiWithTheQuestionAndRealRecipeContext(String role) throws Exception {
        mvc.perform(post("/ai/ask").with(jwt().authorities(new SimpleGrantedAuthority(role)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(questionJson()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answer").value(expectedAnswer))
                .andExpect(jsonPath("$.sources[0]").value("Recept #" + recipeId + ": Banana Bread"));

        openAi.verify(1, postRequestedFor(urlEqualTo(COMPLETIONS))
                .withHeader("Authorization", equalTo("Bearer test-key-not-a-real-secret"))
                .withRequestBody(matchingJsonPath("$.model", equalTo("gpt-4o-mini")))
                .withRequestBody(matchingJsonPath("$.messages[0].role", equalTo("system")))
                .withRequestBody(matchingJsonPath("$.messages[0].content", containing("Life is Bananas")))
                .withRequestBody(matchingJsonPath("$.messages[1].content", containing("Banana Bread")))
                .withRequestBody(matchingJsonPath("$.messages[1].content", containing("\"quantityPerUnit\":0.3")))
                .withRequestBody(matchingJsonPath("$.messages[1].content", containing("\"unit\":\"KG\"")))
                .withRequestBody(matchingJsonPath("$.messages[1].content", containing("\"availableStock\":2.4")))
                .withRequestBody(matchingJsonPath("$.messages[2].content", equalTo(QUESTION))));
        assertThat(stocks.findById(bananaStockId).orElseThrow().getQuantity()).isEqualTo(2.4);
        assertThat(stocks.findById(eggStockId).orElseThrow().getQuantity()).isEqualTo(20);
    }

    @Test
    void retrievesFreshStockForEveryQuestion() throws Exception {
        askSuccessfully();
        var stock = stocks.findById(bananaStockId).orElseThrow();
        stock.setQuantity(7.5);
        stocks.save(stock);

        askSuccessfully();

        openAi.verify(2, postRequestedFor(urlEqualTo(COMPLETIONS)));
        openAi.verify(1, postRequestedFor(urlEqualTo(COMPLETIONS))
                .withRequestBody(matchingJsonPath("$.messages[1].content", containing("\"availableStock\":7.5"))));
    }

    @Test
    void preservesUnknownStockInsteadOfInventingZero() throws Exception {
        stocks.deleteById(eggStockId);

        askSuccessfully();

        openAi.verify(1, postRequestedFor(urlEqualTo(COMPLETIONS))
                .withRequestBody(matchingJsonPath("$.messages[1].content", containing("\"availableStock\":null"))));
    }

    @Test
    void rejectsAnonymousRequestsBeforeCallingOpenAi() throws Exception {
        mvc.perform(post("/ai/ask").contentType(MediaType.APPLICATION_JSON).content(questionJson()))
                .andExpect(status().isUnauthorized());
        openAi.verify(0, postRequestedFor(urlEqualTo(COMPLETIONS)));
    }

    @ParameterizedTest
    @MethodSource("invalidQuestions")
    void rejectsInvalidQuestionsBeforeCallingOpenAi(String request) throws Exception {
        mvc.perform(post("/ai/ask").with(jwt()).contentType(MediaType.APPLICATION_JSON).content(request))
                .andExpect(status().isBadRequest());
        openAi.verify(0, postRequestedFor(urlEqualTo(COMPLETIONS)));
    }

    static Stream<String> invalidQuestions() {
        return Stream.of(
                "{\"recipeId\":1}",
                "{\"question\":null,\"recipeId\":1}",
                "{\"question\":\"   \",\"recipeId\":1}",
                "{\"question\":\"" + "x".repeat(2001) + "\",\"recipeId\":1}",
                "{\"question\":\"Voorraad?\"}",
                "{\"question\":\"Voorraad?\",\"recipeId\":-1}"
        );
    }

    @Test
    void rejectsUnknownRecipesBeforeCallingOpenAi() throws Exception {
        mvc.perform(post("/ai/ask").with(jwt()).contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new Question(QUESTION, Integer.MAX_VALUE))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Recept niet gevonden."));
        openAi.verify(0, postRequestedFor(urlEqualTo(COMPLETIONS)));
    }

    @ParameterizedTest
    @ValueSource(ints = {401, 429, 500})
    void handlesOpenAiErrorsWithoutExposingProviderDetails(int providerStatus) throws Exception {
        openAi.stubFor(WireMock.post(urlEqualTo(COMPLETIONS)).willReturn(aResponse()
                .withStatus(providerStatus).withHeader("Content-Type", "application/json")
                .withBody("{\"error\":{\"message\":\"provider-secret\",\"type\":\"test_error\"}}")));

        mvc.perform(post("/ai/ask").with(jwt()).contentType(MediaType.APPLICATION_JSON).content(questionJson()))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.detail").value("De AI-assistent is tijdelijk niet beschikbaar. Probeer het later opnieuw."))
                .andExpect(content().string(not(containsString("provider-secret"))));
        openAi.verify(1, postRequestedFor(urlEqualTo(COMPLETIONS)));
    }

    @Test
    void handlesAnEmptyModelAnswer() throws Exception {
        openAi.stubFor(WireMock.post(urlEqualTo(COMPLETIONS)).willReturn(okJson("""
                {"id":"empty","object":"chat.completion","choices":[
                  {"index":0,"message":{"role":"assistant","content":" "},"finish_reason":"stop"}
                ]}
                """)));

        mvc.perform(post("/ai/ask").with(jwt()).contentType(MediaType.APPLICATION_JSON).content(questionJson()))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.detail").value("De AI-assistent gaf geen antwoord. Probeer het opnieuw."));
    }

    private void askSuccessfully() throws Exception {
        mvc.perform(post("/ai/ask").with(jwt()).contentType(MediaType.APPLICATION_JSON).content(questionJson()))
                .andExpect(status().isOk());
    }

    private String questionJson() throws Exception {
        return objectMapper.writeValueAsString(new Question(QUESTION, recipeId));
    }

    private Stock stock(String name, String unit, double quantity) {
        var material = new RawMaterial();
        material.setName(name);
        material.setUnit(unit);
        var stock = new Stock();
        stock.setRawMaterial(material);
        stock.setQuantity(quantity);
        return stocks.save(stock);
    }

    private void ingredient(recipe recipe, RawMaterial material, double quantity) {
        var ingredient = new Recipeingredients();
        ingredient.setRecipe(recipe);
        ingredient.setRawMaterial(material);
        ingredient.setQuantity(quantity);
        recipe.getRecipeingredients().add(ingredient);
    }
}
