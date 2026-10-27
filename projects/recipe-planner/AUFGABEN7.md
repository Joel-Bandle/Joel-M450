# Lösungen zu den Übungen "Code Review" (Features umgesetzt)

Gleiches Projekt wie bei Aufgabe 6 ([recipe-planner/](.)). Umgesetzt wurden die beiden Features; Pull Requests, gegenseitiges Review und der Vergleich der Erkenntnisse waren laut Rücksprache nicht Teil der Abgabe.

## Aufgabe 1 – Rezept hinzufügen (Frontend)

Das "Add Recipe"-Formular war nur ein UI-Gerüst ohne Funktion. Jetzt:
- [AddRecipe.js](recipe-planner-frontend/src/components/AddRecipe/AddRecipe.js): Formular über `react-hook-form` angebunden (Name und Beschreibung Pflicht), POST an `/api/recipes`, danach Weiterleitung zur Rezeptliste.
- [AddIngredient.js](recipe-planner-frontend/src/components/AddIngredient/AddIngredient.js): Zutatenzeilen (Name, Einheit, Menge) schreiben jetzt wirklich in den State.

Zwei Fallen unterwegs:
- Die Buttons "Add Ingredient" und "x" brauchen `type="button"`, sonst schicken sie im neuen `<Form>` das ganze Formular ab.
- Eine kaputte ESLint-Konfiguration (`react-app/jest` in der `package.json`) legte beim Start ein unsichtbares Fehler-Overlay über die Seite, das alle Klicks blockierte. Entfernt (im Projekt gibt es keine Jest-Tests, die es brauchen).

## Aufgabe 2 – Rezept bearbeiten (Frontend und Backend)

**Backend**
- `PUT /api/recipes/{recipeId}` im [RecipeController](recipe-planner-backend/src/main/java/ch/tbz/recipe/planner/controller/RecipeController.java): 200 mit dem aktualisierten Rezept, 404 wenn es die ID nicht gibt.
- `updateRecipe` im [RecipeService](recipe-planner-backend/src/main/java/ch/tbz/recipe/planner/service/RecipeService.java): prüft, ob das Rezept existiert, setzt die ID aus dem Pfad und speichert. Zutaten, die im Formular entfernt wurden, werden dank `orphanRemoval` aus der Datenbank gelöscht.
- Tests: 2 neue Controller-Tests (200, 404) und neu [RecipeServiceTest](recipe-planner-backend/src/test/java/ch/tbz/recipe/planner/service/RecipeServiceTest.java) (Mockito). Gesamt **13/13 grün**.

**Frontend**
- "Edit Details" auf der Rezeptkarte führt jetzt zu `/edit/<id>` ([Recipe.js](recipe-planner-frontend/src/components/Recipe/Recipe.js), neue Route in `App.js`).
- Dasselbe Formular `AddRecipe` wird wiederverwendet (DRY): mit `:id` in der URL lädt es das Rezept, füllt alle Felder und Zutaten vor und speichert per PUT; ohne `:id` ist es das leere "Add Recipe".
- Zutaten behalten beim Speichern ihre ID und ihren Kommentar, damit sie nicht gelöscht und neu angelegt werden.

Zwei weitere Funde:
- Die Navigationslinks (`href="planer"`) waren relativ und führten von `/edit/<id>` aus ins Leere – auf absolute Pfade umgestellt.
- `Browse.js` hatte kein `key` auf den Listenelementen (React-Warnung) – ergänzt.

Im Browser durchgespielt: Name und Menge geändert, zweite Zutat hinzugefügt, gespeichert → Rezeptliste zeigt den neuen Namen, per API bestätigt (Kommentar der alten Zutat erhalten, weiterhin 15 Rezepte statt eines Duplikats). `PUT` auf eine unbekannte ID gibt 404.
