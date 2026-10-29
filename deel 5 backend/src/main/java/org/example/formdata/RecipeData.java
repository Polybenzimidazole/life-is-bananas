package org.example.formdata;

import java.util.List;
import jakarta.validation.constraints.NotBlank;

public class RecipeData {

    private int id = 0;

    @NotBlank(message = "recept moet een title hebben")
    private String title;

    private List<RecipeIngredientData> ingredients;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<RecipeIngredientData> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<RecipeIngredientData> ingredients) {
        this.ingredients = ingredients;
    }
}