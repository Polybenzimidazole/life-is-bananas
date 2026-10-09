package be.lifeisbananas.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * De receptuur: de exacte samenstelling en bereidingswijze van een snack.
 * Dit is het hart van de productontwikkeling.
 */
@Entity
@Table(name = "formulas")
@Getter
@Setter
@NoArgsConstructor
public class Formula {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String name;

	private String version;

	@Column(name = "preparation_method", length = 2000)
	private String preparationMethod;

	@Column(name = "creation_date")
	private LocalDate creationDate;

	@Column(name = "last_modified")
	private LocalDate lastModified;

	@Enumerated(EnumType.STRING)
	private LifecycleStatus status = LifecycleStatus.IN_DEVELOPMENT;

	@ManyToOne
	@JoinColumn(name = "bio_engineer_id")
	private BioEngineer bioEngineer;

	/** De ingrediënten staan steeds in de volgorde van hun stapnummer. */
	@OneToMany(mappedBy = "formula", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("sequence ASC")
	private List<Ingredient> ingredients = new ArrayList<>();

	public Formula(String name, String version, String preparationMethod, BioEngineer bioEngineer) {
		this.name = name;
		this.version = version;
		this.preparationMethod = preparationMethod;
		this.bioEngineer = bioEngineer;
		this.creationDate = LocalDate.now();
		this.lastModified = LocalDate.now();
		this.status = LifecycleStatus.IN_DEVELOPMENT;
	}

	/**
	 * Voegt een grondstof als ingrediënt toe, achteraan in de bereidingsvolgorde.
	 *
	 * @throws IllegalStateException    als de receptuur al goedgekeurd is
	 * @throws IllegalArgumentException als er geen grondstof of hoeveelheid is
	 */
	public Ingredient addIngredient(RawMaterial rawMaterial, BigDecimal quantity, UnitType unit) {
		if (rawMaterial == null) {
			throw new IllegalArgumentException("Kies een grondstof");
		}
		if (quantity == null || quantity.signum() <= 0) {
			throw new IllegalArgumentException("De hoeveelheid moet groter zijn dan nul");
		}
		if (status == LifecycleStatus.APPROVED || status == LifecycleStatus.IN_PRODUCTION) {
			throw new IllegalStateException(
					"Een goedgekeurde receptuur mag niet meer gewijzigd worden");
		}
		Ingredient ingredient = new Ingredient(this, rawMaterial, quantity, unit, nextSequence());
		ingredients.add(ingredient);
		this.lastModified = LocalDate.now();
		return ingredient;
	}

	private int nextSequence() {
		return ingredients.stream()
				.mapToInt(Ingredient::getSequence)
				.max()
				.orElse(0) + 1;
	}

	/**
	 * Zet de receptuur één stap verder in haar levenscyclus.
	 *
	 * @throws IllegalStateException als die overgang niet toegelaten is
	 */
	public void changeStatus(LifecycleStatus target) {
		if (!status.mayChangeTo(target)) {
			throw new IllegalStateException(
					"Overgang van " + status + " naar " + target + " is niet toegelaten");
		}
		this.status = target;
		this.lastModified = LocalDate.now();
	}

	public int getIngredientCount() {
		return ingredients.size();
	}
}
