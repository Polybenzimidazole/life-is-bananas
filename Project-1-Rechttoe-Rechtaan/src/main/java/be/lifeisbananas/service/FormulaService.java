package be.lifeisbananas.service;

import be.lifeisbananas.domain.Formula;
import be.lifeisbananas.domain.LifecycleStatus;
import be.lifeisbananas.domain.UnitType;

import java.math.BigDecimal;
import java.util.List;

public interface FormulaService {

	/** Zoekt recepturen op een stuk van hun naam; een lege zoekterm geeft alles. */
	List<Formula> search(String zoekterm);

	/** @throws NietGevondenException als er geen receptuur met dat id is */
	Formula findById(Long id);

	/**
	 * Voegt een grondstof als ingrediënt toe aan de receptuur.
	 *
	 * @throws NietGevondenException    als receptuur of grondstof niet bestaat
	 * @throws IllegalArgumentException bij een ontbrekende of foute hoeveelheid
	 * @throws IllegalStateException    als de receptuur al goedgekeurd is
	 */
	Formula addIngredient(Long formulaId, Long rawMaterialId, BigDecimal quantity, UnitType unit);

	/**
	 * Zet de receptuur één stap verder in haar levenscyclus.
	 *
	 * @throws NietGevondenException als er geen receptuur met dat id is
	 * @throws IllegalStateException als die overgang niet toegelaten is
	 */
	Formula changeStatus(Long formulaId, LifecycleStatus target);
}
