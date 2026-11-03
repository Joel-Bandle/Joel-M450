# Library – Bibliotheksverwaltung mit Ausleihe

REST-Backend (Spring Boot, Java 21, H2) zum Verwalten von Büchern, Mitgliedern und Ausleihen.
Schwerpunkt des Projekts ist das **Testen**: Unit-, Component- und Integrationstests, gemockte Schnittstellen, Reports und eine Pipeline.

Die ausführliche Dokumentation (Planung, Architektur, Testkonzept, Reflexion) steht in [DOKUMENTATION.md](DOKUMENTATION.md).

## Geschäftsregeln

| Regel | Wert |
|---|---|
| Ausleihdauer | 21 Tage |
| Maximal gleichzeitige Ausleihen pro Mitglied | 3 |
| Ein ausgeliehenes Buch kann nicht erneut ausgeliehen werden | – |
| Mahngebühr bis 0 Tage zu spät | CHF 0.00 |
| Mahngebühr 1 bis 7 Tage zu spät | CHF 2.00 |
| Mahngebühr 8 bis 14 Tage zu spät | CHF 5.00 |
| Mahngebühr ab 15 Tagen zu spät | CHF 10.00 (Maximum) |

Wird ein Buch nur mit ISBN angelegt, holt die Anwendung Titel und Autor von der externen Schnittstelle [Open Library](https://openlibrary.org/dev/docs/api/books).

## Starten

Voraussetzung: Java 21 und Maven.

```bash
mvn spring-boot:run
```

Die API läuft danach auf `http://localhost:8080`. Die Datenbank ist eine H2-In-Memory-Datenbank (leer bei jedem Start).

| Methode | Pfad | Zweck | Erfolg | Fehler |
|---|---|---|---|---|
| POST | `/api/books` | Buch anlegen (Titel optional, sonst per ISBN-Lookup) | 201 | 400, 404 (ISBN unbekannt), 409 (ISBN existiert), 502 (ISBN-Dienst down) |
| GET | `/api/books`, `/api/books/{id}` | Bücher abfragen | 200 | 404 |
| POST | `/api/members` | Mitglied anlegen | 201 | 400 |
| GET | `/api/members/{id}` | Mitglied abfragen | 200 | 404 |
| POST | `/api/loans` | Buch ausleihen | 201 | 400, 404, 409 (ausgeliehen / Limit) |
| POST | `/api/loans/{id}/return` | Buch zurückgeben, liefert die Mahngebühr | 200 | 404, 409 (schon zurückgegeben) |
| GET | `/api/loans/{id}` | Ausleihe abfragen | 200 | 404 |

## Teststrategie

| Stufe | Was wird getestet | Werkzeuge | Schnittstellen |
|---|---|---|---|
| **Unit** | Geschäftslogik in `FeeCalculator`, `LoanService`, `BookService` | JUnit 5, `@ParameterizedTest`, Mockito | Repositories und `FeeCalculator` **gemockt**, Uhr fest eingestellt |
| **Component** | Controller (HTTP, Validierung, Fehlercodes) und der `OpenLibraryIsbnClient` | `@WebMvcTest` + MockMvc, WireMock | Services **gemockt**, externe API durch WireMock-Server **ersetzt** |
| **Integration** | Zusammenspiel Controller, Service, Repository, echte H2-Datenbank | `@SpringBootTest`, `@DataJpaTest`, REST-Assured | Nur der externe ISBN-Dienst ist gemockt, der Rest läuft echt |

Vorgehen:
- **Grenzwertanalyse** bei Mahngebühren (0|1, 7|8, 14|15 Tage) und Ausleihlimit (2|3 Ausleihen).
- **Jeder Fehlerfall** der API hat einen Test (400, 404, 409, 502).
- **TDD** wurde für den `FeeCalculator` angewendet, siehe [DOKUMENTATION.md](DOKUMENTATION.md#tdd-im-projekt).
- Die Tests sind voneinander unabhängig: Die Integrationstests leeren vor jedem Test die Datenbank.

Aktueller Stand: **58 Tests** (27 Unit, 20 Component, 11 Integration), Zeilenabdeckung ca. **97 %**.

### Tests lokal ausführen

```bash
mvn verify
```

Das führt alle Tests aus und prüft zusätzlich, dass die Zeilen-Coverage mindestens **80 %** beträgt (sonst schlägt der Build fehl).

### Reports lokal erzeugen

```bash
mvn surefire-report:report-only site:site -DgenerateReports=false
```

| Report | Datei |
|---|---|
| Testresultate | `target/reports/surefire.html` |
| Code Coverage | `target/site/jacoco/index.html` |

## Pipeline und Reports

Die Pipeline [.github/workflows/library-ci.yml](../../.github/workflows/library-ci.yml) läuft bei jedem Push auf `main` (und bei Pull Requests, die dieses Projekt ändern):

| Stage | Inhalt |
|---|---|
| **build** | `mvn package`, Projekt wird gebaut |
| **test** | `mvn verify`: alle Tests, Coverage-Prüfung, HTML-Reports, JUnit-Check-Run, Reports als Artefakt |
| **deploy** | Nur auf `main`: Reports werden auf GitHub Pages veröffentlicht |

Veröffentlichte Reports: https://joel-bandle.github.io/Joel-M450/ (Testresultate und Coverage).

## Code Reviews

Änderungen werden über Feature-Branches und Pull Requests integriert, mit kleinen Commits. Vorgehen und Erkenntnisse stehen in [DOKUMENTATION.md](DOKUMENTATION.md#reflexion-über-tdd-und-code-reviews).

## KI-Nutzung

| Rolle | Eingesetzt |
|---|---|
| Nachschlagewerk für Detailfragen | Ja |
| Schreiben von Code mit KI-Unterstützung (Autocomplete) | Nein |
| **Automatisches Erstellen von Code durch einen Agenten** | **Ja, Claude Code** |
| Reviewen oder Optimieren von bereits geschriebenem Code | Ja, Claude Code (z.B. gezieltes Einbauen von Fehlern, um zu prüfen, dass die Tests sie finden) |

Das Thema (Bibliothek mit Ausleihe) und den Schwerpunkt Testen habe ich gewählt. Umfang, Geschäftsregeln, Code, Tests, Pipeline und Dokumentation hat Claude Code als Agent vorgeschlagen, erstellt und lokal ausgeführt. Ich arbeite das Ergebnis durch, damit ich es erklären und selbständig weiterentwickeln kann, und führe die Commits selbst durch.
