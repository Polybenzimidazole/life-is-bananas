package org.example.formdata;

public class RecipeIngredientData {

    private int rawMaterialId;
    private double quantity;

    // Getters en setters
    public RecipeIngredientData(int rawMaterialId, double quantity) {
        this.rawMaterialId = rawMaterialId;
        this.quantity = quantity;
    }
    public RecipeIngredientData() {}

    public int getRawMaterialId() {
        return rawMaterialId;
    }
    public void setRawMaterialId(int rawMaterialId) {
        this.rawMaterialId = rawMaterialId;
    }
    public double getQuantity() {
        return quantity;

    }
    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }
}