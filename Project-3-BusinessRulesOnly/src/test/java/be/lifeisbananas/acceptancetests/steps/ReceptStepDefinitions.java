package be.lifeisbananas.acceptancetests.steps;

import be.lifeisbananas.acceptancetests.business.ReceptTaken;
import be.lifeisbananas.acceptancetests.pageobjects.ReceptPage;
import be.lifeisbananas.acceptancetests.support.Browserlogboek;
import be.lifeisbananas.acceptancetests.support.Testdata;
import be.lifeisbananas.acceptancetests.support.Testomgeving;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import static org.assertj.core.api.Assertions.assertThat;

/** Stap 3: step-definities drukken bedrijfsregels uit, geen UI-locators. */
public class ReceptStepDefinitions {
    private static final String RECIPE_NAME = "Banana Chips Formula";
    private static final String INGREDIENT = "Banana";
    private static final String AMOUNT = "300";
    private static final String UNIT = "GRAM";
    private WebDriver driver;
    private ReceptTaken recipes;

    @Before
    public void openBrowser() {
        Browserlogboek.dempCdpWaarschuwingen();
        ChromeOptions options = new ChromeOptions();
        if (Testomgeving.headless()) options.addArguments("--headless=new");
        driver = new ChromeDriver(options);
        recipes = new ReceptTaken(driver);
    }

    @After
    public void closeBrowser() {
        if (driver != null) driver.quit();
    }

    @Given("the test data has been initialized")
    public void initData() {
        Testdata.initialiseer();
    }

    @Given("the bio-engineer is signed in")
    public void engineerSignedIn() {
        recipes.engineerSignsIn();
    }

    @When("the bio-engineer completes a recipe for testing by adding an ingredient")
    public void completeRecipe() {
        recipes.completeRecipeForTesting(RECIPE_NAME, INGREDIENT, AMOUNT, UNIT);
    }

    @Then("the recipe is tested")
    public void testedRecipe() {
        assertThat(recipes.currentRecipeName()).isEqualTo(RECIPE_NAME);
        assertThat(recipes.currentRecipeStatus()).isEqualTo("TESTED");
    }

    @Then("the added ingredient is included in the recipe")
    public void ingredientIsPresent() {
        assertThat(recipes.currentIngredients())
                .contains(new ReceptPage.IngredientRow(INGREDIENT, AMOUNT, UNIT));
    }

    @When("the bio-engineer tries to approve a recipe without testing")
    public void attemptApproval() {
        recipes.attemptApprovalWithoutTesting(RECIPE_NAME);
    }

    @Then("the recipe remains in development")
    public void recipeRemainsInDevelopment() {
        assertThat(recipes.currentRecipeName()).isEqualTo(RECIPE_NAME);
        assertThat(recipes.currentRecipeStatus()).isEqualTo("IN_DEVELOPMENT");
    }

    @Then("the system refuses to skip the testing phase")
    public void cannotSkipTesting() {
        assertThat(recipes.recipeErrorMessage())
                .contains("Overgang van IN_DEVELOPMENT naar APPROVED is niet toegelaten");
    }

    @When("the bio-engineer searches for a recipe that does not exist")
    public void searchUnknown() {
        recipes.searchRecipes("Chocolate");
    }

    @Then("no recipes are found")
    public void noRecipesFound() {
        assertThat(recipes.noRecipesFoundMessageVisible()).isTrue();
        assertThat(recipes.matchingRecipes()).isEmpty();
    }

    @When("someone attempts to sign in with an incorrect password")
    public void incorrectPassword() {
        recipes.attemptInvalidSignIn();
    }

    @Then("access to recipe management is refused")
    public void accessDenied() {
        assertThat(recipes.loginErrorMessage())
                .contains("Onbekend e-mailadres of verkeerd wachtwoord");
    }
}
