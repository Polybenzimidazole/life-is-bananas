package be.lifeisbananas.acceptancetests.support;

/**
 * Waar de acceptatietest de toepassing en de databank vindt.
 * <p>
 * De standaardwaarden zijn die van de toepassing zoals ze op MySQL draait.
 * Maven geeft ze mee via het profiel {@code acceptance}; vanuit de IDE
 * gelden gewoon de standaardwaarden hieronder.
 */
public final class Testomgeving {

	private Testomgeving() {
	}

	/** Het adres waarop de toepassing draait. */
	public static String baseUrl() {
		return eigenschap("lib.base.url", "http://localhost:8080");
	}

	/** Dezelfde databank als die van de draaiende toepassing. */
	public static String dbUrl() {
		return eigenschap("lib.db.url", "jdbc:mysql://localhost:3306/life_is_bananas");
	}

	public static String dbUser() {
		return eigenschap("lib.db.user", "lifeisbananasuser");
	}

	/**
	 * Een leeg wachtwoord is een geldige keuze (H2 gebruikt er geen), dus hier
	 * telt alleen een ontbrekende eigenschap als "niet ingevuld".
	 */
	public static String dbPassword() {
		String waarde = System.getProperty("lib.db.password");
		if (waarde == null || waarde.startsWith("${")) {
			return "lifeisbananasuserpw";
		}
		return waarde;
	}

	/** Draait de browser onzichtbaar? Handig op een buildserver. */
	public static boolean headless() {
		return Boolean.parseBoolean(eigenschap("lib.headless", "false"));
	}

	/**
	 * Maven vult een niet-ingevulde eigenschap op als de letterlijke tekst
	 * {@code ${naam}}; die behandelen we als niet ingevuld.
	 */
	private static String eigenschap(String naam, String standaardwaarde) {
		String waarde = System.getProperty(naam);
		if (waarde == null || waarde.isBlank() || waarde.startsWith("${")) {
			return standaardwaarde;
		}
		return waarde.trim();
	}
}
