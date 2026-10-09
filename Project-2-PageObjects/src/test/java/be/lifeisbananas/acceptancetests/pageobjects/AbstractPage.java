package be.lifeisbananas.acceptancetests.pageobjects;

import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
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

    /**
     * Wacht tot een element van de vorige pagina niet meer bestaat: zo weet je
     * dat het herladen achter de rug is en je niet nog de oude pagina leest.
     * <p>
     * Dit vervangt {@code ExpectedConditions.stalenessOf}. Die vangt enkel een
     * {@link StaleElementReferenceException} op, terwijl chromedriver 154 een
     * losgekoppeld element soms meldt als een algemene fout ("Node with given
     * id does not belong to the document"). Die glipte er dan door en deed het
     * scenario struikelen, maar enkel als het herladen net op dat ogenblik
     * bezig was: vandaar dat het de ene keer werkte en de andere keer niet.
     */
    protected void waitUntilGone(WebElement element) {
        waitFor().until(isGone(element));
    }

    private static ExpectedCondition<Boolean> isGone(WebElement element) {
        return driver -> {
            try {
                // Eender welke opdracht dwingt de controle af.
                element.isEnabled();
                return false;
            } catch (StaleElementReferenceException weg) {
                return true;
            } catch (WebDriverException fout) {
                String bericht = fout.getMessage();
                return bericht != null && bericht.contains("does not belong to the document");
            }
        };
    }
}
