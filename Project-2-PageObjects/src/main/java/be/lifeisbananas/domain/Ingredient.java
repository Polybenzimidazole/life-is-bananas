package be.lifeisbananas.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * De koppeling tussen een receptuur en een grondstof: hoeveel ervan nodig is,
 * in welke eenheid, en de hoeveelste stap het is in de bereiding.
 */
@Entity
@Table(name = "ingredients")
@Getter
@Setter
@NoArgsConstructor
public class Ingredient {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private BigDecimal quantity;

	@Enumerated(EnumType.STRING)
	private UnitType unit;

	private int sequence;

	@ManyToOne
	@JoinColumn(name = "formula_id")
	private Formula formula;

	@ManyToOne
	@JoinColumn(name = "raw_material_id")
	private RawMaterial rawMaterial;

	public Ingredient(Formula formula, RawMaterial rawMaterial, BigDecimal quantity, UnitType unit, int sequence) {
		this.formula = formula;
		this.rawMaterial = rawMaterial;
		this.quantity = quantity;
		this.unit = unit;
		this.sequence = sequence;
	}

	/** De hoeveelheid zonder overbodige nullen, bv. {@code 300} in plaats van {@code 300.00}. */
	public String getQuantityDisplay() {
		return quantity == null ? "" : quantity.stripTrailingZeros().toPlainString();
	}

	public String getRawMaterialName() {
		return rawMaterial == null ? "" : rawMaterial.getName();
	}
}
