package org.example.service;

import org.example.domain.*;
import org.example.formdata.*;

import java.util.List;

public interface LifeIsBananasService {

    public recipe GetRecipe(int id);

    recipe UpdateRecipe(int id, RecipeData recipeData);

    public void deleteRecipe(int id);

    public List<recipe> GetAllRecipe();

    void addRecipe(RecipeData recipeData);

    public List<StockDto> GetAllStock();

    public StockDto ToDto(Stock stock);

    public StockDto addStock(int id, double amount);

    public AddStockDto QuantitytoStockDto(Stock stock);

    void useStock(ProductionData productionData);
}
