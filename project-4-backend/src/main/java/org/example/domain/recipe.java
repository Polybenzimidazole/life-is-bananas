package org.example.domain;


import jakarta.persistence.*;

import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Recipes")
public class recipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String title;

    @OneToMany(
            mappedBy = "recipe",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Recipeingredients> recipeingredients = new ArrayList<>();





    public recipe(int id, String title, String ingredients) {
        this.id = id;
        this.title = title;
    }

    public recipe() {

    }

    public int getId() {return id;}
    public void setId(int id) {this.id = id;}
    public String getTitle() {return title;}
    public void setTitle(String name) {this.title = name;}
    public List<Recipeingredients> getRecipeingredients() {return recipeingredients;}
    public void setRecipeingredients(List<Recipeingredients> recipeingredients) {this.recipeingredients = recipeingredients;}

}
