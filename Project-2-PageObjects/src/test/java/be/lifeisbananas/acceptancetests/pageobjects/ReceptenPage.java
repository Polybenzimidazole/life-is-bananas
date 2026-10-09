package be.lifeisbananas.acceptancetests.pageobjects;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import java.util.List;

/** Recepturen zoeken en er één openen. Alle HTML-locators staan hier. */
public class ReceptenPage extends AbstractPage {
    public ReceptenPage(WebDriver driver) { super(driver); }

    public ReceptenPage search(String name) {
        WebElement input = waitFor().until(ExpectedConditions.visibilityOfElementLocated(By.id("zoek")));
        input.clear();
        input.sendKeys(name);
        driver.findElement(By.id("zoeken")).click();
        waitUntilGone(input);
        waitFor().until(ExpectedConditions.visibilityOfElementLocated(By.id("zoek")));
        return this;
    }

    public List<String> recipeNames() {
        return driver.findElements(By.cssSelector("#recepturen tbody tr td:first-child a"))
                .stream().map(WebElement::getText).toList();
    }

    public boolean showsNoResults() {
        return !driver.findElements(By.id("geenResultaten")).isEmpty();
    }

    public ReceptPage openRecipe(String name) {
        waitFor().until(ExpectedConditions.elementToBeClickable(By.linkText(name))).click();
        waitFor().until(ExpectedConditions.textToBePresentInElementLocated(By.id("receptuurNaam"), name));
        return new ReceptPage(driver);
    }
}
