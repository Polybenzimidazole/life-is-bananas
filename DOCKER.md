# Projecten samen op Docker

Voer de commando's uit vanuit de repositoryroot. Docker Desktop moet draaien met Linux-containers.

| Project | Frontend / toepassing | Backend | Databasebeheer |
|---|---|---|---|
| Project 1, bestaande instantie | http://localhost:8082/app/index.html | http://localhost:8082 | http://localhost:8083 |
| Project 4 | http://localhost:8081 | http://localhost:8080 | MySQL intern |
| Deel 5 | http://localhost:8085 | http://localhost:8084 | Optioneel http://localhost:8086 |

```powershell
docker compose -f Project-1-Rechttoe-Rechtaan/compose.existing.yaml up -d --wait
docker compose up --build -d --wait --wait-timeout 240
docker compose -f compose.deel5.yaml up --build -d --wait --wait-timeout 240
```

Project 1: `sarah.johnson@lifeisbananas.com` / `banaan123`.
Project 4 en Deel 5: `admin` / `Admin123!`, of de gebruiker met leesrechten
`engineer1` / `Engineer123!`.

Project 1 gebruikt het bestaande JAR-bestand in `C:/Users/vanov/javamount`,
de bestaande `mariadb-data`-volume en het externe netwerk `lifebananas-network`.
De lokale `.env.app.local` en `.env.db.local` in de Project 1-map bewaren de
bestaande instellingen en worden niet in Git opgenomen. Bewaar deze bestanden.
`PROJECT1_JAR_DIR` kan de locatie van het JAR-bestand overschrijven.

Elk project heeft een eigen databasevolume en sessiecookienaam. De oude gestopte
Project 1-containers met suffix `-before-port-move` blijven als terugvaloptie
bewaard; start ze niet naast hun vervangers met hetzelfde databasevolume.

## Deel 5 en de nieuwe deployment-map

Deel 5 gebruikt de actuele OAuth/PKCE-implementatie van de branch. De frontend
benadert de API via `/api/` en de autorisatieserver via `/oauth2/` op dezelfde
frontend-origin. De callback en issuer worden ingesteld via `APP_FRONTEND_URL`
en `APP_ISSUER`; beide gebruiken standaard frontendpoort 8085 in Docker.
CORS wordt centraal geconfigureerd via `APP_ALLOWED_ORIGINS`.

De deployment-map verwijst met Compose `include` naar dezelfde configuratie:

```powershell
docker compose -f "deel 5 deployment/compose.yaml" up --build -d --wait --wait-timeout 240
```

Beide ingangen gebruiken dezelfde projectnaam, containers en databasevolume.
De images bouwen de JAR en Vue-bestanden uit de huidige broncode. Voor deze
opstelling hoef je dus geen JAR of frontendbuild handmatig naar mountmappen te
kopiëren. De eerder toegevoegde bestanden in `nginxetcmount` blijven behouden;
de gebouwde frontendimage gebruikt `deel 5 frontend/nginx.conf`.

De optionele phpMyAdmin voor Deel 5 gebruikt poort 8086 en wordt zo gestart:

```powershell
docker compose -f compose.deel5.yaml --profile admin up -d phpmyadmin
```

Een nieuwe push verandert de inhoud van bestaande images niet. Bouw na relevante
broncodewijzigingen opnieuw met `up --build`. Los eerst eventuele mergeconflicten
op. De startupcontrole wacht op de database en de backend.

## Stoppen en status

```powershell
docker compose -f Project-1-Rechttoe-Rechtaan/compose.existing.yaml down
docker compose down
docker compose -f compose.deel5.yaml --profile admin down
docker compose ls
```

Deze commando's bewaren de gegevens. Het Project 1-volume is extern en wordt niet
door deze Compose-configuratie beheerd. Gebruik geen `down --volumes` wanneer je
de Project 4- of Deel 5-database wilt bewaren.

Kopieer `.env.example` naar `.env` voor optionele poort- en wachtwoordinstellingen.
Het wijzigen van een databasewachtwoord in de omgeving past een bestaande
databasegebruiker niet automatisch aan.
