package be.lifeisbananas.acceptancetests.support;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Zet de databank voor elk scenario terug op een bekende begintoestand.
 * <p>
 * De test praat hier rechtstreeks met dezelfde databank als de draaiende
 * toepassing, en niet via de toepassing zelf. Zo staat de begintoestand vast,
 * ongeacht wat een vorig scenario of een handmatige klik achterliet, en
 * blijft het scenario zelf over de bedrijfslogica gaan.
 * <p>
 * De id's worden niet vastgelegd maar teruggelezen uit de databank. Dat werkt
 * zowel op MySQL als op H2, en voorkomt dat de teller van de identity-kolom
 * uit de pas loopt.
 */
public final class Testdata {

	/** De bio-ingenieur waarmee de scenario's aanmelden. */
	public static final String EMAIL = "sarah.johnson@lifeisbananas.com";
	public static final String PASSWORD = "banaan123";
	public static final String NAAM = "Dr. Sarah Johnson";

	/** De receptuur die de scenario's openen. */
	public static final String RECEPTUUR = "Banana Chips Formula";

	/** Een tweede receptuur, zodat zoeken ook echt iets te filteren heeft. */
	public static final String ANDERE_RECEPTUUR = "Green Smoothie Mix";

	private Testdata() {
	}

	public static void initialiseer() {
		try (Connection connection = DriverManager.getConnection(
				Testomgeving.dbUrl(), Testomgeving.dbUser(), Testomgeving.dbPassword())) {

			connection.setAutoCommit(false);
			maakLeeg(connection);
			vulAan(connection);
			connection.commit();
		}
		catch (SQLException e) {
			throw new IllegalStateException(
					"Kon de testgegevens niet klaarzetten in " + Testomgeving.dbUrl()
							+ ". Draait de databank, en wijst de test naar dezelfde databank"
							+ " als de toepassing?", e);
		}
	}

	/** Leegmaken van kind naar ouder, zodat geen enkele sleutel in de weg zit. */
	private static void maakLeeg(Connection connection) throws SQLException {
		String[] tabellen = {
				"parameters",
				"freeze_dry_instructions",
				"products",
				"ingredients",
				"formulas",
				"raw_materials",
				"nutritional_values",
				"bio_engineers"
		};
		try (Statement statement = connection.createStatement()) {
			for (String tabel : tabellen) {
				statement.executeUpdate("DELETE FROM " + tabel);
			}
		}
	}

	private static void vulAan(Connection connection) throws SQLException {
		long bioEngineer = voegBioEngineerToe(connection, NAAM, EMAIL, "Fruit Processing", PASSWORD);

		long voedingBanaan = voegNutritionalValueToe(connection, 89.0, 1.1, 22.8, 0.3);
		long voedingAardbei = voegNutritionalValueToe(connection, 32.0, 0.7, 7.7, 0.3);
		long voedingSpinazie = voegNutritionalValueToe(connection, 23.0, 2.9, 3.6, 0.4);

		long banaan = voegGrondstofToe(connection, "FRUIT", "Banana", voedingBanaan);
		long aardbei = voegGrondstofToe(connection, "FRUIT", "Strawberry", voedingAardbei);
		long spinazie = voegGrondstofToe(connection, "VEGETABLE", "Spinach", voedingSpinazie);

		// De receptuur van het scenario start met één ingrediënt en in ontwikkeling.
		long receptuur = voegReceptuurToe(connection, RECEPTUUR, "1.0",
				"1. Was de bananen. 2. Snijd ze op 3 mm. 3. Vries in op -40 graden.", bioEngineer);
		voegIngredientToe(connection, receptuur, aardbei, "200.00", "GRAM", 1);

		// Een tweede receptuur die niet aan de zoekterm "Banana" beantwoordt.
		long andere = voegReceptuurToe(connection, ANDERE_RECEPTUUR, "2.1",
				"1. Was de spinazie. 2. Meng met aardbei. 3. Vries in.", bioEngineer);
		voegIngredientToe(connection, andere, spinazie, "250.00", "GRAM", 1);

		// Zorgt ervoor dat de grondstof banaan ook echt gekozen kan worden.
		if (banaan <= 0) {
			throw new IllegalStateException("De grondstof Banana werd niet aangemaakt");
		}
	}

	private static long voegBioEngineerToe(Connection connection, String naam, String email,
										   String specialisatie, String wachtwoord) throws SQLException {
		String sql = "INSERT INTO bio_engineers (name, email, phone_number, specialization, password)"
				+ " VALUES (?, ?, ?, ?, ?)";
		try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			statement.setString(1, naam);
			statement.setString(2, email);
			statement.setString(3, "+32-123-456-789");
			statement.setString(4, specialisatie);
			statement.setString(5, wachtwoord);
			statement.executeUpdate();
			return gegenereerdeSleutel(statement);
		}
	}

	private static long voegNutritionalValueToe(Connection connection, double calorieen, double eiwitten,
												double koolhydraten, double vetten) throws SQLException {
		// Alle kolommen zijn verplicht: het zijn primitieve velden in de entiteit.
		String sql = "INSERT INTO nutritional_values"
				+ " (calories, proteins, carbohydrates, fats, fibers, sugars, salt,"
				+ "  vitamin_a, vitamin_c, calcium, iron)"
				+ " VALUES (?, ?, ?, ?, 0, 0, 0, 0, 0, 0, 0)";
		try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			statement.setDouble(1, calorieen);
			statement.setDouble(2, eiwitten);
			statement.setDouble(3, koolhydraten);
			statement.setDouble(4, vetten);
			statement.executeUpdate();
			return gegenereerdeSleutel(statement);
		}
	}

	private static long voegGrondstofToe(Connection connection, String soort, String naam,
										 long voedingswaardeId) throws SQLException {
		String sql = "INSERT INTO raw_materials"
				+ " (material_type, name, organic_certified, price, unit, nutritional_value_id)"
				+ " VALUES (?, ?, ?, ?, ?, ?)";
		try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			statement.setString(1, soort);
			statement.setString(2, naam);
			statement.setBoolean(3, true);
			statement.setBigDecimal(4, new java.math.BigDecimal("2.50"));
			statement.setString(5, "kg");
			statement.setLong(6, voedingswaardeId);
			statement.executeUpdate();
			return gegenereerdeSleutel(statement);
		}
	}

	private static long voegReceptuurToe(Connection connection, String naam, String versie,
										 String bereidingswijze, long bioEngineerId) throws SQLException {
		String sql = "INSERT INTO formulas"
				+ " (name, version, preparation_method, creation_date, last_modified, status, bio_engineer_id)"
				+ " VALUES (?, ?, ?, ?, ?, 'IN_DEVELOPMENT', ?)";
		try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			java.sql.Date vandaag = java.sql.Date.valueOf(java.time.LocalDate.now());
			statement.setString(1, naam);
			statement.setString(2, versie);
			statement.setString(3, bereidingswijze);
			statement.setDate(4, vandaag);
			statement.setDate(5, vandaag);
			statement.setLong(6, bioEngineerId);
			statement.executeUpdate();
			return gegenereerdeSleutel(statement);
		}
	}

	private static void voegIngredientToe(Connection connection, long receptuurId, long grondstofId,
										  String hoeveelheid, String eenheid, int volgorde) throws SQLException {
		String sql = "INSERT INTO ingredients (quantity, unit, sequence, formula_id, raw_material_id)"
				+ " VALUES (?, ?, ?, ?, ?)";
		try (PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setBigDecimal(1, new java.math.BigDecimal(hoeveelheid));
			statement.setString(2, eenheid);
			statement.setInt(3, volgorde);
			statement.setLong(4, receptuurId);
			statement.setLong(5, grondstofId);
			statement.executeUpdate();
		}
	}

	private static long gegenereerdeSleutel(PreparedStatement statement) throws SQLException {
		try (ResultSet sleutels = statement.getGeneratedKeys()) {
			if (!sleutels.next()) {
				throw new SQLException("De databank gaf geen gegenereerde sleutel terug");
			}
			return sleutels.getLong(1);
		}
	}
}
