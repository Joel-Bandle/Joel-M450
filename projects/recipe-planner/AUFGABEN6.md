# Lösungen zu den Übungen "Automatisiertes Testen und Deployen"

Vorgabe: [recipe-planner-backend/](recipe-planner-backend/) (Spring Boot, Java) + [recipe-planner-frontend/](recipe-planner-frontend/) (React) aus `recipe-planner-fronend-and-backend.zip`. Laut Aufgabenstellung werden nur Backend-Änderungen benötigt, das Frontend dient nur zum Verständnis.

## In Betrieb genommen

```bash
# Backend
cd projects/recipe-planner/recipe-planner-backend
mvn spring-boot:run          # Port 8080, seedet 15 Rezepte beim Start

# Frontend (zweites Terminal)
cd projects/recipe-planner/recipe-planner-frontend
npm install
npm start                    # Port 3000
```

Beide zusammen manuell getestet: Startseite zeigt die 15 geseedeten Rezepte, vom Backend geladen.

**Zwei Anpassungen an der Vorgabe, unabhängig vom eigentlichen Auftrag:**
- `pom.xml` verlangte `java.version 20` (kurzlebige, nicht-LTS-Version, hier nicht verfügbar) und referenzierte an einer Stelle eine gar nicht definierte Property `${java.version}` – auf `21` korrigiert (via IntelliJs mitgelieferter JBR 21, kein separates JDK installiert).
- Frontend zeigt beim Start eine ESLint-Fehlermeldung ("Environment key jest/globals is unknown") als Overlay – eine Versions-Inkompatibilität in der Vorgabe selbst (neueres ESLint als von Create-React-App erwartet). Die App läuft trotzdem korrekt darunter, Overlay lässt sich schliessen. Nicht behoben, da ausserhalb des Auftrags (nur Backend).

## Aufgabe 1 – Unit Testing

**1. Controller-Methoden** ([RecipeControllerTest.java](recipe-planner-backend/src/test/java/ch/tbz/recipe/planner/controller/RecipeControllerTest.java)): alle drei Endpunkte (`GET /api/recipes`, `GET /api/recipes/recipe/{id}`, `POST /api/recipes`) via **MockMvc** mit `@WebMvcTest`, `RecipeService` gemockt (`@MockBean`) – kein Kontakt zur echten H2-DB.

Stolperstein: `@WebMvcTest` lädt trotzdem die `@Bean`-Methode `init(RecipeRepository)` aus der Hauptklasse `RecipePlannerApplication` mit, wodurch der Testkontext ohne ein zusätzliches `@MockBean RecipeRepository` nicht startete. Behoben durch Mocken dieser (im Controller selbst gar nicht gebrauchten) Abhängigkeit.

Nebenbefund: `RecipeEntityMapper` wird dem Controller injiziert, aber in keiner der drei Methoden tatsächlich benutzt – toter Code.

**2. Mapper-Klassen mit SoftAssertions** ([RecipeEntityMapperTest.java](recipe-planner-backend/src/test/java/ch/tbz/recipe/planner/mapper/RecipeEntityMapperTest.java), [IngredientEntityMapperTest.java](recipe-planner-backend/src/test/java/ch/tbz/recipe/planner/mapper/IngredientEntityMapperTest.java)): reine Unit-Tests ohne Spring-Kontext – die von MapStruct generierten `...Impl`-Klassen lassen sich trotz `componentModel = "spring"` ganz normal per `new` instanzieren, dadurch sehr schnell.

**Vorteil von SoftAssertions:** Normale Assertions brechen beim ersten fehlschlagenden Feld sofort ab – man sieht pro Testlauf höchstens einen Fehler und muss fixen → neu ausführen → nächster Fehler → wiederholen. `SoftAssertions` sammelt dagegen alle Prüfungen und wirft sie erst bei `softly.assertAll()` gemeinsam – bei einem Mapper mit vielen Feldern sieht man sofort **alle** falsch gemappten Felder auf einen Blick statt sie einzeln nacheinander aufzudecken.

Nebenbefund: `RecipeEntityMapper` deklariert kein `uses = IngredientEntityMapper.class`, MapStruct generiert die Ingredient-Mapping-Logik deshalb doppelt (einmal in jedem Mapper), statt sie zu teilen – funktioniert, ist aber Code-Duplikation.

Ausführen: `mvn test` → **9/9 Tests grün** (3 Controller, 3+3 Mapper).

## Aufgabe 2 – Reports

`pom.xml` um **JaCoCo** ergänzt (Coverage-Report), **Surefire** war als Teil des Spring-Boot-Builds bereits vorhanden (JUnit-XML-Reports).

```bash
mvn test
```

erzeugt automatisch:
- `target/surefire-reports/` – JUnit-Testergebnisse (TXT + XML)
- `target/site/jacoco/index.html` – Coverage-Report (HTML, sichtbar/klickbar)

Coverage-Ergebnis: Controller **100 %**, `RecipeEntityMapperImpl` **95 %**, `IngredientEntityMapperImpl` **71 %** (jeweils Instruction Coverage) – genau die Klassen, die laut Aufgabe 1 getestet werden sollten. Gesamt über alle Klassen ca. 44 %, niedriger gezogen durch die vielen von Lombok generierten, hier nicht direkt getesteten Methoden (`equals`/`hashCode`/`toString`) auf den Domain-/Entity-Klassen sowie den ungetesteten `RecipeService` – beides ausserhalb des Scopes von Aufgabe 1.

## Aufgabe 3 – Pipeline

[.github/workflows/recipe-planner-backend-ci.yml](../../.github/workflows/recipe-planner-backend-ci.yml) – GitHub Actions statt GitLab CI (Aufgabenstellung erlaubt beides, das Repo liegt auf GitHub). Drei Stages wie in der Theorie:

1. **build** – `mvn compile`
2. **test** – `mvn test` (Surefire + JaCoCo), JUnit-Ergebnisse werden zusätzlich direkt als Check-Run auf dem Commit/PR sichtbar gemacht (`mikepenz/action-junit-report`)
3. **deploy** – bestätigt, dass die Reports (Surefire + JaCoCo) als herunterladbares Workflow-Artefakt `test-and-coverage-report` bereitstehen

Trigger: jeder Push/PR, der etwas unter `recipe-planner-backend/` ändert (Pfad-Filter, damit nicht jede Änderung an den anderen Modul-Ordnern im selben Repo unnötig die Pipeline auslöst), plus manueller Trigger (`workflow_dispatch`).

**Live auf GitHub verifiziert** (nach dem Push, [Lauf #1](https://github.com/Joel-Bandle/Joel-M450/actions/runs/35722642226)): `build` und `test` liefen beim ersten Versuch grün (9/9 Tests, JaCoCo- und Surefire-Reports, JUnit-Check-Run alle erfolgreich), `deploy` schlug fehl. Ursache: `deploy` hat keinen `actions/checkout`-Schritt, der Workflow-weite `defaults.run.working-directory` (der auf den Backend-Unterordner zeigt) existiert dort also gar nicht – jeder Job läuft auf einer eigenen, leeren VM, Dateien aus vorherigen Jobs sind nicht automatisch da. Behoben, indem `deploy` den `working-directory`-Default lokal auf das Workspace-Root zurücksetzt. Noch nicht erneut auf GitHub verifiziert, da das die nächste Push-abhängige Pipeline-Ausführung ist.
