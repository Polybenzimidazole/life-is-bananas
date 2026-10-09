package be.lifeisbananas.domain;

import be.lifeisbananas.dao.BioEngineerRepository;
import be.lifeisbananas.dao.FormulaRepository;
import be.lifeisbananas.dao.RawMaterialRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

/**
 * Controleert dat het volledige domeinmodel op een databank afgebeeld kan
 * worden, en dat de opzoekmethodes van de repositories werken.
 * <p>
 * Deze test draait op een in-memory databank die Hibernate zelf opbouwt uit de
 * entiteiten. Er is dus geen MySQL nodig. Let op: dit controleert het
 * domeinmodel, niet of db/life_is_bananas.sql daarmee overeenkomt.
 */
@DataJpaTest
@TestPropertySource(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
class DomeinMappingTest {

	@Autowired
	private BioEngineerRepository bioEngineerRepository;

	@Autowired
	private FormulaRepository formulaRepository;

	@Autowired
	private RawMaterialRepository rawMaterialRepository;

	@Autowired
	private EntityManager entityManager;

	private BioEngineer bewaardeBioEngineer() {
		return bioEngineerRepository.save(new BioEngineer("Dr. Sarah Johnson",
				"sarah.johnson@lifeisbananas.com", "+32-123-456-789", "Fruit Processing", "banaan123"));
	}

	private RawMaterial bewaardeBanaan() {
		FruitRawMaterial banaan = new FruitRawMaterial("Banana", true, new BigDecimal("2.50"), "kg");
		banaan.setRipenessLevel("rijp");
		banaan.setSugarContent(12.2);
		banaan.setNutritionalValue(new NutritionalValue(89.0, 1.1, 22.8, 0.3));
		return rawMaterialRepository.save(banaan);
	}

	@Test
	void eenBioIngenieurWordtBewaardEnTeruggevondenOpEmail() {
		bewaardeBioEngineer();

		assertThat(bioEngineerRepository.findByEmail("sarah.johnson@lifeisbananas.com"))
				.get()
				.extracting(BioEngineer::getName)
				.isEqualTo("Dr. Sarah Johnson");
		assertThat(bioEngineerRepository.findByEmail("niemand@odisee.be")).isEmpty();
	}

	@Test
	void fruitEnGroenteStaanSamenInEenTabelMetHunEigenKenmerken() {
		bewaardeBanaan();
		VegetableRawMaterial spinazie = new VegetableRawMaterial("Spinach", false, new BigDecimal("3.20"), "kg");
		spinazie.setFirmness("stevig");
		spinazie.setMoistureContent(91.4);
		spinazie.setNutritionalValue(new NutritionalValue(23.0, 2.9, 3.6, 0.4));
		rawMaterialRepository.save(spinazie);

		entityManager.flush();
		entityManager.clear();

		List<RawMaterial> grondstoffen = rawMaterialRepository.findAllByOrderByNameAsc();

		assertThat(grondstoffen)
				.extracting(RawMaterial::getName, RawMaterial::getMaterialType)
				.containsExactly(tuple("Banana", "FRUIT"), tuple("Spinach", "VEGETABLE"));
		assertThat(grondstoffen.get(0).getNutritionalValue().getCalories()).isEqualTo(89.0);
	}

	@Test
	void eenReceptuurBewaartHaarIngredientenInVolgorde() {
		BioEngineer sarah = bewaardeBioEngineer();
		RawMaterial banaan = bewaardeBanaan();
		RawMaterial aardbei = rawMaterialRepository.save(
				new FruitRawMaterial("Strawberry", true, new BigDecimal("6.80"), "kg"));

		Formula receptuur = new Formula("Banana Chips Formula", "1.0", "1. Was de bananen.", sarah);
		receptuur.addIngredient(aardbei, new BigDecimal("200"), UnitType.GRAM);
		receptuur.addIngredient(banaan, new BigDecimal("300"), UnitType.GRAM);
		Long id = formulaRepository.save(receptuur).getId();

		entityManager.flush();
		entityManager.clear();

		Formula bewaard = formulaRepository.findByIdWithIngredients(id).orElseThrow();

		assertThat(bewaard.getStatus()).isEqualTo(LifecycleStatus.IN_DEVELOPMENT);
		assertThat(bewaard.getIngredientCount()).isEqualTo(2);
		assertThat(bewaard.getIngredients())
				.extracting(Ingredient::getSequence, Ingredient::getRawMaterialName, Ingredient::getQuantityDisplay)
				.containsExactly(
						tuple(1, "Strawberry", "200"),
						tuple(2, "Banana", "300"));
	}

	@Test
	void erWordtGezochtOpEenStukVanDeNaamZonderOpHoofdlettersTeLetten() {
		BioEngineer sarah = bewaardeBioEngineer();
		formulaRepository.save(new Formula("Banana Chips Formula", "1.0", "...", sarah));
		formulaRepository.save(new Formula("Green Smoothie Mix", "2.1", "...", sarah));

		assertThat(formulaRepository.findByNameContainingIgnoreCaseOrderByNameAsc("banana"))
				.extracting(Formula::getName)
				.containsExactly("Banana Chips Formula");
		assertThat(formulaRepository.findByNameContainingIgnoreCaseOrderByNameAsc("Chocolate")).isEmpty();
		assertThat(formulaRepository.findAllByOrderByNameAsc()).hasSize(2);
	}

	@Test
	void eenProductEnEenVriesdrooginstructieWordenOokCorrectAfgebeeld() {
		BioEngineer sarah = bewaardeBioEngineer();
		Formula receptuur = formulaRepository.save(
				new Formula("Banana Chips Formula", "1.0", "...", sarah));

		Product product = new Product("Banana Chips Original", "Crispy freeze-dried banana slices",
				365, 50, "BATCH-2024-001");
		product.setFormula(receptuur);
		product.setBioEngineer(sarah);
		entityManager.persist(product);

		FreezeDryInstruction instructie = new FreezeDryInstruction("Banana Chips Formula", "1.0",
				"1. Was de bananen.", "Voorzichtig behandelen");
		instructie.setFormula(receptuur);
		instructie.setBioEngineer(sarah);
		instructie.addParameter("Temperatuur fase 1", "-40", "C");
		entityManager.persist(instructie);

		entityManager.flush();
		entityManager.clear();

		Product bewaardProduct = entityManager.find(Product.class, product.getId());
		assertThat(bewaardProduct.getStatus()).isEqualTo(LifecycleStatus.IN_DEVELOPMENT);
		assertThat(bewaardProduct.isReadyForSale()).isFalse();
		assertThat(bewaardProduct.getFormula().getName()).isEqualTo("Banana Chips Formula");

		FreezeDryInstruction bewaardeInstructie =
				entityManager.find(FreezeDryInstruction.class, instructie.getId());
		assertThat(bewaardeInstructie.getParameters())
				.extracting(Parameter::getName, Parameter::getValue)
				.containsExactly(tuple("Temperatuur fase 1", "-40"));
	}
}
