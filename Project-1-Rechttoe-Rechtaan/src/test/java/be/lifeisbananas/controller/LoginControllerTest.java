package be.lifeisbananas.controller;

import be.lifeisbananas.config.SessionKeys;
import be.lifeisbananas.domain.BioEngineer;
import be.lifeisbananas.service.BioEngineerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(LoginController.class)
class LoginControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private BioEngineerService bioEngineerService;

	@Test
	void hetAanmeldschermWordtGetoond() throws Exception {
		mockMvc.perform(get("/login.html"))
				.andExpect(status().isOk())
				.andExpect(view().name("login"))
				.andExpect(content().string(containsString("Aanmelden")));
	}

	@Test
	void eenJuisteAanmeldingZetDeBioIngenieurInDeSessie() throws Exception {
		BioEngineer sarah = new BioEngineer("Dr. Sarah Johnson", "sarah.johnson@lifeisbananas.com",
				null, "Fruit Processing", "banaan123");
		sarah.setId(1L);
		when(bioEngineerService.authenticate("sarah.johnson@lifeisbananas.com", "banaan123"))
				.thenReturn(Optional.of(sarah));

		mockMvc.perform(post("/login.html")
						.param("email", "sarah.johnson@lifeisbananas.com")
						.param("password", "banaan123"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/recepten.html"))
				.andExpect(request().sessionAttribute(SessionKeys.BIO_ENGINEER_ID, 1L))
				.andExpect(request().sessionAttribute(SessionKeys.BIO_ENGINEER_NAME, "Dr. Sarah Johnson"));
	}

	@Test
	void eenVerkeerdWachtwoordLevertEenFoutmeldingOp() throws Exception {
		when(bioEngineerService.authenticate("sarah.johnson@lifeisbananas.com", "verkeerd"))
				.thenReturn(Optional.empty());

		mockMvc.perform(post("/login.html")
						.param("email", "sarah.johnson@lifeisbananas.com")
						.param("password", "verkeerd"))
				.andExpect(status().isOk())
				.andExpect(view().name("login"))
				.andExpect(content().string(containsString("Onbekend e-mailadres of verkeerd wachtwoord")));
	}

	@Test
	void afmeldenLeidtTerugNaarHetAanmeldscherm() throws Exception {
		mockMvc.perform(get("/logout.html")
						.sessionAttr(SessionKeys.BIO_ENGINEER_ID, 1L))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/login.html"));
	}
}
