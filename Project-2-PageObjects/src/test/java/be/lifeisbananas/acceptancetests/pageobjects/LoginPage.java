package be.lifeisbananas.acceptancetests.pageobjects;

import be.lifeisbananas.acceptancetests.support.Testomgeving;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

/** Het scherm waarop de bio-ingenieur zich aanmeldt. */
public class LoginPage extends AbstractPage {
    public LoginPage(WebDriver driver) { super(driver); }

    public LoginPage open() {
        driver.get(Testomgeving.baseUrl() + "/login.html");
        waitFor().until(ExpectedConditions.visibilityOfElementLocated(By.id("email")));
        return this;
    }

    private void submitCredentials(String email, String password) {
        WebElement emailField = waitFor().until(ExpectedConditions.visibilityOfElementLocated(By.id("email")));
        emailField.clear();
        emailField.sendKeys(email);
        WebElement passwordField = driver.findElement(By.id("password"));
        passwordField.clear();
        passwordField.sendKeys(password);
        driver.findElement(By.id("aanmelden")).click();
    }

    public ReceptenPage signIn(String email, String password) {
        submitCredentials(email, password);
        waitFor().until(ExpectedConditions.visibilityOfElementLocated(By.id("zoek")));
        return new ReceptenPage(driver);
    }

    public LoginPage trySignIn(String email, String password) {
        submitCredentials(email, password);
        waitFor().until(ExpectedConditions.visibilityOfElementLocated(By.id("fout")));
        return this;
    }

    public String errorMessage() {
        return waitFor().until(ExpectedConditions.visibilityOfElementLocated(By.id("fout"))).getText();
    }
}
