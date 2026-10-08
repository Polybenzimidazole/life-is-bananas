package be.lifeisbananas.acceptancetests.steps;

import be.lifeisbananas.acceptancetests.support.Testdata;
import be.lifeisbananas.acceptancetests.support.Testomgeving;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * De stappen van de acceptatietesten. De browser gaat open voor en weer dicht
 * na elk scenario, ook als een scenario faalt.
 */
public class ReceptStepDefinitions {

	private static final Duration TIMEOUT = Duration.ofSeconds(10);

	private WebDriver driver;

	@Before
	public void openBrowser() {
		ChromeOptions options = new ChromeOptions();
		if (Testomgeving.headless()) {
			options.addArguments("--headless=new");
		}
		// Vanaf Selenium 4.6 haalt Selenium Manager zelf de juiste chromedriver op.
		driver = new ChromeDriver(options);
	}

	@After
	public void closeBrowser() {
		if (driver != null) {
			driver.quit();
		}
	}

	// ----------------------------------------------------------------
	// Testgegevens
	// ----------------------------------------------------------------

	@Given("the test data has been initialized")
	public void theTestDataHasBeenInitialized() {
		Testdata.initialiseer();
	}

	// ----------------------------------------------------------------
	// Aanmelden
	// ----------------------------------------------------------------

	@Given("I log in as {string} with password {string}")
	public void iLogInAs(String email, String wachtwoord) {
		vulAanmeldformulierIn(email, wachtwoord);
		// Na een geslaagde aanmelding komen we op de lijst met recepturen uit.
		wacht().until(ExpectedConditions.visibilityOfElementLocated(By.id("zoek")));
	}

	@Given("I try to log in as {string} with password {string}")
	public void iTryToLogInAs(String email, String wachtwoord) {
		vulAanmeldformulierIn(email, wachtwoord);
	}

	private void vulAanmeldformulierIn(String email, String wachtwoord) {
		driver.get(Testomgeving.baseUrl() + "/login.html");
		WebElement emailveld = wacht().until(ExpectedConditions.visibilityOfElementLocated(By.id("email")));
		emailveld.clear();
		emailveld.sendKeys(email);
		driver.findElement(By.id("password")).clear();
		driver.findElement(By.id("password")).sendKeys(wachtwoord);
		driver.findElement(By.id("aanmelden")).click();
	}

	// ----------------------------------------------------------------
	// Zoeken en openen
	// ----------------------------------------------------------------

	@When("I search for recipes with {string}")
	public void iSearchForRecipesWith(String zoekterm) {
		WebElement zoekveld = wacht().until(ExpectedConditions.visibilityOfElementLocated(By.id("zoek")));
		zoekveld.clear();
		zoekveld.sendKeys(zoekterm);
		driver.findElement(By.id("zoeken")).click();
		wacht().until(ExpectedConditions.visibilityOfElementLocated(By.id("zoek")));
	}

	@Then("I should see the recipe {string} in the search results")
	public void iShouldSeeTheRecipeInTheSearchResults(String naam) {
		assertThat(gevondenRecepturen()).contains(naam);
	}

	@Then("I should see no search results")
	public void iShouldSeeNoSearchResults() {
		assertThat(driver.findElements(By.id("geenResultaten")))
				.as("de melding dat er niets gevonden is")
				.isNotEmpty();
		assertThat(gevondenRecepturen()).isEmpty();
	}

	private List<String> gevondenRecepturen() {
		return driver.findElements(By.cssSelector("#recepturen tbody tr td:first-child a"))
				.stream()
				.map(WebElement::getText)
				.toList();
	}

	@When("I open the recipe {string}")
	public void iOpenTheRecipe(String naam) {
		wacht().until(ExpectedConditions.elementToBeClickable(By.linkText(naam))).click();
		wacht().until(ExpectedConditions.textToBePresentInElementLocated(By.id("receptuurNaam"), naam));
	}

	// ----------------------------------------------------------------
	// Ingrediënt toevoegen en status wijzigen
	// ----------------------------------------------------------------

	@When("I add the ingredient {string} with quantity {string} and unit {string}")
	public void iAddTheIngredient(String grondstof, String hoeveelheid, String eenheid) {
		new Select(wacht().until(ExpectedConditions.visibilityOfElementLocated(By.id("rawMaterialId"))))
				.selectByVisibleText(grondstof);

		WebElement hoeveelheidsveld = driver.findElement(By.id("quantity"));
		hoeveelheidsveld.clear();
		hoeveelheidsveld.sendKeys(hoeveelheid);

		new Select(driver.findElement(By.id("unit"))).selectByVisibleText(eenheid);

		driver.findElement(By.id("voegIngredientToe")).click();
		wachtTotDeReceptpaginaOpnieuwGeladenIs();
	}

	@When("I change the status to {string}")
	public void iChangeTheStatusTo(String doelStatus) {
		new Select(wacht().until(ExpectedConditions.visibilityOfElementLocated(By.id("doelStatus"))))
				.selectByVisibleText(doelStatus);
		driver.findElement(By.id("wijzigStatus")).click();
		wachtTotDeReceptpaginaOpnieuwGeladenIs();
	}

	/** Na het versturen stuurt de toepassing terug naar de detailpagina. */
	private void wachtTotDeReceptpaginaOpnieuwGeladenIs() {
		wacht().until(ExpectedConditions.visibilityOfElementLocated(By.id("status")));
	}

	// ----------------------------------------------------------------
	// Controles
	// ----------------------------------------------------------------

	@Then("the recipe should have {int} ingredients")
	public void theRecipeShouldHaveIngredients(int verwachtAantal) {
		String getoondAantal = wacht()
				.until(ExpectedConditions.visibilityOfElementLocated(By.id("aantalIngredienten")))
				.getText();
		assertThat(getoondAantal).isEqualTo(String.valueOf(verwachtAantal));
		assertThat(driver.findElements(By.cssSelector("#ingredienten tbody tr"))).hasSize(verwachtAantal);
	}

	@Then("the recipe should contain the ingredient {string} with quantity {string} and unit {string}")
	public void theRecipeShouldContainTheIngredient(String grondstof, String hoeveelheid, String eenheid) {
		List<WebElement> rijen = driver.findElements(By.cssSelector("#ingredienten tbody tr"));

		boolean gevonden = rijen.stream().anyMatch(rij ->
				rij.findElement(By.cssSelector("td.grondstof")).getText().equals(grondstof)
						&& rij.findElement(By.cssSelector("td.hoeveelheid")).getText().equals(hoeveelheid)
						&& rij.findElement(By.cssSelector("td.eenheid")).getText().equals(eenheid));

		assertThat(gevonden)
				.as("ingrediënt %s %s %s in de tabel", grondstof, hoeveelheid, eenheid)
				.isTrue();
	}

	@Then("the recipe status should be {string}")
	public void theRecipeStatusShouldBe(String verwachteStatus) {
		String status = wacht()
				.until(ExpectedConditions.visibilityOfElementLocated(By.id("status")))
				.getText();
		assertThat(status).isEqualTo(verwachteStatus);
	}

	@Then("I should see the error {string}")
	public void iShouldSeeTheError(String verwachteFout) {
		String fout = wacht()
				.until(ExpectedConditions.visibilityOfElementLocated(By.id("fout")))
				.getText();
		assertThat(fout).contains(verwachteFout);
	}

	private WebDriverWait wacht() {
		return new WebDriverWait(driver, TIMEOUT);
	}
}
