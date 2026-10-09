package be.lifeisbananas.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * De bio-ingenieur: hoofdgebruiker van de toepassing. Hij stelt recepturen op,
 * ontwikkelt producten en schrijft vriesdrooginstructies.
 */
@Entity
@Table(name = "bio_engineers")
@Getter
@Setter
@NoArgsConstructor
public class BioEngineer {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank(message = "Naam is verplicht")
	private String name;

	@NotBlank(message = "E-mailadres is verplicht")
	@Email(message = "Dit is geen geldig e-mailadres")
	@Column(unique = true)
	private String email;

	@Column(name = "phone_number")
	private String phoneNumber;

	private String specialization;

	/**
	 * Enkel om aan te melden. In een echte toepassing hoort hier een
	 * versleuteld wachtwoord te staan; zie de README.
	 */
	@NotBlank(message = "Wachtwoord is verplicht")
	private String password;

	@OneToMany(mappedBy = "bioEngineer")
	private List<Formula> formulas = new ArrayList<>();

	@OneToMany(mappedBy = "bioEngineer")
	private List<Product> products = new ArrayList<>();

	@OneToMany(mappedBy = "bioEngineer")
	private List<FreezeDryInstruction> freezeDryInstructions = new ArrayList<>();

	public BioEngineer(String name, String email, String phoneNumber, String specialization, String password) {
		this.name = name;
		this.email = email;
		this.phoneNumber = phoneNumber;
		this.specialization = specialization;
		this.password = password;
	}

	/** Maakt een receptuur en legt meteen de verwijzing naar beide kanten. */
	public Formula createFormula(String name, String version, String preparationMethod) {
		Formula formula = new Formula(name, version, preparationMethod, this);
		formulas.add(formula);
		return formula;
	}
}
