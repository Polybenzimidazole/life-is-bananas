package be.lifeisbananas.acceptancetests.pageobjects;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

/** Gedeelde browsertechniek. Geen assertions of businessregels. */
public abstract class AbstractPage {
    protected final WebDriver driver;
    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    protected AbstractPage(WebDriver driver) {
        this.driver = driver;
    }

    protected WebDriverWait waitFor() {
        return new WebDriverWait(driver, TIMEOUT);
    }
}
