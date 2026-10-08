# Life is Bananas

Webtoepassing voor het receptbeheer van vriesgedroogde snacks: Spring Boot met
Thymeleaf-pagina's, JPA op MySQL, en een geautomatiseerde UI-acceptatietest met
Cucumber en Selenium.

Het domeinmodel komt uit de paper *Analysis, Design & Testing: Desktop*
(team 2), hier uitgewerkt als een gelaagde webtoepassing: domain, dao, service
en controller, met de acceptatietesten daar los van.

## Wat je nodig hebt

| | |
|---|---|
| Java | 21 of hoger |
| MySQL of MariaDB | een database `life_is_bananas` (zie hieronder) |
| Chrome | enkel voor de acceptatietest |

Maven mag je overslaan: gebruik de Maven die in IntelliJ zit.

## Database klaarzetten

Het schema staat in [db/life_is_bananas.sql](db/life_is_bananas.sql). De
toepassing staat op `ddl-auto: none`, dus Hibernate maakt zelf geen tabellen
aan: dat script is de enige bron van het schema. Pas je een entiteit aan, pas
dan ook het script aan.

```bash
mysql -u root -p < db/life_is_bananas.sql
```

Bestaan de database en de gebruiker nog niet, haal dan eerst het commentaar
bovenaan het script weg. De verbindingsgegevens staan in
[application.yml](src/main/resources/application.yml) en zijn te overschrijven
met `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER` en `DB_PASSWORD`.

## Draaien

```bash
mvn spring-boot:run
```

De toepassing luistert op <http://localhost:8080>. Aanmelden kan met
`sarah.johnson@lifeisbananas.com` en wachtwoord `banaan123`.

| Pagina | URL |
|---|---|
| Aanmelden | `/login.html` |
| Recepturen zoeken | `/recepten.html`, `/recepten.html?zoek=Banana` |
| Eén receptuur | `/recept.html?id=1` |
| Afmelden | `/logout.html` |

### Zonder MySQL

Er is een demoprofiel dat op een H2-bestandsdatabank draait en zichzelf vult:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=demo
```

Handig om iets te tonen op een machine zonder MySQL. Het is nadrukkelijk geen
vervanging van de echte opstelling.

## Testen

### Unit- en weblaagtesten

```bash
mvn test
```

Deze hebben geen databank en geen browser nodig:

- `FormulaTest` test de bedrijfsregels van de receptuur: de volgorde van de
  ingrediënten en de toegelaten statusovergangen.
- `FormulaServiceImplTest` test de servicelaag met nagebootste repositories.
- `ReceptControllerTest` en `LoginControllerTest` sturen echte verzoeken naar
  de controllers en laten de Thymeleaf-templates ook echt renderen.
- `DomeinMappingTest` zet het hele domeinmodel op een in-memory databank, en
  controleert zo de JPA-mapping en de opzoekmethodes van de repositories.

### De acceptatietest

Dit is de geautomatiseerde UI-test met Cucumber en Selenium. Hij opent een
echte Chrome en klikt door de echte toepassing heen.

**Start eerst de toepassing**, en draai daarna:

```bash
mvn verify -Pacceptance
```

Of draai `AcceptanceTestSuite` rechtstreeks vanuit IntelliJ.

Nuttige schakelaars:

```bash
# browser onzichtbaar
mvn verify -Pacceptance -Dlib.headless=true

# toepassing draait elders
mvn verify -Pacceptance -Dlib.base.url=http://localhost:9000

# tegen de toepassing die met het demoprofiel op H2 draait
mvn verify -Pacceptance,acceptance-demo
```

Het Serenity-rapport komt in `target/site/serenity/index.html`, ook als een
scenario faalt.

Sinds Selenium 4.6 haalt Selenium Manager zelf de juiste chromedriver op: je
hoeft er dus geen meer naast het project te zetten.

#### Het scenario

[receptBeheren.feature](src/test/resources/features/receptBeheren.feature) legt
de gevraagde flow vast: aanmelden, een recept zoeken, het openen, er een
ingrediënt aan toevoegen, de status wijzigen en het resultaat controleren.

```gherkin
Scenario: Add an ingredient to a recipe and mark it as tested
  Given I log in as "sarah.johnson@lifeisbananas.com" with password "banaan123"
  When I search for recipes with "Banana"
  Then I should see the recipe "Banana Chips Formula" in the search results
  When I open the recipe "Banana Chips Formula"
  And I add the ingredient "Banana" with quantity "300" and unit "GRAM"
  And I change the status to "TESTED"
  Then the recipe should have 2 ingredients
  And the recipe should contain the ingredient "Banana" with quantity "300" and unit "GRAM"
  And the recipe status should be "TESTED"
```

Daarnaast staan er drie scenario's die de randgevallen afdekken: een status
overslaan, zoeken zonder resultaat, en aanmelden met een verkeerd wachtwoord.

#### Hoe de testgegevens klaargezet worden

Elk scenario begint met `Given the test data has been initialized`. Die stap
roept [Testdata](src/test/java/be/lifeisbananas/acceptancetests/support/Testdata.java)
aan, die rechtstreeks met **dezelfde databank als de draaiende toepassing**
praat en daar:

1. alle tabellen leegmaakt, van kind naar ouder zodat geen enkele
   verwijssleutel in de weg zit;
2. een vaste set gegevens inzet: de bio-ingenieur waarmee aangemeld wordt,
   drie grondstoffen, en twee recepturen waarvan `Banana Chips Formula` met
   één ingrediënt en in status `IN_DEVELOPMENT` start.

Daardoor begint elk scenario vanaf precies dezelfde toestand, ongeacht wat een
vorig scenario of een handmatige klik achterliet. Het scenario kan dan harde
uitspraken doen ("2 ingrediënten", "status TESTED") in plaats van vage.

De id's liggen niet vast maar worden teruggelezen uit de databank
(`getGeneratedKeys`). Dat werkt zowel op MySQL als op H2, en voorkomt dat de
teller van een identity-kolom uit de pas loopt met de ingevoerde rijen.

De test leest zijn verbindingsgegevens uit `lib.db.url`, `lib.db.user` en
`lib.db.password`; het profiel `acceptance` vult die met dezelfde waarden als
`application.yml`.

## Hoe het in elkaar zit

```
src/main/java/be/lifeisbananas/
├── LifeIsBananasApplication.java   startpunt
├── config/                         sessie en de LoginInterceptor
├── controller/                     neemt verzoeken aan, kiest een view
├── service/                        de logica; enige laag die de dao's gebruikt
├── dao/                            Spring Data-repositories
└── domain/                         de entiteiten uit het klassendiagram
src/main/resources/
├── application.yml                 MySQL
├── application-demo.yml            H2, voor het demoprofiel
├── demo-data.sql                   startgegevens van het demoprofiel
├── static/css/                     Bootstrap 3 en wat eigen opmaak
└── templates/                      Thymeleaf-pagina's, met fragments/
src/test/
├── java/.../acceptancetests/
│   ├── AcceptanceTestSuite.java    draait de features
│   ├── steps/                      de stapdefinities, met de WebDriver erin
│   └── support/                    testgegevens en testinstellingen
├── java/.../{controller,service,domain}/   de gewone testen
└── resources/features/             de .feature-bestanden
db/life_is_bananas.sql              schema en startgegevens
```

De lagen hangen in één richting aan elkaar: **controller → service → dao →
domain**. Een controller spreekt dus nooit rechtstreeks een repository aan, en
de bedrijfsregels staan in het domein, niet in de service.

### Een nieuw stuk functionaliteit toevoegen

Steeds dezelfde route, naar het voorbeeld van `Formula`:

1. Entiteit in `domain/`, en de tabel in `db/life_is_bananas.sql`.
2. Repository in `dao/`.
3. Methode bij de service en haar implementatie.
4. Methode in een controller, en een template in `templates/`.
5. Een scenario in een `.feature` met de stappen in `acceptancetests/steps/`.

## Keuzes die uitleg verdienen

- **De statuscyclus zit op `Formula`.** De paper legt
  `IN_DEVELOPMENT → TESTED → APPROVED → IN_PRODUCTION` bij `Product`. Het
  gevraagde scenario wijzigt de status van het recept dat je net geopend hebt,
  dus heeft de receptuur die cyclus ook gekregen. Beide gebruiken dezelfde
  enum `LifecycleStatus`, met dezelfde regel: er mag telkens maar één stap
  vooruit gezet worden. De paper noemt die enum `ProductStatus`; hij is
  hernoemd omdat hij nu door twee entiteiten gedeeld wordt.
- **Een goedgekeurde receptuur mag niet meer gewijzigd worden.** Zodra de
  status `APPROVED` is, weigert `addIngredient`. Anders zou je de inhoud van
  iets kunnen veranderen dat al goedgekeurd is.
- **Hand geschreven DAO's zijn Spring Data-repositories geworden.** De
  interface-per-entiteit uit de paper blijft, maar de implementatie komt van
  Spring.
- **Het wachtwoord staat onversleuteld in de databank** en het aanmelden
  gebeurt met een gewone sessie, zonder Spring Security. Dat is bewust simpel
  gehouden voor deze opdracht; in een echte toepassing hoort daar minstens
  een gehasht wachtwoord te staan.
- **`vitaminA` en `vitaminC` hebben een expliciete kolomnaam.** De naamstrategie
  van Spring zet geen underscore voor een hoofdletter die helemaal achteraan
  staat, en maakte er anders `vitamina` van.
- **`Parameter.value` heet in de databank `parameter_value`**, omdat `value`
  in H2 een gereserveerd woord is.
