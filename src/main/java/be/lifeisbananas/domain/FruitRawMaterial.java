package be.lifeisbananas.domain;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/** Fruit als grondstof, met zijn eigen rijpheids- en suikerkenmerken. */
@Entity
@DiscriminatorValue("FRUIT")
@Getter
@Setter
@NoArgsConstructor
public class FruitRawMaterial extends RawMaterial {

	@Column(name = "ripeness_level")
	private String ripenessLevel;

	@Column(name = "sugar_content")
	private Double sugarContent;

	@Column(name = "acidity_level")
	private Double acidityLevel;

	public FruitRawMaterial(String name, boolean organicCertified, BigDecimal price, String unit) {
		super(name, organicCertified, price, unit);
	}

	@Override
	public String getMaterialType() {
		return "FRUIT";
	}
}
