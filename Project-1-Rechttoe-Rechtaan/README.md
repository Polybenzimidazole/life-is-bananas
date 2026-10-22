# Project 1 — REST-API en Vue

Dit breidt de bestaande Spring Boot/Thymeleaf-toepassing uit met een REST-API
en een eenvoudige Vue 3-app. Naar het voorbeeld van `timesheetback` staat de
JSON-controller in de backend; zoals bij `timesheet-front-simple` haalt Vue
gegevens op via HTTP. De Vue-app gebruikt `fetch` en de Options API.

De bestaande servicelaag, JPA-repositories, MySQL-tabellen en domeinregels
worden hergebruikt. De klassieke `.html`-pagina's blijven beschikbaar.

## Bestaande Docker-opstelling

In de bestaande opstelling draait `java-lifebananas` op **8080**,
phpMyAdmin (`myadmin2`) op **8081** en MariaDB intern als `mariadb-server:3306`
op `lifebananas-network`. Poort 8081 is dus de databasebeheerinterface.
Gebruik hier de standaard Spring-configuratie, zonder profiel `local`.
De aanvullende `compose.yaml` is alleen voor een afzonderlijke lokale MySQL;
die is niet nodig voor de bestaande Docker-opstelling.

De Vue-build kan in hetzelfde jar-bestand worden opgenomen (zie onderaan).
De bestaande Java-container leest zijn jar uit
`C:\Users\vanov\javamount\life-is-bananas-1.0.0-SNAPSHOT.jar`.
Bewaar een kopie van het vorige jar-bestand, vervang het door
`target/life-is-bananas-1.0.0-SNAPSHOT.jar` en voer
`docker restart java-lifebananas` uit. De database blijft behouden.

Open daarna **http://localhost:8080/app/index.html** voor de Vue-app;
phpMyAdmin blijft op **http://localhost:8081**.

## Starten met MySQL

Benodigd: Java 21+, Maven (of de Maven van IntelliJ), Node.js/npm en Docker Desktop.
Voer de backendcommando's uit in deze map, `Project-1-Rechttoe-Rechtaan`.

```powershell
docker compose up -d --wait
mvn spring-boot:run "-Dspring-boot.run.profiles=local"
```

Docker maakt een MySQL 8.4-database en voert `db/life_is_bananas.sql` uit
bij de eerste start met een leeg volume. De data staat in het benoemde
volume `mysql-data` en blijft behouden na een containerherstart of
`docker compose down`. Het SQL-script handmatig opnieuw uitvoeren wist de
bestaande tabellen en zet de startgegevens terug.

Het profiel `local` gebruikt `localhost:3306`, database `life_is_bananas`,
gebruiker `lifeisbananasuser` en wachtwoord `lifeisbananasuserpw`.
Overschrijven kan met `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`.
Bij een bezette poort zet je zowel `MYSQL_PORT` (Docker) als `DB_PORT` (Spring)
op dezelfde vrije poort. De oorspronkelijke standaardconfiguratie met host
`mariadb-server` blijft beschikbaar zonder het profiel `local`.

Start in een tweede terminal de frontend:

```powershell
cd frontend
npm ci
npm run serve
```

Open **http://localhost:8082/app/**.

Aanmelden: `sarah.johnson@lifeisbananas.com` / `banaan123`.
Zoek op `Banana`, open de receptuur, voeg een grondstof toe en wijzig de
status. Vernieuw de pagina om de bewaarde toestand opnieuw uit MySQL te lezen.

Vue stuurt `/api` via de ontwikkelproxy naar `http://localhost:8080`.
De browser bewaart de sessiecookie; er zijn geen losse CORS-instellingen nodig.
Voor een andere backend stel je `API_PROXY_TARGET` in vóór `npm run serve`.

Wil je naast de bestaande Docker-app een aparte backend starten, dan kan dat met
`mvn spring-boot:run "-Dspring-boot.run.profiles=local" "-Dspring-boot.run.arguments=--server.port=8090"`.
Zet in de frontendterminal `$env:API_PROXY_TARGET = 'http://localhost:8090'`
en gebruik `npm run serve -- --port 8091`. Open dan `http://localhost:8091/app/`.

Zonder MySQL kan de backend tijdelijk met `-Dspring-boot.run.profiles=demo`
starten. Dit profiel maakt zijn H2-schema bij elke start opnieuw en dient
alleen om de toepassing te demonstreren.

## REST-contract

Alle verzoeken en antwoorden gebruiken JSON, behalve de lege `204` bij afmelden.
Alle routes behalve `/api/session` vereisen een aangemelde sessie.

| Methode | Route | Betekenis |
|---|---|---|
| POST | `/api/session` | Aanmelden met `email` en `password` |
| GET | `/api/session` | Huidige gebruiker (`id`, `name`) |
| DELETE | `/api/session` | Sessie beëindigen |
| GET | `/api/recipes?search=Banana` | Recepturen zoeken; leeg geeft alle recepturen |
| GET | `/api/recipes/{id}` | Detail met ingrediënten, `editable` en `nextStatus` |
| GET | `/api/raw-materials` | Beschikbare grondstoffen |
| GET | `/api/units` | Toegestane eenheden |
| POST | `/api/recipes/{id}/ingredients` | Ingrediënt toevoegen (`201`) |
| PATCH | `/api/recipes/{id}/status` | Status wijzigen |

Ingrediënt toevoegen:

```json
{"rawMaterialId":1,"quantity":300,"unit":"GRAM"}
```

Status wijzigen:

```json
{"status":"TESTED"}
```

De schrijfverzoeken geven het bijgewerkte receptdetail terug. Hoeveelheden
moeten positief zijn, met maximaal 8 cijfers voor en 2 na de komma (passend
bij MySQL `DECIMAL(10,2)`). Eenheden komen uit `/api/units`.

De status volgt `IN_DEVELOPMENT → TESTED → APPROVED → IN_PRODUCTION`.
Je mag telkens één stap vooruit. Vanaf `APPROVED` ligt de samenstelling vast.
Fouten hebben de vorm `{"status":409,"message":"..."}`:
`400` ongeldige invoer, `401` niet aangemeld/verkeerde login,
`404` onbekende receptuur/grondstof en `409` strijdig met een bedrijfsregel.
DTO's vermijden circulaire JPA-relaties en geven geen wachtwoorden terug.

## Testen en bouwen

```powershell
mvn test
cd frontend
npm run build
```

`RecipeApiIntegrationTest` test echte HTTP-controllerverwerking met MockMvc,
services en JPA op een geïsoleerde H2-database: sessies, zoeken, detail,
validatie, bewaren tussen verzoeken en statusregels. De bestaande unit- en
webtesten draaien mee. De Selenium/Cucumber-testen blijven apart via
`mvn verify -Pacceptance` tegen de klassieke pagina's.

De frontendbuild staat in `frontend/dist`. Om deze samen met Spring Boot te
serveren, voer je vanuit de projectmap uit:

```powershell
New-Item -ItemType Directory -Force src/main/resources/static/app
Copy-Item frontend/dist/* src/main/resources/static/app -Recurse -Force
mvn package -DskipTests
java -jar target/life-is-bananas-1.0.0-SNAPSHOT.jar --spring.profiles.active=local
```

Open daarna `http://localhost:8080/app/index.html`. De build gebruikt `/app/`
voor assets en `/api` voor de backend. De gegenereerde bestanden zijn genegeerd
in Git; de Vue-broncode en package-lock worden bijgehouden.

De login hergebruikt het eenvoudige sessiemodel en de bestaande wachtwoorden
van project 1. Voor productie moeten onder meer wachtwoordhashing en
CSRF-bescherming toegevoegd worden.
