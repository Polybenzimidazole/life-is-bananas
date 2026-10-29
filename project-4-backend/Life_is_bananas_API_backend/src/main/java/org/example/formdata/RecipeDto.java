package org.example.formdata;

public class RecipeDto {
    private int id;
    private String title;
    private String ingredients;

    public RecipeDto(int id, String title, String ingredients) {
        this.id = id;
        this.title = title;
        this.ingredients = ingredients;
    }

    public int getId() {return id;}
    public void setId(int id) {this.id = id;}
    public String getTitle() {return title;}
    public void setTitle(String title) {this.title = title;}
    public String getIngredients() {return ingredients;}
    public void setIngredients(String ingredients) {this.ingredients = ingredients;}
}
