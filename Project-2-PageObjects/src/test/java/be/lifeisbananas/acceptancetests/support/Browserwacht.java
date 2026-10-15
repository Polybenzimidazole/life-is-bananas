package be.lifeisbananas.acceptancetests.support;

import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.function.Function;

/**
 * Een {@link WebDriverWait} die rustig blijft wachten terwijl de pagina
 * herlaadt.
 * <p>
 * De toepassing stuurt na elk formulier een nieuwe pagina terug. Zoekt een
 * wachtvoorwaarde net op dat ogenblik een element op, dan vindt ze het nog op
 * de oude pagina, waarna dat element verdwijnt voor ze het kan uitlezen.
 * Selenium verwacht daarvoor een {@link StaleElementReferenceException} en
 * vangt die in al zijn voorwaarden op, maar chromedriver 154 meldt zo'n
 * losgekoppeld element soms als een algemene fout ("Node with given id does not
 * belong to the document"). Die glipte door de opvang en deed het scenario
 * struikelen, terwijl er niets aan de hand was.
 * <p>
 * Hier geldt die fout als "de pagina is nog niet klaar": de wachtlus probeert
 * gewoon opnieuw tot de nieuwe pagina er staat, of tot de tijd om is. Omdat
 * alle wachtvoorwaarden langs {@link #until} passeren, geldt dat voor elke
 * voorwaarde zonder dat de aanroepen zelf moeten veranderen.
 */
public class Browserwacht extends WebDriverWait {

	/** Hoe chromedriver een element meldt dat niet meer op de pagina staat. */
	private static final String LOSGEKOPPELD = "does not belong to the document";

	public Browserwacht(WebDriver driver, Duration tijdslimiet) {
		super(driver, tijdslimiet);
	}

	@Override
	public <V> V until(Function<? super WebDriver, V> voorwaarde) {
		return super.until(herlaadtNog(voorwaarde));
	}

	/**
	 * {@code null} betekent voor de wachtlus "nog niet klaar": ze probeert
	 * daarna gewoon opnieuw. Een fout die niets met herladen te maken heeft,
	 * gaat ongemoeid door.
	 */
	private static <V> Function<WebDriver, V> herlaadtNog(Function<? super WebDriver, V> voorwaarde) {
		return driver -> {
			try {
				return voorwaarde.apply(driver);
			} catch (StaleElementReferenceException herlaadt) {
				return null;
			} catch (WebDriverException fout) {
				String bericht = fout.getMessage();
				if (bericht != null && bericht.contains(LOSGEKOPPELD)) {
					return null;
				}
				throw fout;
			}
		};
	}
}
