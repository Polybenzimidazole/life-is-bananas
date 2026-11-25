package org.example;

import org.example.dao.RecipeRepository;
import org.example.domain.Recipe;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class LifeIsBananasApplication {

    public static void main(String[] args) {
        SpringApplication.run(LifeIsBananasApplication.class, args);
    }

    @Bean
    CommandLineRunner init(RecipeRepository recipeRepository) {
        return (evt) -> {
            recipeRepository.save(new Recipe("Spaghetti Bolognese", "Klassieke Italiaanse pasta met gehakt en tomatensaus."));
            recipeRepository.save(new Recipe("Bananenpannenkoeken", "Luchtige pannenkoeken van rijpe bananen en eieren."));
            recipeRepository.save(new Recipe("Caesarsalade", "Frisse salade met gegrilde kip, croutons en parmezaankaas."));
            recipeRepository.save(new Recipe("Groentecurry", "Kruidige kokoscurry met zoete aardappel en kikkererwten."));
        };
    }
}