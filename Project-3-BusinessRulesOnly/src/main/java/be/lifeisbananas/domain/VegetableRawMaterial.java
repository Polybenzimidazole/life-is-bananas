package be.lifeisbananas.domain;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/** Groente als grondstof, met haar eigen stevigheid en vochtgehalte. */
@Entity
@DiscriminatorValue("VEGETABLE")
@Getter
@Setter
@NoArgsConstructor
public class VegetableRawMaterial extends RawMaterial {

	private String firmness;

	@Column(name = "moisture_content")
	private Double moistureContent;

	public VegetableRawMaterial(String name, boolean organicCertified, BigDecimal price, String unit) {
		super(name, organicCertified, price, unit);
	}

	@Override
	public String getMaterialType() {
		return "VEGETABLE";
	}
}
