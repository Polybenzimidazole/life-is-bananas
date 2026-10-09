package be.lifeisbananas.acceptancetests.support;

import java.util.List;
import java.util.logging.Filter;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * Dempt de CDP-waarschuwingen die Selenium bij elke browserstart logt.
 * <p>
 * Selenium levert het Chrome DevTools Protocol enkel mee voor de Chrome-versies
 * die bestonden toen Selenium uitkwam: versie {@code 4.26.0} kent 128, 129 en
 * 130. Draait de test op een nieuwere Chrome, dan vindt Selenium geen passende
 * CDP-versie en waarschuwt het daarover, twee keer per browserstart. Die
 * waarschuwing is hier zonder gevolg: de acceptatietest klikt enkel door de
 * pagina's heen en gebruikt het DevTools Protocol nergens. Gewone
 * WebDriver-opdrachten gaan langs chromedriver, niet langs CDP. Een nieuwere
 * Selenium helpt niet: die loopt altijd achter op de Chrome-versies.
 * <p>
 * Enkel die ene waarschuwing wordt tegengehouden; iedere andere boodschap van
 * dezelfde loggers komt er nog gewoon door.
 */
public final class Browserlogboek {

	/** De twee loggers die over een ontbrekende CDP-versie klagen. */
	private static final List<Logger> LOGGERS = Stream.of(
					"org.openqa.selenium.devtools.CdpVersionFinder",
					"org.openqa.selenium.chromium.ChromiumDriver")
			.map(Logger::getLogger)
			.toList();

	/**
	 * Laat alles door behalve wat over CDP gaat.
	 */
	private static final Filter ZONDER_CDP = logregel -> {
		String bericht = logregel.getMessage();
		return bericht == null || !bericht.contains("CDP");
	};

	private Browserlogboek() {
	}

	/**
	 * Mag meermaals aangeroepen worden: de filter wordt telkens opnieuw gezet.
	 * <p>
	 * De lijst hierboven houdt de loggers met een harde verwijzing vast, en dat
	 * is geen overbodige omslachtigheid: de {@code LogManager} bewaart loggers
	 * enkel met een zwakke verwijzing. Zonder die lijst wordt een logger die
	 * hier net een filter kreeg weer opgeruimd zolang Selenium hem zelf nog niet
	 * vasthoudt, en logt Selenium zijn eerste waarschuwing alsnog.
	 */
	public static void dempCdpWaarschuwingen() {
		LOGGERS.forEach(logger -> logger.setFilter(ZONDER_CDP));
	}
}
