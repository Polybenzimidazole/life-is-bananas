package be.lifeisbananas.controller.api;

import be.lifeisbananas.dao.*;
import be.lifeisbananas.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Echte services en repositories; elk HTTP-verzoek heeft zijn eigen transactie. */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:recipe-api;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa", "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop", "spring.sql.init.mode=never"
})
@AutoConfigureMockMvc
class RecipeApiIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired FormulaRepository formulas;
    @Autowired RawMaterialRepository materials;
    @Autowired BioEngineerRepository engineers;
    Long recipeId;
    Long materialId;

    @BeforeEach
    void seed() {
        formulas.deleteAll();
        materials.deleteAll();
        engineers.deleteAll();
        var engineer = engineers.save(new BioEngineer("Sarah", "sarah@example.com", null, "Fruit", "banaan123"));
        var material = materials.save(new FruitRawMaterial("Banana", true, new BigDecimal("2.50"), "kg"));
        materialId = material.getId();
        recipeId = formulas.save(new Formula("Banana Chips", "1.0", "Snijd de bananen.", engineer)).getId();
    }

    MockHttpSession login() throws Exception {
        var result = mvc.perform(post("/api/session").contentType("application/json")
                        .content("{\"email\":\"sarah@example.com\",\"password\":\"banaan123\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.name").value("Sarah"))
                .andExpect(jsonPath("$.password").doesNotExist()).andReturn();
        return (MockHttpSession) result.getRequest().getSession(false);
    }

    @Test
    void authenticationAndLogout() throws Exception {
        mvc.perform(get("/api/recipes")).andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith("application/json"));
        mvc.perform(post("/api/session").contentType("application/json")
                .content("{\"email\":\"sarah@example.com\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.message").isNotEmpty());
        var session = login();
        mvc.perform(get("/api/session").session(session)).andExpect(status().isOk());
        mvc.perform(delete("/api/session").session(session)).andExpect(status().isNoContent());
        mvc.perform(get("/api/session")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/recipes/{id}/ingredients", recipeId).contentType("application/json")
                .content("{}" )).andExpect(status().isUnauthorized());
    }

    @Test
    void searchAndReadContracts() throws Exception {
        var session = login();
        mvc.perform(get("/api/recipes").param("search", "BANANA").session(session))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Banana Chips"));
        mvc.perform(get("/api/recipes").param("search", "unknown").session(session))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(0)));
        mvc.perform(get("/api/recipes/{id}", recipeId).session(session))
                .andExpect(status().isOk()).andExpect(jsonPath("$.nextStatus").value("TESTED"))
                .andExpect(jsonPath("$.ingredients", hasSize(0)));
        mvc.perform(get("/api/raw-materials").session(session)).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Banana"));
        mvc.perform(get("/api/units").session(session)).andExpect(status().isOk())
                .andExpect(jsonPath("$", hasItem("GRAM")));
        mvc.perform(get("/api/recipes/999999").session(session)).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    String ingredient(String quantity) {
        return "{\"rawMaterialId\":" + materialId + ",\"quantity\":" + quantity + ",\"unit\":\"GRAM\"}";
    }

    @Test
    void persistsChangesAcrossRequestsAndEnforcesLifecycle() throws Exception {
        var session = login();
        mvc.perform(post("/api/recipes/{id}/ingredients", recipeId).session(session)
                .contentType("application/json").content(ingredient("300")))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.ingredients[0].id").isNumber());
        mvc.perform(get("/api/recipes/{id}", recipeId).session(session))
                .andExpect(jsonPath("$.ingredients[0].quantity").value(300))
                .andExpect(jsonPath("$.ingredients[0].name").value("Banana"));
        mvc.perform(patch("/api/recipes/{id}/status", recipeId).session(session)
                .contentType("application/json").content("{\"status\":\"APPROVED\"}"))
                .andExpect(status().isConflict());
        for (String status : new String[]{"TESTED", "APPROVED", "IN_PRODUCTION"}) {
            mvc.perform(patch("/api/recipes/{id}/status", recipeId).session(session)
                    .contentType("application/json").content("{\"status\":\"" + status + "\"}"))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.status").value(status));
        }
        mvc.perform(post("/api/recipes/{id}/ingredients", recipeId).session(session)
                .contentType("application/json").content(ingredient("10")))
                .andExpect(status().isConflict());
        mvc.perform(get("/api/recipes/{id}", recipeId).session(session))
                .andExpect(jsonPath("$.status").value("IN_PRODUCTION"))
                .andExpect(jsonPath("$.editable").value(false))
                .andExpect(jsonPath("$.ingredients", hasSize(1)));
    }

    @Test
    void rejectsInvalidInputWithoutWriting() throws Exception {
        var session = login();
        for (String body : new String[]{ingredient("0"), ingredient("-1"), ingredient("1.001"),
                ingredient("100000000"), "{}", "{", ingredient("1").replace("GRAM", "INVALID"),
                ingredient("1").replace("\"GRAM\"", "null")}) {
            mvc.perform(post("/api/recipes/{id}/ingredients", recipeId).session(session)
                    .contentType("application/json").content(body))
                    .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").isNotEmpty());
        }
        mvc.perform(get("/api/recipes/{id}", recipeId).session(session))
                .andExpect(jsonPath("$.ingredients", hasSize(0)));
        mvc.perform(get("/api/recipes/not-a-number").session(session)).andExpect(status().isBadRequest());
    }
}
