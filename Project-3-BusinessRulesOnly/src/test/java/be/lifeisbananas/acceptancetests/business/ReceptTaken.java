package be.lifeisbananas.acceptancetests.business;

import be.lifeisbananas.acceptancetests.pageobjects.LoginPage;
import be.lifeisbananas.acceptancetests.pageobjects.ReceptPage;
import be.lifeisbananas.acceptancetests.pageobjects.ReceptenPage;
import be.lifeisbananas.acceptancetests.support.Testdata;
import org.openqa.selenium.WebDriver;

import java.util.List;

/**
 * Business flow layer: hoe een gebruiker door de toepassing gaat.
 * Alle browserdetails zijn afgeschermd in de Page Objects.
 * Hier staan geen assertions; die blijven bij de Cucumber-stappen.
 */
public class ReceptTaken {
    private final WebDriver driver;
    private LoginPage loginPage;
    private ReceptenPage receptenPage;
    private ReceptPage receptPage;

    public ReceptTaken(WebDriver driver) {
        this.driver = driver;
    }

    public void engineerSignsIn() {
        loginPage = new LoginPage(driver).open();
        receptenPage = loginPage.signIn(Testdata.EMAIL, Testdata.PASSWORD);
    }

    private void selectRecipe(String name) {
        receptenPage = receptenPage.search(name);
        receptPage = receptenPage.openRecipe(name);
    }

    public void completeRecipeForTesting(String recipeName, String ingredient, String amount, String unit) {
        selectRecipe(recipeName);
        receptPage.addIngredient(ingredient, amount, unit).changeStatus("TESTED");
    }

    public void attemptApprovalWithoutTesting(String recipeName) {
        selectRecipe(recipeName);
        receptPage.changeStatus("APPROVED");
    }

    public void searchRecipes(String text) {
        receptenPage = receptenPage.search(text);
    }

    public void attemptInvalidSignIn() {
        loginPage = new LoginPage(driver).open().trySignIn(Testdata.EMAIL, "verkeerd");
    }

    /** Eerst twee geldige overgangen door de echte UI: IN_DEVELOPMENT -> TESTED -> APPROVED. */
    public void approveTestedRecipe(String recipeName) {
        selectRecipe(recipeName);
        receptPage.changeStatus("TESTED").changeStatus("APPROVED");
    }

    public void attemptToModifyApprovedRecipe(String ingredient, String amount, String unit) {
        receptPage.addIngredient(ingredient, amount, unit);
    }

    public String currentRecipeName() { return receptPage.name(); }
    public String currentRecipeStatus() { return receptPage.status(); }
    public int currentIngredientCount() { return receptPage.ingredientCount(); }
    public List<ReceptPage.IngredientRow> currentIngredients() { return receptPage.ingredientRows(); }
    public String recipeErrorMessage() { return receptPage.errorMessage(); }
    public List<String> matchingRecipes() { return receptenPage.recipeNames(); }
    public boolean noRecipesFoundMessageVisible() { return receptenPage.showsNoResults(); }
    public String loginErrorMessage() { return loginPage.errorMessage(); }
}
