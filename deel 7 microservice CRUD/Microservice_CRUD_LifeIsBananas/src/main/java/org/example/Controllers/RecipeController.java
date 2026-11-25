package org.example.Controllers;


import org.example.domain.Recipe;
import org.example.service.RecipeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/recipes")
public class RecipeController {

    @Autowired
    private RecipeService recipeService;

    @GetMapping("/{id}")
    public Recipe getRecipe (@PathVariable int id) {
        return recipeService.getRecipe(id);
    }

    @GetMapping("all")
    public List<Recipe> getAllRecipe () {
        return recipeService.getAllRecipes();
    }

    @PostMapping("add")
    public Recipe addRecipe (@RequestBody Recipe recipe) {
        return recipeService.addRecipe(recipe);
    }

    @PutMapping("update/{id}")
    public Recipe updateRecipe (@PathVariable int id,  @RequestBody Recipe recipe) {
        return recipeService.updateRecipe(id, recipe);
    }

    @DeleteMapping("delete/{id}")
    public void deleteRecipe (@PathVariable int id) {
        recipeService.deleteRecipe(id);
    }

}
