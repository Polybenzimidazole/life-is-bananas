package be.lifeisbananas.controller;

import be.lifeisbananas.config.SessionKeys;
import be.lifeisbananas.domain.BioEngineer;
import be.lifeisbananas.domain.Formula;
import be.lifeisbananas.domain.FruitRawMaterial;
import be.lifeisbananas.domain.LifecycleStatus;
import be.lifeisbananas.domain.RawMaterial;
import be.lifeisbananas.domain.UnitType;
import be.lifeisbananas.service.FormulaService;
import be.lifeisbananas.service.RawMaterialService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * Testen van de weblaag: de controller en de Thymeleaf-templates worden echt
 * uitgevoerd, de servicelaag wordt nagebootst. Er is geen databank nodig.
 */
@WebMvcTest(ReceptController.class)
class ReceptControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private FormulaService formulaService;

	@MockitoBean
	private RawMaterialService rawMaterialService;

	private Formula receptuur;

	@BeforeEach
	void setUp() {
		BioEngineer bioEngineer = new BioEngineer("Dr. Sarah Johnson", "sarah.johnson@lifeisbananas.com",
				null, "Fruit Processing", "banaan123");
		receptuur = new Formula("Banana Chips Formula", "1.0", "1. Was de bananen.", bioEngineer);
		receptuur.setId(1L);

		RawMaterial banaan = new FruitRawMaterial("Banana", true, new BigDecimal("2.50"), "kg");
		banaan.setId(7L);
		when(rawMaterialService.findAll()).thenReturn(List.of(banaan));
	}

	/** Elk verzoek doet zich voor als een aangemelde bio-ingenieur. */
	private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder aangemeld(
			org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder builder) {
		return builder
				.sessionAttr(SessionKeys.BIO_ENGINEER_ID, 1L)
				.sessionAttr(SessionKeys.BIO_ENGINEER_NAME, "Dr. Sarah Johnson");
	}

	@Test
	void wieNietAangemeldIsWordtNaarHetAanmeldschermGestuurd() throws Exception {
		mockMvc.perform(get("/recepten.html"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/login.html"));
	}

	@Test
	void deLijstToontDeGevondenRecepturen() throws Exception {
		when(formulaService.search("Banana")).thenReturn(List.of(receptuur));

		mockMvc.perform(aangemeld(get("/recepten.html").param("zoek", "Banana")))
				.andExpect(status().isOk())
				.andExpect(view().name("recepten"))
				.andExpect(content().string(containsString("Banana Chips Formula")))
				.andExpect(content().string(containsString("IN_DEVELOPMENT")));
	}

	@Test
	void eenZoekopdrachtZonderResultaatWordtNetjesGemeld() throws Exception {
		when(formulaService.search("Chocolate")).thenReturn(List.of());

		mockMvc.perform(aangemeld(get("/recepten.html").param("zoek", "Chocolate")))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("Geen recepturen gevonden")));
	}

	@Test
	void deDetailpaginaToontDeReceptuurMetHaarIngredienten() throws Exception {
		RawMaterial banaan = new FruitRawMaterial("Banana", true, new BigDecimal("2.50"), "kg");
		receptuur.addIngredient(banaan, new BigDecimal("300"), UnitType.GRAM);
		when(formulaService.findById(1L)).thenReturn(receptuur);

		mockMvc.perform(aangemeld(get("/recept.html").param("id", "1")))
				.andExpect(status().isOk())
				.andExpect(view().name("recept"))
				.andExpect(content().string(containsString("Banana Chips Formula")))
				.andExpect(content().string(containsString("Banana")))
				.andExpect(content().string(containsString("300")))
				.andExpect(content().string(containsString("GRAM")));
	}

	@Test
	void eenIngredientToevoegenLeidtTerugNaarDeDetailpagina() throws Exception {
		when(formulaService.addIngredient(eq(1L), eq(7L), any(BigDecimal.class), eq(UnitType.GRAM)))
				.thenReturn(receptuur);

		mockMvc.perform(aangemeld(post("/recept/ingredient.html")
						.param("id", "1")
						.param("rawMaterialId", "7")
						.param("quantity", "300")
						.param("unit", "GRAM")))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/recept.html?id=1"))
				.andExpect(flash().attributeExists("melding"));
	}

	@Test
	void eenHoeveelheidDieGeenGetalIsLevertEenFoutmeldingOp() throws Exception {
		mockMvc.perform(aangemeld(post("/recept/ingredient.html")
						.param("id", "1")
						.param("rawMaterialId", "7")
						.param("quantity", "veel")
						.param("unit", "GRAM")))
				.andExpect(redirectedUrl("/recept.html?id=1"))
				.andExpect(flash().attribute("fout", containsString("moet een getal zijn")));
	}

	@Test
	void deStatusWijzigenLeidtTerugNaarDeDetailpagina() throws Exception {
		when(formulaService.changeStatus(1L, LifecycleStatus.TESTED)).thenReturn(receptuur);

		mockMvc.perform(aangemeld(post("/recept/status.html")
						.param("id", "1")
						.param("doelStatus", "TESTED")))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/recept.html?id=1"))
				.andExpect(flash().attributeExists("melding"));
	}

	@Test
	void eenNietToegelatenOvergangKomtAlsFoutTerug() throws Exception {
		when(formulaService.changeStatus(1L, LifecycleStatus.APPROVED))
				.thenThrow(new IllegalStateException("Overgang van IN_DEVELOPMENT naar APPROVED is niet toegelaten"));

		mockMvc.perform(aangemeld(post("/recept/status.html")
						.param("id", "1")
						.param("doelStatus", "APPROVED")))
				.andExpect(redirectedUrl("/recept.html?id=1"))
				.andExpect(flash().attribute("fout", containsString("niet toegelaten")));
	}
}
