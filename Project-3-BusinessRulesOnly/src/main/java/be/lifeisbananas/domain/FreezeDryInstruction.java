package be.lifeisbananas.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * De technische instructies voor de externe vriesdroger. De parameters horen
 * er onlosmakelijk bij: ze worden mee bewaard en mee verwijderd.
 */
@Entity
@Table(name = "freeze_dry_instructions")
@Getter
@Setter
@NoArgsConstructor
public class FreezeDryInstruction {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "formula_name")
	private String formulaName;

	private String version;

	@Column(name = "creation_date")
	private LocalDate creationDate;

	@Column(length = 4000)
	private String steps;

	@Column(length = 2000)
	private String remarks;

	@ManyToOne
	@JoinColumn(name = "bio_engineer_id")
	private BioEngineer bioEngineer;

	@ManyToOne
	@JoinColumn(name = "formula_id")
	private Formula formula;

	@OneToMany(mappedBy = "freezeDryInstruction", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Parameter> parameters = new ArrayList<>();

	public FreezeDryInstruction(String formulaName, String version, String steps, String remarks) {
		this.formulaName = formulaName;
		this.version = version;
		this.steps = steps;
		this.remarks = remarks;
		this.creationDate = LocalDate.now();
	}

	public Parameter addParameter(String name, String value, String unit) {
		Parameter parameter = new Parameter(this, name, value, unit);
		parameters.add(parameter);
		return parameter;
	}
}
