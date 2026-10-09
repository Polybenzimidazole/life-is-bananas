package be.lifeisbananas.acceptancetests.pageobjects;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import java.util.List;

/** Receptdetails, ingrediënten en statusformulier. Geen assertions. */
public class ReceptPage extends AbstractPage {
    public ReceptPage(WebDriver driver) { super(driver); }

    public ReceptPage addIngredient(String material, String quantity, String unit) {
        WebElement oldStatus = waitFor().until(ExpectedConditions.visibilityOfElementLocated(By.id("status")));
        new Select(driver.findElement(By.id("rawMaterialId"))).selectByVisibleText(material);
        WebElement quantityField = driver.findElement(By.id("quantity"));
        quantityField.clear();
        quantityField.sendKeys(quantity);
        new Select(driver.findElement(By.id("unit"))).selectByVisibleText(unit);
        driver.findElement(By.id("voegIngredientToe")).click();
        waitUntilReloaded(oldStatus);
        return this;
    }

    public ReceptPage changeStatus(String targetStatus) {
        WebElement oldStatus = waitFor().until(ExpectedConditions.visibilityOfElementLocated(By.id("status")));
        new Select(driver.findElement(By.id("doelStatus"))).selectByVisibleText(targetStatus);
        driver.findElement(By.id("wijzigStatus")).click();
        waitUntilReloaded(oldStatus);
        return this;
    }

    private void waitUntilReloaded(WebElement oldStatus) {
        waitFor().until(ExpectedConditions.stalenessOf(oldStatus));
        waitFor().until(ExpectedConditions.visibilityOfElementLocated(By.id("status")));
    }

    public String name() {
        return waitFor().until(ExpectedConditions.visibilityOfElementLocated(By.id("receptuurNaam"))).getText();
    }

    public String status() {
        return waitFor().until(ExpectedConditions.visibilityOfElementLocated(By.id("status"))).getText();
    }

    public int ingredientCount() {
        String count = waitFor().until(ExpectedConditions.visibilityOfElementLocated(By.id("aantalIngredienten"))).getText();
        return Integer.parseInt(count);
    }

    public List<IngredientRow> ingredientRows() {
        return driver.findElements(By.cssSelector("#ingredienten tbody tr"))
                .stream().map(row -> new IngredientRow(
                        row.findElement(By.cssSelector("td.grondstof")).getText(),
                        row.findElement(By.cssSelector("td.hoeveelheid")).getText(),
                        row.findElement(By.cssSelector("td.eenheid")).getText()))
                .toList();
    }

    public String errorMessage() {
        return waitFor().until(ExpectedConditions.visibilityOfElementLocated(By.id("fout"))).getText();
    }

    public ReceptenPage backToRecipes() {
        driver.findElement(By.id("terugNaarRecepturen")).click();
        waitFor().until(ExpectedConditions.visibilityOfElementLocated(By.id("zoek")));
        return new ReceptenPage(driver);
    }

    public record IngredientRow(String material, String quantity, String unit) { }
}
