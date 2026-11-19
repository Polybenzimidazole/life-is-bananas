package org.example.ai;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:banana-assistant-disabled;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=false",
        "app.db-init=false",
        "spring.ai.model.chat=none",
        "spring.ai.openai.api-key="
})
@AutoConfigureMockMvc
class AssistantDisabledIntegrationTest {

    @Autowired MockMvc mvc;

    @Test
    void startsWithoutAnOpenAiKeyAndExplainsThatAiIsDisabled() throws Exception {
        mvc.perform(post("/ai/ask").with(jwt()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"question\":\"Wat zit er in dit recept?\",\"recipeId\":1}"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.detail").value(
                        "De AI-assistent is nog niet ingesteld. Configureer OpenAI op de server."));
    }
}
