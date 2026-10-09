package be.lifeisbananas.controller;

import be.lifeisbananas.domain.Formula;
import be.lifeisbananas.domain.LifecycleStatus;
import be.lifeisbananas.domain.UnitType;
import be.lifeisbananas.service.FormulaService;
import be.lifeisbananas.service.RawMaterialService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

/**
 * Zoeken in de recepturen, een receptuur openen, er een ingrediënt aan
 * toevoegen en haar status wijzigen.
 */
@Controller
@RequiredArgsConstructor
public class ReceptController {

	private static final String VIEW_LIJST = "recepten";
	private static final String VIEW_DETAIL = "recept";

	private final FormulaService formulaService;
	private final RawMaterialService rawMaterialService;

	@GetMapping("/recepten.html")
	public String zoeken(@RequestParam(value = "zoek", required = false) String zoekterm, ModelMap model) {
		List<Formula> recepturen = formulaService.search(zoekterm);
		model.addAttribute("zoek", zoekterm == null ? "" : zoekterm);
		model.addAttribute("recepturen", recepturen);
		return VIEW_LIJST;
	}

	@GetMapping("/recept.html")
	public String detail(@RequestParam("id") Long id, ModelMap model) {
		vulDetailModel(id, model);
		return VIEW_DETAIL;
	}

	@PostMapping("/recept/ingredient.html")
	public String ingredientToevoegen(@RequestParam("id") Long id,
									  @RequestParam("rawMaterialId") Long rawMaterialId,
									  @RequestParam("quantity") String quantity,
									  @RequestParam("unit") UnitType unit,
									  RedirectAttributes redirectAttributes) {
		try {
			formulaService.addIngredient(id, rawMaterialId, leesHoeveelheid(quantity), unit);
			redirectAttributes.addFlashAttribute("melding", "Ingrediënt toegevoegd");
		}
		catch (IllegalArgumentException | IllegalStateException e) {
			redirectAttributes.addFlashAttribute("fout", e.getMessage());
		}
		return "redirect:/recept.html?id=" + id;
	}

	@PostMapping("/recept/status.html")
	public String statusWijzigen(@RequestParam("id") Long id,
								 @RequestParam("doelStatus") LifecycleStatus doelStatus,
								 RedirectAttributes redirectAttributes) {
		try {
			formulaService.changeStatus(id, doelStatus);
			redirectAttributes.addFlashAttribute("melding", "Status gewijzigd naar " + doelStatus);
		}
		catch (IllegalStateException e) {
			redirectAttributes.addFlashAttribute("fout", e.getMessage());
		}
		return "redirect:/recept.html?id=" + id;
	}

	private void vulDetailModel(Long id, ModelMap model) {
		model.addAttribute("receptuur", formulaService.findById(id));
		model.addAttribute("grondstoffen", rawMaterialService.findAll());
		model.addAttribute("eenheden", UnitType.values());
		model.addAttribute("statussen", LifecycleStatus.values());
	}

	/** Aanvaardt zowel "300" als "300,5": de komma is hier gebruikelijk. */
	private BigDecimal leesHoeveelheid(String quantity) {
		if (quantity == null || quantity.isBlank()) {
			throw new IllegalArgumentException("Vul een hoeveelheid in");
		}
		try {
			return new BigDecimal(quantity.trim().replace(',', '.'));
		}
		catch (NumberFormatException e) {
			throw new IllegalArgumentException("De hoeveelheid moet een getal zijn");
		}
	}
}
