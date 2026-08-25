## Aufgabe 1 – Testformen aus der Praxis

Drei Testarten, die uns aus eigenen Projekten bekannt sind:

* **Unit-Tests** – testen einzelne Methoden bzw. Klassen isoliert von der restlichen
  Applikation. Werden mit Frameworks wie JUnit (Java) oder xUnit/NUnit (C#/.NET)
  geschrieben und laufen bei jedem Build automatisch mit.
* **Integrationstests** – prüfen, ob mehrere Komponenten korrekt zusammenspielen,
  z.B. ob das Backend über die REST-API die erwarteten Daten liefert. Solche Tests
  lassen sich manuell mit Tools wie Postman oder Bruno auslösen, oder automatisiert
  in eine Test-Suite einbinden.
* **End-to-End-Tests (Systemtests)** – testen die Applikation aus Sicht der
  Benutzerin/des Benutzers, also über die komplette Kette Frontend → Backend →
  Datenbank. Wird z.B. mit Cypress oder Playwright umgesetzt, wobei ein echter
  Browser automatisiert gesteuert wird.

**Durchführung:** Unit- und Integrationstests laufen bei uns automatisiert in der
CI/CD-Pipeline bei jedem Push bzw. Pull Request. End-to-End-Tests werden zusätzlich
manuell vor einem Release nochmals stichprobenartig durchgeklickt.

## Aufgabe 2 – Fehler vs. Mangel

* **SW-Fehler:** Ein Formular akzeptiert beim Datum den 31.02.2026, obwohl dieses
  Datum gar nicht existiert das IST-Verhalten weicht klar vom SOLL-Verhalten
  (Validierung gemäss Spezifikation) ab.
* **SW-Mangel:** Die Berechnung eines Totals ist korrekt, wird der Nutzerin aber
  ohne Tausendertrennzeichen und ohne Währungssymbol angezeigt die Anforderung
  "gut lesbare Darstellung" wird nicht angemessen erfüllt, obwohl die Logik stimmt.
* **Beispiel für hohen Schaden:** Der Handelsalgorithmus von Knight Capital (2012)
  enthielt einen Fehler, durch den in ca. 45 Minuten unkontrolliert Aktien gekauft
  und verkauft wurden. Das Unternehmen verlor dadurch rund 440 Millionen US-Dollar
  und musste kurz danach übernommen werden.

## Aufgabe 3 – Testtreiber

Umsetzung siehe [code/Preisrechner.cs](code/Preisrechner.cs) (Berechnung) und
[code/Program.cs](code/Program.cs) (Testtreiber, führt mehrere Testfälle aus und
vergleicht das Resultat mit einem von Hand berechneten Sollwert).

Ausführen mit:

```bash
dotnet run --project code
```

## Aufgabe 3 – Bonus: Der Fehler im Code

Im ursprünglichen Code werden die beiden Bedingungen in falscher Reihenfolge
geprüft:

```
if (extras >= 3)
    addon_discount = 10;
else if (extras >= 5)
    addon_discount = 15;
```

Da jede Zahl `>= 5` auch automatisch `>= 3` ist, greift bei fünf oder mehr
Zusatzausstattungen immer schon der erste Zweig. Der `else if`-Zweig mit den 15%
wird dadurch nie erreicht => toter Code. Kunden mit 5 oder mehr Extras erhalten so
fälschlicherweise nur 10% statt der vorgesehenen 15% Rabatt.

**Korrektur:** Die Bedingung mit dem grösseren Grenzwert muss zuerst geprüft
werden:

```
if (extras >= 5)
    addon_discount = 15;
else if (extras >= 3)
    addon_discount = 10;
```

Genau dieser Fall wird im Testtreiber mit dem Testfall `extras=5` abgedeckt: Mit
dem ursprünglichen (fehlerhaften) Code würde dieser Test fehlschlagen (erwartet
1575, berechnet 1600), mit der Korrektur ist er grün. Das zeigt gut, wieso man
Testfälle bewusst an den Grenzwerten (hier 3 und 5) platzieren sollte.
