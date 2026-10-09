package be.lifeisbananas.acceptancetests.steps;

import be.lifeisbananas.acceptancetests.pageobjects.LoginPage;
import be.lifeisbananas.acceptancetests.pageobjects.ReceptenPage;
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

/** Stap 2: Selenium-locators staan UITSLUITEND in de Page Objects. */
public class ReceptStepDefinitions {
    private WebDriver driver;
    private LoginPage loginPage;
    private ReceptenPage receptenPage;
    private ReceptPage receptPage;

    @Before
    public void openBrowser() {
        Browserlogboek.dempCdpWaarschuwingen();
        ChromeOptions options = new ChromeOptions();
        if (Testomgeving.headless()) options.addArguments("--headless=new");
        driver = new ChromeDriver(options);
        loginPage = new LoginPage(driver);
    }

    @After
    public void closeBrowser() {
        if (driver != null) driver.quit();
    }

    @Given("the test data has been initialized")
    public void initialiseerTestdata() {
        Testdata.initialiseer();
    }

    @Given("I log in as {string} with password {string}")
    public void signIn(String email, String password) {
        receptenPage = loginPage.open().signIn(email, password);
    }

    @Given("I try to log in as {string} with password {string}")
    public void trySignIn(String email, String password) {
        loginPage = loginPage.open().trySignIn(email, password);
    }

    @When("I search for recipes with {string}")
    public void searchForRecipes(String term) {
        receptenPage = receptenPage.search(term);
    }

    @Then("I should see the recipe {string} in the search results")
    public void recipeInSearchResults(String recipeName) {
        assertThat(receptenPage.recipeNames()).contains(recipeName);
    }

    @Then("I should see no search results")
    public void noSearchResults() {
        assertThat(receptenPage.showsNoResults()).isTrue();
        assertThat(receptenPage.recipeNames()).isEmpty();
    }

    @When("I open the recipe {string}")
    public void openRecipe(String name) {
        receptPage = receptenPage.openRecipe(name);
    }

    @When("I add the ingredient {string} with quantity {string} and unit {string}")
    public void addIngredient(String material, String amount, String unit) {
        receptPage = receptPage.addIngredient(material, amount, unit);
    }

    @When("I change the status to {string}")
    public void changeStatus(String target) {
        receptPage = receptPage.changeStatus(target);
    }

    @Then("the recipe should have {int} ingredients")
    public void recipeHasIngredientCount(int count) {
        assertThat(receptPage.ingredientCount()).isEqualTo(count);
        assertThat(receptPage.ingredientRows()).hasSize(count);
    }

    @Then("the recipe should contain the ingredient {string} with quantity {string} and unit {string}")
    public void ingredientShown(String material, String amount, String unit) {
        assertThat(receptPage.ingredientRows())
                .contains(new ReceptPage.IngredientRow(material, amount, unit));
    }

    @Then("the recipe status should be {string}")
    public void recipeHasStatus(String status) {
        assertThat(receptPage.status()).isEqualTo(status);
    }

    @Then("I should see the error {string}")
    public void errorShown(String error) {
        String displayed = receptPage != null ? receptPage.errorMessage() : loginPage.errorMessage();
        assertThat(displayed).contains(error);
    }
}
