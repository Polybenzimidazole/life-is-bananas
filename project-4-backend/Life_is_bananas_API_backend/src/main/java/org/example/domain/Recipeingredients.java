package org.example.domain;


import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "Portions")
public class Recipeingredients {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JsonIgnore
    private recipe recipe;

    @ManyToOne
    private RawMaterial rawMaterial;

    private double Quantity;

    public Recipeingredients() {

    }


    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public recipe getRecipe() {
        return recipe;
    }
    public void setRecipe(recipe recipe) {
        this.recipe = recipe;
    }
    public RawMaterial getRawMaterial() {
        return rawMaterial;
    }
    public void setRawMaterial(RawMaterial rawMaterial) {
        this.rawMaterial = rawMaterial;
    }
    public double getQuantity() {
        return Quantity;
    }
    public void setQuantity(double quantity) {
        Quantity = quantity;
    }
}
