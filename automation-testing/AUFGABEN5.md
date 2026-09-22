# Lösungen zu den Übungen "Automation Testing"

Vorgabe: [spring-boot-angular-basic/](spring-boot-angular-basic/) (aus `spring-boot-angular-basic-lw.zip`) – Spring-Boot-Backend (Java 17, Port 8081, `/students` REST-API) + Angular-Frontend (Angular 16, Port 4200, "ng serve"), unverändert übernommen bis auf die Bonus-Änderungen (siehe unten).

## Lokal zum Laufen bringen

```bash
# Backend
cd automation-testing/spring-boot-angular-basic
mvn spring-boot:run          # Port 8081, seedet 5 Studenten beim Start

# Frontend (zweites Terminal)
cd automation-testing/spring-boot-angular-basic/src/main/js/my-app
npm install
npm start                    # Port 4200
```

Beide zusammen manuell getestet: Startseite lädt, "List Students" zeigt die 5 geseedeten Studenten, "Add Students" legt einen neuen an und leitet zurück zur Liste.

## Übung 1 – REST-API automatisiert testen

Tool: **JUnit 5 + REST-Assured** ([StudentApiTest.java](spring-boot-angular-basic/src/test/java/ch/tbz/m450/testing/tools/StudentApiTest.java)). Die App startet dafür pro Testklasse selbst auf einem zufälligen Port (`@SpringBootTest(webEnvironment = RANDOM_PORT)`), komplett automatisiert per `mvn test`, kein manuelles Klicken nötig.

7 Tests: `GET /students` liefert die 5 Studenten, `POST /students` legt einen neuen an und er erscheint danach in der Liste, jeder Student hat ID und Email, plus 3 Tests fürs Bonus-Feature (siehe unten).

**Stolperstein unterwegs:** Die Tests liefen anfangs nur einzeln zuverlässig grün, im vollen Lauf schlug einer fehl – klassisches Testisolations-Problem (JUnit garantiert keine Ausführungsreihenfolge, ein Test hatte einen 6. Studenten angelegt, ein anderer erwartete aber genau 5). Behoben mit `@BeforeEach`, das die DB vor **jedem** Test explizit auf denselben Ausgangszustand zurücksetzt, statt sich auf die Reihenfolge zu verlassen.

Ausführen: `mvn test` → **7/7 grün**.

## Übung 2 – Frontend E2E-Test

Tool: **Playwright** ([e2e/](spring-boot-angular-basic/e2e/)), automatisiert in echtem (headless) Chromium.

4 Tests: Startseite zeigt beide Navigationslinks, Studentenliste zeigt die Backend-Daten, ein neuer Student wird über das Formular angelegt und erscheint danach in der Liste, der Submit-Button bleibt deaktiviert bis beide Pflichtfelder ausgefüllt sind.

Voraussetzung: Backend **und** Frontend laufen bereits lokal (siehe oben).

```bash
cd automation-testing/spring-boot-angular-basic/e2e
npm install
npx playwright install chromium
npx playwright test
```

Ergebnis: **4/4 grün**.

## Übung 3 – Lasttest

Details, Tool-Begründung und Ergebnisse in [load-test/LASTTEST.md](spring-boot-angular-basic/load-test/LASTTEST.md). Kurzfassung: Postman-Collection erstellt und mit Newman funktional validiert (5/5 Tests grün), echter Lasttest mit `autocannon` (Newman spielt Requests nur nacheinander ab, kein echter Nebenläufigkeits-Test) – bis zu ~15'800 Requests/Sekunde bei 20 gleichzeitigen Verbindungen, alle beantworteten Requests mit Status 200, aber steigende Anzahl abgebrochener Verbindungen bei hoher Last (~0,3 %) – vermutlich die Kapazitätsgrenze des eingebetteten Tomcat mit Standardeinstellungen.

## Bonus – Feature: Input-Validierung & Error Handling

**Feature-Definition:** `POST /students` nimmt aktuell jede Eingabe an, auch leere Namen oder ungültige Email-Adressen. Neu: Validierung auf `Student` (`@NotBlank` für Name, `@NotBlank` + `@Email` für Email), der Controller nutzt `@Valid` und liefert bei ungültiger Eingabe HTTP 400 mit einer kompakten JSON-Fehlermeldung pro Feld statt Springs generischer Standardantwort (eigener `@ExceptionHandler`).

**Umsetzung:** [Student.java](spring-boot-angular-basic/src/main/java/ch/tbz/m450/testing/tools/repository/entities/Student.java), [StudentController.java](spring-boot-angular-basic/src/main/java/ch/tbz/m450/testing/tools/controller/StudentController.java), getestet in `StudentApiTest.java` (leerer Name → 400 mit Meldung, ungültige Email → 400 mit Meldung, ungültiger Request landet nicht in der DB).

**Zeit/Reflexion:** Die Aufgabe ist als Schätz-Übung gedacht ("eine Lektion einplanen, dann Ist-Zeit dokumentieren") – das ist für eine KI kein sinnvoller Vergleich, deshalb hier ehrlich eingeordnet statt einer erfundenen Zeitangabe: Der Umfang (zwei Annotationen auf der Entity, `@Valid` + ein `@ExceptionHandler` im Controller, drei Tests) ist klein genug, dass er in einer Lektion realistisch machbar ist. Der einzige Teil, der beim ersten Durchlauf tatsächlich stolperte, war nicht die Validierung selbst, sondern der bereits erwähnte Testisolations-Bug in Übung 1 – ein guter Reminder, dass "ich habe einen Test geschrieben" nicht dasselbe ist wie "meine Tests sind unabhängig voneinander", und dass sich das lohnt, immer im vollen Lauf (nicht nur einzeln) zu prüfen.
