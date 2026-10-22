package be.lifeisbananas.controller.api;

import be.lifeisbananas.domain.*;
import be.lifeisbananas.service.FormulaService;
import be.lifeisbananas.service.RawMaterialService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** JSON-contracten houden JPA-relaties en wachtwoorden buiten de API. */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RecipeApiController {
    private final FormulaService formulas;
    private final RawMaterialService materials;

    @GetMapping("/recipes")
    public List<RecipeSummary> search(@RequestParam(defaultValue = "") String search) {
        return formulas.search(search).stream().map(RecipeSummary::from).toList();
    }

    @GetMapping("/recipes/{id}")
    public RecipeDetail detail(@PathVariable Long id) {
        return RecipeDetail.from(formulas.findById(id));
    }

    @GetMapping("/raw-materials")
    public List<Material> rawMaterials() {
        return materials.findAll().stream()
                .map(m -> new Material(m.getId(), m.getName(), m.getMaterialType(), m.isOrganicCertified()))
                .toList();
    }

    @GetMapping("/units")
    public UnitType[] units() {
        return UnitType.values();
    }

    @PostMapping("/recipes/{id}/ingredients")
    @ResponseStatus(HttpStatus.CREATED)
    public RecipeDetail addIngredient(@PathVariable Long id, @Valid @RequestBody AddIngredient request) {
        return RecipeDetail.from(formulas.addIngredient(id, request.rawMaterialId(), request.quantity(), request.unit()));
    }

    @PatchMapping("/recipes/{id}/status")
    public RecipeDetail changeStatus(@PathVariable Long id, @Valid @RequestBody ChangeStatus request) {
        return RecipeDetail.from(formulas.changeStatus(id, request.status()));
    }

    public record AddIngredient(@NotNull @Positive Long rawMaterialId,
                                @NotNull @DecimalMin(value = "0", inclusive = false)
                                @Digits(integer = 8, fraction = 2) BigDecimal quantity,
                                @NotNull UnitType unit) {}
    public record ChangeStatus(@NotNull LifecycleStatus status) {}
    public record Material(Long id, String name, String type, boolean organicCertified) {}
    public record RecipeSummary(Long id, String name, String version, LifecycleStatus status, LocalDate lastModified) {
        static RecipeSummary from(Formula f) {
            return new RecipeSummary(f.getId(), f.getName(), f.getVersion(), f.getStatus(), f.getLastModified());
        }
    }
    public record IngredientView(Long id, Long rawMaterialId, String name, BigDecimal quantity,
                                 UnitType unit, int sequence) {
        static IngredientView from(Ingredient i) {
            return new IngredientView(i.getId(), i.getRawMaterial().getId(), i.getRawMaterialName(),
                    i.getQuantity(), i.getUnit(), i.getSequence());
        }
    }
    public record RecipeDetail(Long id, String name, String version, String preparationMethod,
                               LocalDate creationDate, LocalDate lastModified, LifecycleStatus status,
                               LifecycleStatus nextStatus, boolean editable, List<IngredientView> ingredients) {
        static RecipeDetail from(Formula f) {
            return new RecipeDetail(f.getId(), f.getName(), f.getVersion(), f.getPreparationMethod(),
                    f.getCreationDate(), f.getLastModified(), f.getStatus(), f.getStatus().next(),
                    f.getStatus() == LifecycleStatus.IN_DEVELOPMENT || f.getStatus() == LifecycleStatus.TESTED,
                    f.getIngredients().stream().map(IngredientView::from).toList());
        }
    }
}
