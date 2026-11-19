package org.example.ai;

import java.util.List;

/** Een losstaande momentopname: geen JPA-entiteiten of gebruikersgegevens in de prompt. */
public record RecipeContext(int recipeId, String title, List<Ingredient> ingredients) {

    public record Ingredient(String name, double quantityPerUnit, String unit, Double availableStock) {
    }

    public List<String> sources() {
        return List.of("Recept #" + recipeId + ": " + title,
                "Actuele voorraad van de ingrediënten van dit recept");
    }
}
