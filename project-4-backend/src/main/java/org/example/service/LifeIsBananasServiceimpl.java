package org.example.service;

import jakarta.transaction.Transactional;
import org.example.dao.RawMaterialRepository;
import org.example.dao.RecipeRepository;
import org.example.dao.StockRepository;
import org.example.domain.Stock;
import org.example.domain.recipe;
import org.example.formdata.AddStockDto;
import org.example.formdata.RecipeDto;
import org.example.formdata.StockDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.example.domain.*;
import org.example.formdata.*;


import java.util.ArrayList;
import java.util.List;

@Service
public class LifeIsBananasServiceimpl implements LifeIsBananasService {

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private RawMaterialRepository rawMaterialRepository;

    public recipe GetRecipe(int id){
        return recipeRepository.findById(id).orElse(null);
    };

    @Override
    public recipe UpdateRecipe(int id, RecipeData recipeData) {

        recipe Recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Recipe not found"));

        Recipe.setTitle(recipeData.getTitle());

        return recipeRepository.save(Recipe);
    }
    public void deleteRecipe(int id){
         recipeRepository.deleteById(id);
    };
    public List<recipe> GetAllRecipe() {
        List<recipe> recipeList = new ArrayList<>();
        recipeRepository.findAll().forEach(recipeList::add);
        return recipeList;
    }
    @Override
    @Transactional
    public void useStock(ProductionData productionData) {

        recipe Recipe = recipeRepository
                .findById(productionData.getRecipeId())
                .orElseThrow(() -> new RuntimeException("Recipe not found"));

        int quantity = productionData.getQuantity();

        if (quantity <= 0) {
            throw new IllegalArgumentException("Production quantity must be positive");
        }

        List<Stock> stocksToUpdate = new ArrayList<>();
        List<Double> quantitiesToUse = new ArrayList<>();

        for (Recipeingredients ingredient : Recipe.getRecipeingredients()) {

            double requiredQuantity = ingredient.getQuantity() * quantity;

            Stock stock = stockRepository
                    .findByRawMaterial_Id(ingredient.getRawMaterial().getId())
                    .orElseThrow(() -> new RuntimeException(
                            "Stock not found for raw material"
                    ));

            double availableQuantity = stock.getQuantity();

            if (availableQuantity < requiredQuantity) {
                throw new IllegalArgumentException(
                        "Not enough stock for " + ingredient.getRawMaterial().getName()
                );
            }

            stocksToUpdate.add(stock);
            quantitiesToUse.add(requiredQuantity);
        }

        for (int i = 0; i < stocksToUpdate.size(); i++) {

            Stock stock = stocksToUpdate.get(i);

            stock.setQuantity(
                    stock.getQuantity() - quantitiesToUse.get(i)
            );

            stockRepository.save(stock);
        }
    }
    @Override
    @Transactional
    public void addRecipe(RecipeData recipeData) {

        recipe Recipe = new recipe();
        Recipe.setTitle(recipeData.getTitle());

        for (RecipeIngredientData data : recipeData.getIngredients()) {

            RawMaterial material = rawMaterialRepository
                    .findById(data.getRawMaterialId())
                    .orElseThrow();

            Recipeingredients ingredient = new Recipeingredients();

            ingredient.setRecipe(Recipe);
            ingredient.setRawMaterial(material);
            ingredient.setQuantity(data.getQuantity());

            Recipe.getRecipeingredients().add(ingredient);
        }

        recipeRepository.save(Recipe);
    }


    public StockDto ToDto(Stock stock){
        return new StockDto(
                stock.getQuantity(),
                stock.getRawMaterial().getName(),
                stock.getId(),
                stock.getRawMaterial().getUnit()
        );
    }

    public List<StockDto> GetAllStock(){
        List<StockDto> stockList = new ArrayList<>();
        stockRepository.findAll().forEach(Stock -> {
            stockList.add(ToDto(Stock));
        });
        return stockList;
    }
    public AddStockDto QuantitytoStockDto(Stock stock){
        return new AddStockDto(
                stock.getQuantity()
        );

    }



    public StockDto addStock(int id, double amount){
        Stock stock = stockRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Stock not found"));

        stock.setQuantity(stock.getQuantity() + amount);

        stockRepository.save(stock);

        return ToDto(stock);
    }



}

