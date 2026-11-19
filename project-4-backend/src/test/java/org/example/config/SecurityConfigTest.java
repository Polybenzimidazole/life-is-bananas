package org.example.config;

import org.example.controllers.HomeController;
import org.example.controllers.RecipeRestController;
import org.example.controllers.StockRestController;
import org.example.domain.User;
import org.example.service.LifeIsBananasServiceimpl;
import org.example.service.UserDetailsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.net.URI;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {HomeController.class, RecipeRestController.class, StockRestController.class},
        excludeAutoConfiguration = UserDetailsServiceAutoConfiguration.class,
        properties = {"app.frontend-url=http://localhost:8081/", "debug=false"})
@Import(SecurityConfig.class)
class SecurityConfigTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private LifeIsBananasServiceimpl service;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    @BeforeEach
    void usersAndRecipes() {
        when(userDetailsService.loadUserByUsername("admin")).thenReturn(
                new User("admin", "{noop}password", "ROLE_ADMIN", "Admin", "User", "admin@example.com"));
        when(service.GetAllRecipe()).thenReturn(List.of());
    }

    @Test
    void loginPreservesTheCurrentHostAndAuthenticatedSession() throws Exception {
        for (String host : List.of("localhost", "127.0.0.1")) {
            var result = mvc.perform(post("http://" + host + ":8081/login")
                            .param("username", "admin")
                            .param("password", "password")
                            .with(csrf()))
                    .andExpect(status().isFound())
                    .andExpect(redirectedUrl("/"))
                    .andReturn();

            var session = (MockHttpSession) result.getRequest().getSession(false);
            assertThat(session).isNotNull();
            mvc.perform(get("http://" + host + ":8081/recipes/all").session(session))
                    .andExpect(status().isOk())
                    .andExpect(content().json("[]"));
        }
    }

    @Test
    void backendRootRedirectsAnonymousVisitorsToTheFrontendOnTheSameLoopbackHost() throws Exception {
        mvc.perform(get("http://127.0.0.1:8080/"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("http://127.0.0.1:8081/"));
        mvc.perform(get("http://localhost:8080/"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("http://localhost:8081/"));
    }

    @Test
    void backendRootUsesTheConfiguredFrontendForOtherHosts() throws Exception {
        mvc.perform(get("http://unrelated.example/"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("http://localhost:8081/"));
    }

    @Test
    void externalFrontendConfigurationKeepsItsHostForLoopbackVisitors() throws Exception {
        var externalFrontend = new HomeController(URI.create("https://frontend.example/app/"));
        MockMvcBuilders.standaloneSetup(externalFrontend).build()
                .perform(get("http://127.0.0.1:8080/"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("https://frontend.example/app/"));
    }

    @Test
    void anonymousApiReadsReturn401WithoutRedirectingToHtml() throws Exception {
        for (String path : List.of("/recipes/all", "/stocks/all")) {
            mvc.perform(get(path).accept("application/json"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(header().doesNotExist("Location"))
                    .andExpect(content().string(""));
        }
        mvc.perform(get("/login")).andExpect(status().isOk());
    }

    @Test
    void writesStillRequireAdminAndCsrf() throws Exception {
        mvc.perform(post("/stocks/add/1")
                        .with(user("engineer").roles("USER"))
                        .with(csrf())
                        .contentType("application/json").content("{\"amount\":1}"))
                .andExpect(status().isForbidden());

        mvc.perform(post("/stocks/add/1")
                        .with(user("admin").roles("ADMIN"))
                        .contentType("application/json").content("{\"amount\":1}"))
                .andExpect(status().isForbidden());

        mvc.perform(post("/stocks/add/1")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .contentType("application/json").content("{\"amount\":1}"))
                .andExpect(status().isOk());
    }
}
