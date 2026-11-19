package be.lifeisbananas.acceptancetests.support;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Waar de acceptatietest de toepassing en de databank vindt.
 * <p>
 * Zonder VM-opties gebruiken IntelliJ-tests de lokale XAMPP-instellingen.
 * Met {@code -Dlib.environment=docker} kies je de aparte Docker-testomgeving.
 * Afzonderlijke {@code -Dlib.*}-eigenschappen hebben steeds voorrang op het
 * bijbehorende {@code acceptance-<omgeving>.properties}-bestand.
 */
public final class Testomgeving {

	private static final Properties INSTELLINGEN = laadInstellingen();

	private Testomgeving() {
	}

	/** Het adres waarop de toepassing draait. */
	public static String baseUrl() {
		return eigenschap("lib.base.url", "http://localhost:8091");
	}

	/** Dezelfde databank als die van de draaiende toepassing. */
	public static String dbUrl() {
		return eigenschap("lib.db.url", "jdbc:mysql://127.0.0.1:3306/life_is_bananas_acceptance");
	}

	public static String dbUser() {
		return eigenschap("lib.db.user", "root");
	}

	/**
	 * Een leeg wachtwoord is een geldige keuze (H2 gebruikt er geen), dus hier
	 * telt alleen een ontbrekende eigenschap als "niet ingevuld".
	 */
	public static String dbPassword() {
		String waarde = System.getProperty("lib.db.password");
		if (ontbreekt(waarde)) {
			return INSTELLINGEN.getProperty("lib.db.password", "");
		}
		return waarde;
	}

	/** Draait de browser onzichtbaar? Handig op een buildserver. */
	public static boolean headless() {
		return Boolean.parseBoolean(eigenschap("lib.headless", "false"));
	}

	private static Properties laadInstellingen() {
		Properties instellingen = new Properties();
		String omgeving = System.getProperty("lib.environment");
		if (ontbreekt(omgeving) || omgeving.isBlank()) {
			omgeving = "xampp";
		}
		omgeving = omgeving.trim();
		if (!"docker".equals(omgeving) && !"xampp".equals(omgeving)) {
			throw new IllegalArgumentException("Onbekende lib.environment: " + omgeving
					+ ". Gebruik docker of xampp; zonder deze eigenschap wordt XAMPP gebruikt.");
		}
		String bestand = "/acceptance-" + omgeving + ".properties";
		try (InputStream invoer = Testomgeving.class.getResourceAsStream(bestand)) {
			if (invoer == null) {
				throw new IllegalStateException("Testinstellingen ontbreken op het classpath: " + bestand);
			}
			instellingen.load(invoer);
		}
		catch (IOException e) {
			throw new IllegalStateException("Kon testinstellingen niet lezen: " + bestand, e);
		}
		return instellingen;
	}

	/**
	 * Maven vult een niet-ingevulde eigenschap op als de letterlijke tekst
	 * {@code ${naam}}; die behandelen we als niet ingevuld.
	 */
	private static String eigenschap(String naam, String standaardwaarde) {
		String waarde = System.getProperty(naam);
		if (ontbreekt(waarde) || waarde.isBlank()) {
			waarde = INSTELLINGEN.getProperty(naam, standaardwaarde);
		}
		return waarde.trim();
	}

	private static boolean ontbreekt(String waarde) {
		return waarde == null || waarde.startsWith("${");
	}
}
