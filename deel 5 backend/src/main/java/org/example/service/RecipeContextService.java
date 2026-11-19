package org.example.service;

import org.example.ai.RecipeContext;
import org.example.dao.RecipeRepository;
import org.example.dao.StockRepository;
import org.example.domain.Stock;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class RecipeContextService {

    private final RecipeRepository recipes;
    private final StockRepository stocks;

    public RecipeContextService(RecipeRepository recipes, StockRepository stocks) {
        this.recipes = recipes;
        this.stocks = stocks;
    }

    /** De retrieval-stap van RAG: haal het gekozen recept en de bijbehorende voorraad opnieuw op. */
    @Transactional(readOnly = true)
    public RecipeContext retrieve(int recipeId) {
        var recipe = recipes.findById(recipeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Recept niet gevonden."));

        var ingredients = recipe.getRecipeingredients().stream().map(ingredient -> {
            var material = ingredient.getRawMaterial();
            Double available = stocks.findByRawMaterial_Id(material.getId())
                    .map(Stock::getQuantity).orElse(null);
            return new RecipeContext.Ingredient(material.getName(), ingredient.getQuantity(),
                    material.getUnit(), available);
        }).toList();

        return new RecipeContext(recipe.getId(), recipe.getTitle(), ingredients);
    }
}
