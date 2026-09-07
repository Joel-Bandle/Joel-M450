## Aufgabe 1 – Simpler Rechner

Maven-Projekt: [calculator/](calculator/) · Klasse: [Calculator.java](calculator/src/main/java/calculator/Calculator.java) · Tests: [CalculatorTest.java](calculator/src/test/java/calculator/CalculatorTest.java)

13 Tests für `add`, `subtract`, `multiply`, `divide` (inkl. Grenzfälle: negative Zahlen, Multiplikation mit 0, Division durch 0 wirft `ArithmeticException`).

Ausführen mit Maven auf der Kommandozeile:

```bash
cd test-levels-unit-testing/calculator
mvn test
```

Ergebnis: `Tests run: 13, Failures: 0, Errors: 0, Skipped: 0` – `BUILD SUCCESS`.

In der IDE (IntelliJ/Eclipse): Rechtsklick auf `CalculatorTest.java` → "Run Tests", oder grüner Pfeil links neben der Klasse/den einzelnen `@Test`-Methoden.

## Aufgabe 2 – JUnit Zusammenfassung

Referenz: [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)

| Feature | Zweck | Beispiel |
|---|---|---|
| `@Test` | Markiert eine Methode als Testfall | `@Test void testAdd() { ... }` |
| `@BeforeEach` / `@AfterEach` | Läuft vor/nach **jedem** Test – z.B. für ein frisches Testobjekt | `@BeforeEach void setUp() { calc = new Calculator(); }` |
| `@BeforeAll` / `@AfterAll` | Läuft **einmal** vor/nach allen Tests der Klasse (muss `static` sein) – z.B. teure Ressourcen wie eine DB-Verbindung | `@BeforeAll static void init() { ... }` |
| `@DisplayName` | Lesbarer Name für Testberichte statt Methodenname | `@DisplayName("Division durch Null wirft Exception")` |
| `@ParameterizedTest` + `@CsvSource`/`@ValueSource` | Ein Test läuft mehrfach mit verschiedenen Eingaben, statt viele fast identische `@Test`-Methoden zu schreiben | `@CsvSource({"2,3,5", "-2,-3,-5"})` |
| `Assertions` (`assertEquals`, `assertTrue`, `assertThrows`, `assertAll`) | Prüft das eigentliche Testergebnis | `assertThrows(ArithmeticException.class, () -> calc.divide(5,0));` |
| `@Disabled` | Test temporär überspringen, z.B. bei bekanntem, noch nicht behobenem Bug | `@Disabled("Bug #42 noch offen")` |
| `@Nested` | Gruppiert zusammengehörige Tests in inneren Klassen, für strukturierte Testberichte | `@Nested class WennKontostandNull { ... }` |

## Aufgabe 3 – Banken Simulation dokumentieren

Vorgabe: [bank/](bank/) (aus `02_bank-vorgabe.zip`, Spring-Boot-Overhead entfernt – Code brauchte kein Spring, nur Maven + JUnit 5). Klassendiagramm: [Design/bank6_klassendiagramm.png](bank/Design/bank6_klassendiagramm.png).

**Aufbau:**
- `Bank` verwaltet alle Konten in einer `TreeMap<String, Account>`, Kontonummer = Präfix + fortlaufende Nummer (`S-`, `Y-`, `P-`)
- `Account` (abstrakt): Basisklasse für alle Kontotypen – `id`, `balance` (in Millirappen), Liste von `Booking`
- `SavingsAccount` (Sparkonto): überschreibt `withdraw()` – kein Abheben über den Kontostand hinaus
- `SalaryAccount` (Lohnkonto): überschreibt `withdraw()` – Abheben bis zu einer Kreditlimite erlaubt (Saldo darf negativ werden)
- `PromoYouthSavingsAccount` (Jugendsparkonto): erbt von `SavingsAccount`, überschreibt `deposit()` – 1 % Bonus auf jede Einzahlung
- `Booking`: einzelne Buchung (Datum, Betrag), entsteht bei jeder Ein-/Auszahlung
- `BankUtils`: statische Formatierung für Datum (360 Banktage/Jahr, 30/Monat) und Beträge
- `AccountBalanceComparator` / `AccountInverseBalanceComparator`: sortieren Konten nach Saldo, für `printTop5()`/`printBottom5()`
- `canTransact(date)`: Transaktionen müssen chronologisch sein (Datum ≥ letzte Buchung), sonst wird die Ein-/Auszahlung verweigert

**Zusammenhänge (siehe Klassendiagramm):** Vererbung `Account → SavingsAccount → PromoYouthSavingsAccount` und `Account → SalaryAccount`. Aggregation `Bank ◇→ Account` (0..*) und `Account ◇→ Booking` (0..*). `BankUtils` wird von `Booking.print()` für die Formatierung genutzt.

**Auffälligkeiten beim Studieren des Codes:**
- `Bank.getBalance()` rechnet `balance -= account.getBalance()` statt `+=` → liefert die **negative** Summe statt der Summe aller Kontostände (vermutlich ein Bug, siehe Test in Aufgabe 4)
- `Account.booking`/`getBooking()`/`setBooking()`: unbenutztes Altlast-Feld, unabhängig von der eigentlichen `bookings`-Liste
- `Main.main()` enthält eine offene Kommentarfrage ("Wie verhindert man mehrere Bank-Objekte?") ohne Umsetzung – Antwort wäre das Singleton-Pattern

## Aufgabe 4 – Unit-Tests implementieren

Tests: [bank/src/test/java/ch/schule/](bank/src/test/java/ch/schule/) – 32 Tests über 7 Testklassen (`AccountTest`, `BankTest`, `BankUtilsTest`, `BookingTest`, `PromoYouthSavingsAccountTest`, `SalaryAccountTest`, `SavingsAccountTest`), alle grün.

Ausführen mit Coverage-Report (JaCoCo, in `pom.xml` ergänzt):

```bash
cd test-levels-unit-testing/bank
mvn test
```

Coverage-Report danach unter `target/site/jacoco/index.html`.

**Ergebnis:** 81,6 % Instruction Coverage / 78,8 % Line Coverage insgesamt. `Main` bewusst ungetestet gelassen (reine manuelle Demo ohne prüfbares Verhalten – ohne `Main` liegt die Line Coverage bei ca. 83 %).

Ein Test (`gesamtsaldoDerBankIstDieNegativeSummeAllerKonten`) dokumentiert bewusst das aktuelle, vermutlich fehlerhafte Verhalten von `Bank.getBalance()` (siehe Aufgabe 3), statt es stillschweigend zu "reparieren".
