package org.example.service;

import org.example.dao.RecipeRepository;
import org.example.domain.Recipe;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.List;


@Service
public class RecipeServiceImpl implements RecipeService {

    @Autowired
    private RecipeRepository recipeRepository;

    @Override
    public Recipe getRecipe(int id) {
       return recipeRepository.findById(id).orElse(null);
    }

    @Override
    public List<Recipe> getAllRecipes() {
        return recipeRepository.findAll();
    }

    @Override
    public Recipe addRecipe(Recipe recipe) {
        return recipeRepository.save(recipe);
    }

    @Override
    public Recipe updateRecipe(int id,Recipe recipe) {
        Recipe oldRecipe = recipeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Recipe niet gevonden met id: " + id));

        oldRecipe.setName(recipe.getName());
        oldRecipe.setDescription(recipe.getDescription());


        return recipeRepository.save(oldRecipe);
    }

    @Override
    public void deleteRecipe(int id) {
        recipeRepository.deleteById(id);
    }
}
