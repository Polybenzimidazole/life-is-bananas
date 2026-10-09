package be.lifeisbananas.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * De bedrijfsregels van de receptuur, los van databank en webtoepassing.
 */
class FormulaTest {

	private BioEngineer bioEngineer;
	private Formula formula;
	private RawMaterial banaan;

	@BeforeEach
	void setUp() {
		bioEngineer = new BioEngineer("Dr. Sarah Johnson", "sarah.johnson@lifeisbananas.com",
				"+32-123-456-789", "Fruit Processing", "banaan123");
		formula = new Formula("Banana Chips Formula", "1.0", "1. Was de bananen.", bioEngineer);
		banaan = new FruitRawMaterial("Banana", true, new BigDecimal("2.50"), "kg");
	}

	@Test
	void eenNieuweReceptuurStaatInOntwikkelingEnIsLeeg() {
		assertThat(formula.getStatus()).isEqualTo(LifecycleStatus.IN_DEVELOPMENT);
		assertThat(formula.getIngredientCount()).isZero();
	}

	@Test
	void ingredientenKrijgenOplopendeStapnummers() {
		RawMaterial aardbei = new FruitRawMaterial("Strawberry", true, new BigDecimal("6.80"), "kg");

		Ingredient eerste = formula.addIngredient(aardbei, new BigDecimal("200"), UnitType.GRAM);
		Ingredient tweede = formula.addIngredient(banaan, new BigDecimal("300"), UnitType.GRAM);

		assertThat(eerste.getSequence()).isEqualTo(1);
		assertThat(tweede.getSequence()).isEqualTo(2);
		assertThat(formula.getIngredientCount()).isEqualTo(2);
		assertThat(tweede.getRawMaterialName()).isEqualTo("Banana");
		assertThat(tweede.getQuantityDisplay()).isEqualTo("300");
	}

	@Test
	void eenHoeveelheidVanNulOfMinderWordtGeweigerd() {
		assertThatThrownBy(() -> formula.addIngredient(banaan, BigDecimal.ZERO, UnitType.GRAM))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("groter zijn dan nul");
	}

	@Test
	void eenIngredientZonderGrondstofWordtGeweigerd() {
		assertThatThrownBy(() -> formula.addIngredient(null, new BigDecimal("300"), UnitType.GRAM))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	@DisplayName("De levenscyclus loopt IN_DEVELOPMENT -> TESTED -> APPROVED -> IN_PRODUCTION")
	void deStatusSchuiftStapVoorStapOp() {
		formula.changeStatus(LifecycleStatus.TESTED);
		assertThat(formula.getStatus()).isEqualTo(LifecycleStatus.TESTED);

		formula.changeStatus(LifecycleStatus.APPROVED);
		assertThat(formula.getStatus()).isEqualTo(LifecycleStatus.APPROVED);

		formula.changeStatus(LifecycleStatus.IN_PRODUCTION);
		assertThat(formula.getStatus()).isEqualTo(LifecycleStatus.IN_PRODUCTION);
	}

	@Test
	void eenStapOverslaanMagNiet() {
		assertThatThrownBy(() -> formula.changeStatus(LifecycleStatus.APPROVED))
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("IN_DEVELOPMENT")
				.hasMessageContaining("APPROVED");

		assertThat(formula.getStatus()).isEqualTo(LifecycleStatus.IN_DEVELOPMENT);
	}

	@Test
	void terugkerenNaarEenVorigeStatusMagNiet() {
		formula.changeStatus(LifecycleStatus.TESTED);

		assertThatThrownBy(() -> formula.changeStatus(LifecycleStatus.IN_DEVELOPMENT))
				.isInstanceOf(IllegalStateException.class);
	}

	@Test
	void eenGoedgekeurdeReceptuurMagNietMeerGewijzigdWorden() {
		formula.changeStatus(LifecycleStatus.TESTED);
		formula.changeStatus(LifecycleStatus.APPROVED);

		assertThatThrownBy(() -> formula.addIngredient(banaan, new BigDecimal("300"), UnitType.GRAM))
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("goedgekeurde receptuur");
	}

	@Test
	void eenReceptuurInOntwikkelingMagNogWelAangevuldWorden() {
		formula.changeStatus(LifecycleStatus.TESTED);

		formula.addIngredient(banaan, new BigDecimal("300"), UnitType.GRAM);

		assertThat(formula.getIngredientCount()).isEqualTo(1);
	}
}
