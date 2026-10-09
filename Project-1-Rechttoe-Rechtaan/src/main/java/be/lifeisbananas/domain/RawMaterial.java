package be.lifeisbananas.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Basisklasse van de grondstoffen. Fruit en groenten staan samen in één tabel;
 * de kolom {@code material_type} houdt bij welke soort het is.
 * <p>
 * De voedingswaarde is een compositie: ze wordt mee bewaard en mee verwijderd.
 */
@Entity
@Table(name = "raw_materials")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "material_type", discriminatorType = DiscriminatorType.STRING)
@Getter
@Setter
@NoArgsConstructor
public abstract class RawMaterial {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String name;

	@Column(name = "organic_certified")
	private boolean organicCertified;

	private BigDecimal price;

	private String unit;

	@OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "nutritional_value_id")
	private NutritionalValue nutritionalValue;

	protected RawMaterial(String name, boolean organicCertified, BigDecimal price, String unit) {
		this.name = name;
		this.organicCertified = organicCertified;
		this.price = price;
		this.unit = unit;
	}

	/** Het soort grondstof, zoals het in de discriminatorkolom terechtkomt. */
	public abstract String getMaterialType();
}
