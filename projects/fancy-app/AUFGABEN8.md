# Lösung zur Übung "Test Driven Development"

Vorgabe: [FancyApp.zip](https://gitlab.com/ch-tbz-it/Stud/m450/m450/-/blob/main/Unterlagen/x_gitres/FancyApp.zip) – [Application/MyFancyClass.php](Application/MyFancyClass.php) (Methoden-Bodies leer) + [Testcases/Test.php](Testcases/Test.php) (fertige PHPUnit-Tests).

## Aufgabe 1 – Setup

Kein Composer nötig: PHPUnit als eigenständige `.phar`-Datei verwendet (offizieller Weg ohne Projekt-Abhängigkeiten, siehe auch der in der Aufgabenstellung verlinkte Anfänger-Guide).

```bash
curl -L -o tools/phpunit.phar https://phar.phpunit.de/phpunit-10.5.phar
cd Testcases
php ../tools/phpunit.phar Test.php
```

`Test.php` benutzt die alte Klasse `PHPUnit_Framework_TestCase`, die es seit PHPUnit 6 nicht mehr gibt – auf die im Kommentar bereits vorgesehene moderne Variante umgestellt (`use PHPUnit\Framework\TestCase; ... class Test extends TestCase`).

**Vorher (unverändertes Gerüst) wirklich ausgeführt, wie in der Aufgabenstellung verlangt:** alle 3 Tests rot, genau wie beschrieben:
```
FFF
1) Test::testShortString    Failed asserting that null matches expected '...'
2) Test::testCalcAverage    Failed asserting that null matches expected 5.
3) Test::testGetOpposite    Failed asserting that null matches expected '4.5,5,5.5'.
```

## Aufgabe 2 – Methoden implementieren

Das Verhalten aller drei Methoden wurde ausschliesslich aus den Assertions in `Test.php` abgeleitet (genau der Sinn von TDD: die Tests sind die Spezifikation).

**`shortString($text, $length, $ending = '...')`**
- Ist `$ending` selbst schon länger als `$length`, ist keine gültige Kürzung möglich → `false`.
- Passt `$text` bereits in `$length`, wird er unverändert zurückgegeben.
- Sonst: die ersten `$length - strlen($ending)` Zeichen von `$text`, gefolgt von `$ending`.

**`calcAverage($values)`**
- Leeres Array → `0` (kein Fehler/Division durch 0).
- Enthält irgendein nicht-numerisches Element → `false`.
- Sonst: normaler Durchschnitt (Summe / Anzahl).

**`getOpposite($value, $delimiter = ',')`** – macht buchstäblich das Gegenteil, je nach Eingabetyp:
- Array → `implode($delimiter, $value)`
- String → `explode($delimiter, $value)`
- Alles andere (z.B. ein Objekt) → `false`

Dadurch ist `getOpposite(getOpposite($x))` für Strings ein Round-Trip zurück zum Original – genau das testen mehrere Assertions.

**Echter Stolperstein beim Grün-Bekommen:** `shortString` schlug bei genau einem Testfall fehl (`$text35Char` mit einem Umlaut "ä" gegen Länge 35). Grund: PHPs `strlen()`/`substr()` zählen **Bytes**, nicht Zeichen – ein Umlaut ist in UTF-8 2 Bytes, wodurch der eigentlich 35 Zeichen lange Text als 36 "lang" galt und die Kürzungslogik danebengriff. Behoben mit `mb_strlen()`/`mb_substr()` (multibyte-fähig, zählt echte Zeichen). Ein gutes Beispiel dafür, wieso Tests mit Umlauten/Sonderzeichen im Datenmaterial solche Bugs überhaupt erst sichtbar machen.

**Ergebnis:** `php tools/phpunit.phar Testcases/Test.php` → **OK (3 tests, 27 assertions)**.

## Aufgabe 3

Laut Rücksprache nicht nötig – kein Vergleich mit anderen Gruppen.
