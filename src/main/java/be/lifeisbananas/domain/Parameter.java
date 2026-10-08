package be.lifeisbananas.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Eén proceswaarde uit een vriesdrooginstructie, bv. "Temperatuur fase 1".
 */
@Entity
@Table(name = "parameters")
@Getter
@Setter
@NoArgsConstructor
public class Parameter {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String name;

	/** "value" is in H2 een gereserveerd woord, vandaar de afwijkende kolomnaam. */
	@Column(name = "parameter_value")
	private String value;

	private String unit;

	@Column(name = "minimum_limit")
	private Double minimumLimit;

	@Column(name = "maximum_limit")
	private Double maximumLimit;

	private boolean critical;

	@ManyToOne
	@JoinColumn(name = "freeze_dry_instruction_id")
	private FreezeDryInstruction freezeDryInstruction;

	public Parameter(FreezeDryInstruction freezeDryInstruction, String name, String value, String unit) {
		this.freezeDryInstruction = freezeDryInstruction;
		this.name = name;
		this.value = value;
		this.unit = unit;
	}

	/** Ligt de gemeten waarde binnen de grenzen die hier vastgelegd zijn? */
	public boolean isWithinLimits(double measured) {
		if (minimumLimit != null && measured < minimumLimit) {
			return false;
		}
		return maximumLimit == null || measured <= maximumLimit;
	}
}
