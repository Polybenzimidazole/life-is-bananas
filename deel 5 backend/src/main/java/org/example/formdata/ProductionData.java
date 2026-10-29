package org.example.formdata;

public class ProductionData {
    private int quantity;
    private int recipeId;




    public ProductionData(int quantity, int recipeId) {
        this.quantity = quantity;
    }
    public ProductionData() {}
    public int getQuantity() {
        return quantity;
    }
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getRecipeId() {
        return recipeId;
    }
    public void setRecipeId(int recipeId) {
        this.recipeId = recipeId;
    }
}
