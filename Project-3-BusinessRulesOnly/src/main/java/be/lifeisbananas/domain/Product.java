package be.lifeisbananas.domain;

import jakarta.persistence.Column;
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

import java.time.LocalDate;

/**
 * Het eindproduct: de vriesgedroogde snack die uit een receptuur voortkomt.
 * Doorloopt dezelfde levenscyclus als de receptuur waarop het gebaseerd is.
 */
@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
public class Product {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String name;

	@Column(length = 2000)
	private String description;

	/** Houdbaarheid in dagen. */
	@Column(name = "shelf_life")
	private Integer shelfLife;

	/** Gewicht in gram. */
	private Integer weight;

	@Column(name = "batch_number")
	private String batchNumber;

	@Column(name = "production_date")
	private LocalDate productionDate;

	@Enumerated(EnumType.STRING)
	private LifecycleStatus status = LifecycleStatus.IN_DEVELOPMENT;

	@ManyToOne
	@JoinColumn(name = "formula_id")
	private Formula formula;

	@ManyToOne
	@JoinColumn(name = "bio_engineer_id")
	private BioEngineer bioEngineer;

	public Product(String name, String description, Integer shelfLife, Integer weight, String batchNumber) {
		this.name = name;
		this.description = description;
		this.shelfLife = shelfLife;
		this.weight = weight;
		this.batchNumber = batchNumber;
		this.status = LifecycleStatus.IN_DEVELOPMENT;
	}

	/**
	 * Zet het product één stap verder in zijn levenscyclus.
	 *
	 * @throws IllegalStateException als die overgang niet toegelaten is
	 */
	public void changeStatus(LifecycleStatus target) {
		if (!status.mayChangeTo(target)) {
			throw new IllegalStateException(
					"Overgang van " + status + " naar " + target + " is niet toegelaten");
		}
		this.status = target;
		if (target == LifecycleStatus.IN_PRODUCTION) {
			this.productionDate = LocalDate.now();
		}
	}

	public boolean isReadyForSale() {
		return status == LifecycleStatus.IN_PRODUCTION;
	}

	/** De uiterste houdbaarheidsdatum, of leeg zolang er niet geproduceerd wordt. */
	public LocalDate getExpirationDate() {
		if (productionDate == null || shelfLife == null) {
			return null;
		}
		return productionDate.plusDays(shelfLife);
	}
}
