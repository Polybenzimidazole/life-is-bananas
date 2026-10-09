package be.lifeisbananas.service;

import be.lifeisbananas.dao.FormulaRepository;
import be.lifeisbananas.dao.RawMaterialRepository;
import be.lifeisbananas.domain.Formula;
import be.lifeisbananas.domain.LifecycleStatus;
import be.lifeisbananas.domain.RawMaterial;
import be.lifeisbananas.domain.UnitType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FormulaServiceImpl implements FormulaService {

	private final FormulaRepository formulaRepository;
	private final RawMaterialRepository rawMaterialRepository;

	@Override
	public List<Formula> search(String zoekterm) {
		if (zoekterm == null || zoekterm.isBlank()) {
			return formulaRepository.findAllByOrderByNameAsc();
		}
		return formulaRepository.findByNameContainingIgnoreCaseOrderByNameAsc(zoekterm.trim());
	}

	@Override
	public Formula findById(Long id) {
		return formulaRepository.findByIdWithIngredients(id)
				.orElseThrow(() -> new NietGevondenException("Geen receptuur met id " + id));
	}

	@Override
	@Transactional
	public Formula addIngredient(Long formulaId, Long rawMaterialId, BigDecimal quantity, UnitType unit) {
		Formula formula = findById(formulaId);
		RawMaterial rawMaterial = rawMaterialRepository.findById(rawMaterialId)
				.orElseThrow(() -> new NietGevondenException("Geen grondstof met id " + rawMaterialId));

		// De regels zelf staan in het domein, niet hier.
		formula.addIngredient(rawMaterial, quantity, unit);
		return formulaRepository.save(formula);
	}

	@Override
	@Transactional
	public Formula changeStatus(Long formulaId, LifecycleStatus target) {
		Formula formula = findById(formulaId);
		formula.changeStatus(target);
		return formulaRepository.save(formula);
	}
}
