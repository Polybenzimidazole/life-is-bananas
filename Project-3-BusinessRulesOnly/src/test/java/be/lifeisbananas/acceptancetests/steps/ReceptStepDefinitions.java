package be.lifeisbananas.acceptancetests.steps;

import be.lifeisbananas.acceptancetests.business.ReceptTaken;
import be.lifeisbananas.acceptancetests.pageobjects.ReceptPage;
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
    private WebDriver driver;
    private ReceptTaken recipes;

    @Before
    public void openBrowser() {
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

    @When("the bio-engineer completes {string} for testing with {string} {string} of {string}")
    public void completeRecipe(String name, String amount, String unit, String material) {
        recipes.completeRecipeForTesting(name, material, amount, unit);
    }

    @Then("{string} is tested and has {int} ingredients")
    public void testedRecipe(String name, int count) {
        assertThat(recipes.currentRecipeName()).isEqualTo(name);
        assertThat(recipes.currentRecipeStatus()).isEqualTo("TESTED");
        assertThat(recipes.currentIngredientCount()).isEqualTo(count);
        assertThat(recipes.currentIngredients()).hasSize(count);
    }

    @Then("it contains {string} {string} of {string}")
    public void ingredientIsPresent(String amount, String unit, String material) {
        assertThat(recipes.currentIngredients())
                .contains(new ReceptPage.IngredientRow(material, amount, unit));
    }

    @When("the bio-engineer tries to approve {string} without testing")
    public void attemptApproval(String name) {
        recipes.attemptApprovalWithoutTesting(name);
    }

    @Then("{string} remains in development")
    public void recipeRemainsInDevelopment(String name) {
        assertThat(recipes.currentRecipeName()).isEqualTo(name);
        assertThat(recipes.currentRecipeStatus()).isEqualTo("IN_DEVELOPMENT");
    }

    @Then("the system refuses to skip the testing phase")
    public void cannotSkipTesting() {
        assertThat(recipes.recipeErrorMessage())
                .contains("Overgang van IN_DEVELOPMENT naar APPROVED is niet toegelaten");
    }

    @When("the bio-engineer searches for recipes containing {string}")
    public void searchUnknown(String term) {
        recipes.searchRecipes(term);
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
