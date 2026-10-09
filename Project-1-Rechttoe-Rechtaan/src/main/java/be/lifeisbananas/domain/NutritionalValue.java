package be.lifeisbananas.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Voedingswaarde per 100 gram grondstof, nodig voor de etikettering.
 * Hoort bij precies één grondstof en bestaat niet zonder die grondstof.
 */
@Entity
@Table(name = "nutritional_values")
@Getter
@Setter
@NoArgsConstructor
public class NutritionalValue {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private double calories;

	private double proteins;

	private double carbohydrates;

	private double fats;

	private double fibers;

	private double sugars;

	private double salt;

	/*
	 * Zonder expliciete naam maakt Spring hiervan de kolom "vitamina": de
	 * naamstrategie zet geen underscore voor een hoofdletter die helemaal
	 * achteraan staat. Daarom staan deze twee er voluit.
	 */
	@Column(name = "vitamin_a")
	private double vitaminA;

	@Column(name = "vitamin_c")
	private double vitaminC;

	private double calcium;

	private double iron;

	/** Beknopte constructor: de overige waarden blijven op nul staan. */
	public NutritionalValue(double calories, double proteins, double carbohydrates, double fats) {
		this.calories = calories;
		this.proteins = proteins;
		this.carbohydrates = carbohydrates;
		this.fats = fats;
	}
}
