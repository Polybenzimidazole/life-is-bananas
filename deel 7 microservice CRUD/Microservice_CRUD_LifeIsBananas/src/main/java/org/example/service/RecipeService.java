package org.example.service;

import org.example.domain.Recipe;

import java.util.List;

public interface RecipeService {

    public Recipe getRecipe(int id);

    public List<Recipe> getAllRecipes();

    public Recipe addRecipe(Recipe recipe);

    public Recipe updateRecipe(int id, Recipe recipe);

    public void deleteRecipe(int id);

}
