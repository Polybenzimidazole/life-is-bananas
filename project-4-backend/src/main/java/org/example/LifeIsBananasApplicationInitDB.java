package org.example;

import org.example.dao.RecipeRepository;
import org.example.dao.StockRepository;
import org.example.dao.UserRepository;

import org.example.domain.RawMaterial;
import org.example.domain.Recipeingredients;
import org.example.domain.Stock;
import org.example.domain.User;
import org.example.domain.recipe;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@ConditionalOnProperty(name = "app.db-init", havingValue = "true")
public class LifeIsBananasApplicationInitDB implements CommandLineRunner {

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {

        // 0. USERS

        createUser("admin", "Admin123!", "ROLE_ADMIN",
                "Admin", "Life Is Bananas", "admin@example.com");

        createUser("engineer1", "Engineer123!", "ROLE_USER",
                "Thomas", "Peeters", "thomas@example.com");

        createUser("engineer2", "Engineer123!", "ROLE_USER",
                "Sarah", "Janssens", "sarah@example.com");

        createUser("production1", "Production123!", "ROLE_USER",
                "Lucas", "Vermeulen", "lucas@example.com");

        createUser("manager", "Manager123!", "ROLE_ADMIN",
                "Emma", "Dubois", "emma@example.com");

        // Voorkom dubbele recepten en stock
        if (recipeRepository.count() > 0 || stockRepository.count() > 0) {
            return;
        }

        // 1. RAW MATERIALS

        RawMaterial bananas = createRawMaterial("Bananas", "KG");
        RawMaterial flour = createRawMaterial("Flour", "KG");
        RawMaterial eggs = createRawMaterial("Eggs", "PCS");
        RawMaterial sugar = createRawMaterial("Sugar", "KG");
        RawMaterial butter = createRawMaterial("Butter", "KG");
        RawMaterial milk = createRawMaterial("Milk", "L");
        RawMaterial chocolate = createRawMaterial("Chocolate", "KG");
        RawMaterial honey = createRawMaterial("Honey", "KG");
        RawMaterial yogurt = createRawMaterial("Yogurt", "L");
        RawMaterial vanilla = createRawMaterial("Vanilla", "L");

        // 2. STOCK

        createStock(bananas, 200);
        createStock(flour, 100);
        createStock(eggs, 500);
        createStock(sugar, 80);
        createStock(butter, 40);
        createStock(milk, 150);
        createStock(chocolate, 35);
        createStock(honey, 20);
        createStock(yogurt, 60);
        createStock(vanilla, 10);

        // 3. RECIPES + RECIPE INGREDIENTS

        recipe bananaBread = createRecipe("Banana Bread");

        addIngredient(bananaBread, bananas, 0.3);
        addIngredient(bananaBread, flour, 0.2);
        addIngredient(bananaBread, eggs, 2);
        addIngredient(bananaBread, sugar, 0.1);
        addIngredient(bananaBread, butter, 0.05);

        recipeRepository.save(bananaBread);

        recipe bananaPancakes = createRecipe("Banana Pancakes");

        addIngredient(bananaPancakes, bananas, 0.2);
        addIngredient(bananaPancakes, eggs, 2);
        addIngredient(bananaPancakes, milk, 0.25);
        addIngredient(bananaPancakes, flour, 0.15);

        recipeRepository.save(bananaPancakes);

        recipe bananaSmoothie = createRecipe("Banana Smoothie");

        addIngredient(bananaSmoothie, bananas, 0.2);
        addIngredient(bananaSmoothie, milk, 0.3);
        addIngredient(bananaSmoothie, honey, 0.02);
        addIngredient(bananaSmoothie, yogurt, 0.1);

        recipeRepository.save(bananaSmoothie);

        recipe chocolateBananaCake = createRecipe("Chocolate Banana Cake");

        addIngredient(chocolateBananaCake, bananas, 0.3);
        addIngredient(chocolateBananaCake, chocolate, 0.15);
        addIngredient(chocolateBananaCake, flour, 0.25);
        addIngredient(chocolateBananaCake, eggs, 3);
        addIngredient(chocolateBananaCake, sugar, 0.1);

        recipeRepository.save(chocolateBananaCake);

        recipe bananaIceCream = createRecipe("Banana Ice Cream");

        addIngredient(bananaIceCream, bananas, 0.4);
        addIngredient(bananaIceCream, milk, 0.2);
        addIngredient(bananaIceCream, vanilla, 0.01);

        recipeRepository.save(bananaIceCream);
    }

    // HELPER METHOD: USER

    private void createUser(
            String username,
            String password,
            String role,
            String firstName,
            String lastName,
            String email
    ) {

        if (userRepository.findByUsername(username) != null) {
            return;
        }

        User user = new User(
                username,
                passwordEncoder.encode(password),
                role,
                firstName,
                lastName,
                email
        );

        userRepository.save(user);
    }

    // HELPER METHOD: RAW MATERIAL

    private RawMaterial createRawMaterial(String name, String unit) {

        RawMaterial material = new RawMaterial();

        material.setName(name);
        material.setUnit(unit);

        return material;
    }

    // HELPER METHOD: STOCK

    private void createStock(RawMaterial material, double quantity) {

        Stock stock = new Stock();

        stock.setRawMaterial(material);
        stock.setQuantity(quantity);

        stockRepository.save(stock);
    }

    // HELPER METHOD: RECIPE

    private recipe createRecipe(String title) {

        recipe newRecipe = new recipe();

        newRecipe.setTitle(title);

        return newRecipe;
    }

    // HELPER METHOD: RECIPE INGREDIENT

    private void addIngredient(
            recipe recipe,
            RawMaterial rawMaterial,
            double quantity
    ) {

        Recipeingredients ingredient = new Recipeingredients();

        ingredient.setRecipe(recipe);
        ingredient.setRawMaterial(rawMaterial);
        ingredient.setQuantity(quantity);

        recipe.getRecipeingredients().add(ingredient);
    }
}