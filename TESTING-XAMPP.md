# Project 1-4 in IntelliJ met XAMPP

De applicatie en de Cucumber/Selenium-tests draaien lokaal vanuit IntelliJ.
Alleen de database draait via XAMPP MySQL (MariaDB).
De bestaande Docker-deployments en Docker-testconfiguraties blijven beschikbaar.

## Eenmalig: testdatabase klaarzetten

1. Open het XAMPP Control Panel en start **MySQL**.
2. Open de IntelliJ-terminal in de repositoryroot en voer uit:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\Initialize-XamppTestDatabase.ps1
```

Dit maakt de aparte database `life_is_bananas_acceptance` met het schema en de
startgegevens. Een bestaande gevulde testdatabase wordt overgeslagen.
De bestaande `life_is_bananas`-database blijft behouden.

De standaardinstellingen zijn:

| Instelling | Waarde |
|---|---|
| Applicatie | http://localhost:8091/login.html |
| Databasehost | 127.0.0.1 |
| Databasepoort | 3306 |
| Databasenaam | life_is_bananas_acceptance |
| Databasegebruiker | root |
| Databasewachtwoord | leeg |

Poort 8091 laat de Docker-applicaties op 8080-8085 en de optionele
Docker-testapp op 8090 naast de lokale applicatie draaien.

## Applicatie en tests starten

1. Druk **Ctrl + Shift + A**, zoek **Reload All Maven Projects** en druk Enter.
2. Druk **Ctrl + Shift + A**, zoek **Rebuild Project** en druk Enter.
3. Druk **Alt + Shift + F10** en start **Project 1 - App (XAMPP)**.
   Wacht op `Started LifeIsBananasApplication` in de console.
4. Druk opnieuw **Alt + Shift + F10** en start **Project 1 - Tests (XAMPP)**.
   Chrome opent en voert de scenario's uit.

Voor Project 2, 3 of 4 kies je de configuraties met dat projectnummer.
Stop de vorige **App (XAMPP)** voordat je de volgende start: ze gebruiken
allemaal poort 8091. Laat XAMPP MySQL draaien.

Start de tests **een voor een**. Elk scenario zet dezelfde aparte testdatabase
terug naar een vaste begintoestand.

De App-configuraties starten `LifeIsBananasApplication` rechtstreeks met het
Spring-profiel `xampp`. De Tests-configuraties draaien `AcceptanceTestSuite`
rechtstreeks via IntelliJ JUnit. XAMPP is ook de standaard wanneer je de suite
met het groene pijltje naast de testklasse start.
Java 21 en de lokaal geinstalleerde Java 25 worden ondersteund. Chrome moet
geinstalleerd zijn.

## Bestaande IntelliJ-configuratie aanpassen

Bij een zelf aangemaakte Spring Boot-configuratie: zet **Active profiles** op
`xampp`, of voeg dit toe aan **Program arguments**:

```text
--spring.profiles.active=xampp
```

Bij een zelf aangemaakte JUnit-configuratie voor `AcceptanceTestSuite`
wordt XAMPP automatisch gekozen. Deze VM-optie kan je ook expliciet instellen:

```text
-Dlib.environment=xampp
```

Gebruik de betreffende projectmap als **Working directory**.
Een Maven-profiel geldt alleen bij een Maven-run.

## Maven en het Serenity-rapport

Met een draaiende App (XAMPP) kun je vanuit de betreffende projectmap ook draaien:

```powershell
mvn verify "-Pacceptance"
# Zonder zichtbaar Chrome-venster:
mvn verify "-Pacceptance" "-Dlib.headless=true"
```

Gebruik IntelliJ's ingebouwde Maven wanneer `mvn` niet op PATH staat.
Maven maakt het volledige HTML-rapport in
`<project>/target/site/serenity/index.html`.

## Afwijkende XAMPP-instellingen

Voor de app kun je in de IntelliJ-runconfiguratie omgevingsvariabelen instellen:
`XAMPP_DB_HOST`, `XAMPP_DB_PORT`, `XAMPP_DB_USER`,
`XAMPP_DB_PASSWORD` en `XAMPP_APP_PORT`.
De initializer ondersteunt onder meer `-XamppPath`, `-DbPort` en `-DbUser`;
het wachtwoord kan via `XAMPP_DB_PASSWORD`.
Gebruik dezelfde waarden bij de tests via VM-opties `-Dlib.db.url=...`,
`-Dlib.db.user=...`, `-Dlib.db.password=...` en eventueel `-Dlib.base.url=...`.
De databasenaam voor deze tests is `life_is_bananas_acceptance`.

## Fouten herkennen

- `UnknownHostException: mariadb-server`: de lokale app gebruikt het
  XAMPP-profiel nog niet. Start de configuratie **App (XAMPP)**.
- `Unknown database 'life_is_bananas_acceptance'`: voer de initializer uit.
- `Communications link failure`: controleer of XAMPP MySQL gestart is op 3306.
- `Access denied for user 'root'`: pas de XAMPP-wachtwoordinstellingen aan.
- `Access denied for user 'lifeisbananasuser'`: verwijder oude
  `-Dlib.db.*`-VM-opties uit de testconfiguratie en rebuild het project.
  De standaard XAMPP-gebruiker is `root` met een leeg databasewachtwoord.
- `Port 8091 was already in use`: stop de vorige lokale app.
- Ontbrekende Lombok-getters: herlaad Maven en controleer annotation processing
  onder **Settings > Build, Execution, Deployment > Compiler > Annotation Processors**.

De optionele Docker-testopstelling staat in [TESTING.md](TESTING.md).
De bestaande Docker-deployments staan in [DOCKER.md](DOCKER.md).
