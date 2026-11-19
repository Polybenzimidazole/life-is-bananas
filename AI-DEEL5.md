# Spring AI in Deel 5

De AI-assistent helpt een aangemelde gebruiker met vragen over een gekozen
recept, de benodigde grondstoffen en de actuele voorraad. Bijvoorbeeld:
**“Is er voldoende voorraad voor 10 eenheden Banana Bread?”**

De uitwerking volgt de structuur van `jah-bot`: een `Question`-record, een
`Answer`-record, een REST-controller, een service met Spring AI `ChatClient`
en WireMock voor de OpenAI REST API. Beide gebruiken Spring AI **1.0.3**
en `wiremock-spring-boot` **4.0.7**. In Life is Bananas komen daar de
receptcontext, JWT-beveiliging en een Vue-pagina bij.

## De drie opdrachtvereisten

| Vereiste | Uitwerking |
| --- | --- |
| Spring AI Question-Answer | `POST /ai/ask` → `AssistantRestController` → `BananaAssistantService` → `ChatClient` → OpenAI Chat Completions. |
| OpenAI REST API mocken in een gepaste test | `BananaAssistantIntegrationTest` start WireMock op een vrije lokale poort, gebruikt een vast testantwoord en controleert ook het uitgaande HTTP-verzoek. |
| Een generatief patroon bestuderen en illustreren | Retrieval Augmented Generation (RAG): het gekozen recept en de voorraad worden uit de eigen database opgehaald en toegevoegd aan de prompt. |

## Proberen met Docker

Gebruik de Deel 5-stack uit [DOCKER.md](DOCKER.md). Vul de AI-instellingen in
de bestaande `.env` in de repositoryroot aan. Als je die nog niet hebt, kun
je `.env.example` kopiëren naar `.env`.

```dotenv
AI_CHAT_PROVIDER=openai
OPENAI_API_KEY=je-eigen-openai-api-sleutel
OPENAI_MODEL=gpt-4o-mini
```

Start of herbouw vanuit de repositoryroot:

```powershell
docker compose -f compose.deel5.yaml up --build -d --wait --wait-timeout 240
```

Open <http://localhost:8085/assistant>, meld je aan met bijvoorbeeld
`engineer1` / `Engineer123!`, kies een recept en stel een vraag. Ook een
gewone gebruiker mag vragen stellen; hiervoor is geen ADMIN-rol nodig.

De sleutel wordt uitsluitend aan de backend doorgegeven. De Vue-app stuurt
een JWT naar de eigen backend, nooit de OpenAI-sleutel. Zonder AI-configuratie
start de toepassing gewoon. Een vraag levert dan HTTP 503 op met de melding
dat de AI-assistent nog niet is ingesteld. `AI_CHAT_PROVIDER=none` schakelt
de OpenAI-chatintegratie uit.

## Lokaal draaien

De backend gebruikt Java 21 en Maven. De bestaande MySQL-instellingen blijven
van toepassing. Stel in de terminal of de IntelliJ-runconfiguratie van de
backend deze omgevingsvariabelen in:

```powershell
$env:AI_CHAT_PROVIDER = 'openai'
$env:OPENAI_API_KEY = 'je-eigen-openai-api-sleutel'
$env:SERVER_PORT = '8084'
$env:APP_FRONTEND_URL = 'http://localhost:8085'
$env:APP_ISSUER = 'http://localhost:8085'
$env:APP_ALLOWED_ORIGINS = 'http://localhost:8085'
Set-Location 'deel 5 backend'
mvn spring-boot:run
```

Start de frontend in een tweede terminal vanuit `deel 5 frontend`:

```powershell
npm ci
npm run serve
```

De ontwikkelproxy stuurt `/api/ai/ask` door naar `/ai/ask` op de backend.
Een `.env` voor Docker Compose wordt niet automatisch ingelezen door een
rechtstreeks gestarte Java-app; geef de variabelen daar expliciet mee.

## Het generatieve patroon: RAG

Een taalmodel kent de inhoud van onze database niet. Alleen vragen hoeveel
bananen beschikbaar zijn zou dus geen betrouwbare koppeling met het project
opleveren. RAG voegt eerst relevante eigen gegevens toe aan de modelinput.
Spring AI beschrijft dit bij
[AI Concepts: eigen data en RAG](https://docs.spring.io/spring-ai/reference/1.0/concepts.html)
en [Retrieval Augmented Generation](https://docs.spring.io/spring-ai/reference/1.0/api/retrieval-augmented-generation.html).

De concrete flow is:

1. **Retrieval:** `RecipeContextService.retrieve(recipeId)` haalt het gekozen
   recept, zijn ingrediënten en de voorraad van die grondstoffen op via JPA.
   Elke vraag haalt de gegevens opnieuw op.
2. **Augmentation:** een JSON-momentopname bevat de receptnaam, de hoeveelheid
   per eenheid, de maateenheid en de beschikbare voorraad per ingrediënt.
   `BananaAssistantService` voegt deze gegevens als apart bericht toe aan de
   prompt, samen met de systeeminstructies en de oorspronkelijke vraag.
3. **Generation:** Spring AI `ChatClient` roept OpenAI aan. Het antwoord komt
   samen met de gebruikte databronnen terug naar de Vue-pagina.

Dit is een kleine RAG-uitwerking met gerichte relationele retrieval: de
gebruiker kiest welk recept relevant is. Er is geen vectorindex of
embedding-aanroep nodig. De Spring AI `QuestionAnswerAdvisor` is een
alternatief voor zoeken in een vectorstore en wordt hier niet gebruikt.

De database-transactie eindigt vóór het HTTP-verzoek aan OpenAI. Er worden
alleen recept- en voorraadgegevens meegestuurd, geen gebruikersentiteiten.
Een ontbrekend voorraadrecord wordt `null` in de context: **onbekend**, geen
nulvoorraad. De systeemprompt legt dit vast en vraagt het model om ontbrekende
informatie te benoemen en geen projectgegevens te verzinnen.

Een demonstratie met gecontroleerde testgegevens:

| Ingrediënt | Nodig per eenheid | Voorraad | Nodig voor 10 | Tekort |
| --- | --- | --- | --- | --- |
| Bananas | 0.3 KG | 2.4 KG | 3 KG | 0.6 KG |
| Eggs | 2 PCS | 20 PCS | 20 PCS | 0 PCS |

Vraag hoeveel voorraad nodig is voor 10 eenheden. Vul daarna de voorraad
Bananas aan via de bestaande voorraadpagina en stel de vraag opnieuw. De
volgende prompt bevat de nieuwe voorraad. De AI-functie zelf wijzigt geen
voorraad en voert geen productie uit.

De bronvermeldingen tonen welke databasegegevens zijn meegestuurd. Een model
kan nog steeds een fout antwoord of een rekenfout geven; productiecontrole
blijft bij de bestaande applicatielogica. De pagina bewaart geen gespreksgeheugen:
elke vraag krijgt een nieuwe context en moet zelfstandig leesbaar zijn.

## REST-contract

```http
POST /ai/ask
Authorization: Bearer <access-token>
Content-Type: application/json

{"question":"Is er voldoende voorraad voor 10 eenheden?","recipeId":1}
```

```json
{
  "answer": "Voor 10 eenheden Banana Bread ...",
  "sources": [
    "Recept #1: Banana Bread",
    "Actuele voorraad van de ingrediënten van dit recept"
  ]
}
```

Een vraag is verplicht, bevat maximaal 2000 tekens en heeft een positieve
`recipeId`. Ongeldige invoer geeft 400, een onbekend recept 404 en een verzoek
zonder geldig JWT 401. Bij uitgeschakelde AI, een providerfout of een leeg
modelantwoord geeft de API 503 met een leesbare melding. Providerdetails worden
niet aan de browser doorgegeven. De HTTP-client gebruikt een verbindingstime-out
van 5 seconden en een leestime-out van 45 seconden; een providerfout veroorzaakt
geen automatische herhaalde modelaanroep.

## Testen zonder echte OpenAI-aanroep

Voer vanuit `deel 5 backend` uit met Maven, of voer de twee testklassen uit
via IntelliJ met Java 21:

```powershell
mvn clean test
```

De tests gebruiken H2 en een expliciete dummy-sleutel. Zowel de algemene als
de chatspecifieke OpenAI-base-URL wijzen naar de lokale WireMock-server.
MySQL, Docker en een echte OpenAI-sleutel zijn voor deze tests niet nodig.

`BananaAssistantIntegrationTest` doorloopt de echte controller, beveiliging,
JPA-repositories, contextservice en Spring AI-client. Alleen de externe
OpenAI-server wordt vervangen. De vaste OpenAI JSON-respons staat in
`deel 5 backend/src/test/resources/canned/raw-openai-response.json`.

De tests controleren:

- het antwoord en de databronnen voor USER en ADMIN;
- model, Authorization-header, systeeminstructies, vraag, ingrediënten,
  hoeveelheden, eenheden en voorraad in `POST /v1/chat/completions`;
- nieuwe voorraad in een volgend verzoek en behoud van onbekende voorraad;
- dat een advies de voorraad niet wijzigt;
- afwijzing van ongeldige vragen, onbekende recepten en anonieme verzoeken
  vóór een OpenAI-aanroep;
- afhandeling van OpenAI-statussen 401, 429 en 500, en een leeg modelantwoord;
- starten zonder AI-sleutel en de bijbehorende 503-melding via
  `AssistantDisabledIntegrationTest`.

Deze tests bewijzen het gedrag van onze integratie en het HTTP-contract.
De inhoud van het modelantwoord is vastgelegd in de fixture en bewijst dus
niet dat een echt taalmodel altijd correct rekent of de instructies volgt.

Controleer de frontend vanuit `deel 5 frontend` met `npm run build`.

## Belangrijkste bronbestanden

- `deel 5 backend/pom.xml`: Spring AI BOM, OpenAI-starter en testdependencies.
- `deel 5 backend/src/main/java/org/example/ai/`: vraag, antwoord en contextrecords.
- `deel 5 backend/src/main/java/org/example/controllers/AssistantRestController.java`.
- `deel 5 backend/src/main/java/org/example/service/BananaAssistantService.java`.
- `deel 5 backend/src/main/java/org/example/service/RecipeContextService.java`.
- `deel 5 backend/src/main/resources/prompts/banana-assistant-system.txt`.
- `deel 5 frontend/src/views/Assistant.vue`: vraagformulier, antwoord en bronnen.

De gebruikte Spring AI-configuratie staat ook beschreven in de officiële
[OpenAI Chat-documentatie](https://docs.spring.io/spring-ai/reference/1.0/api/chat/openai-chat.html).
De HTTP-mocking volgt de
[WireMock Spring Boot-integratie](https://wiremock.org/docs/spring-boot/).
