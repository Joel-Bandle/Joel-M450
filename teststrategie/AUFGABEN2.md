# Lösungen zu den Übungen "Teststrategie"

## Übung 1 – Testfälle aus der Rabattregel ableiten

Regel: `p < 15'000` → 0 % | `15'000 ≤ p ≤ 20'000` → 5 % | `20'000 < p < 25'000` → 7 % | `p ≥ 25'000` → 8,5 %

### Abstrakte Testfälle

| Testfall | Eingabe (Kaufpreis p)                    | Erwarteter Rabatt |
|----------|-------------------------------------------|--------------------|
| A1       | p < 15'000                                 | 0 %                |
| A2       | 15'000 ≤ p ≤ 20'000                        | 5 %                |
| A3       | 20'000 < p < 25'000                        | 7 %                |
| A4       | p ≥ 25'000                                 | 8,5 %              |
| A5       | p = 15'000 (Grenze)                        | 5 %                |
| A6       | p = 20'000 (Grenze)                        | 5 %                |
| A7       | p = 25'000 (Grenze)                        | 8,5 %              |
| A8       | p < 0 (ungültig)                           | Fehlermeldung      |

### Konkrete Testfälle

| Testfall | Kaufpreis (CHF) | Erwarteter Rabatt | Rabattbetrag (CHF) | Preis nach Rabatt (CHF) |
|----------|-----------------|--------------------|---------------------|---------------------------|
| K1       | 12'000          | 0 %                | 0.00                | 12'000.00                 |
| K2       | 15'000          | 5 %                | 750.00              | 14'250.00                 |
| K3       | 20'000          | 5 %                | 1'000.00            | 19'000.00                 |
| K4       | 20'000.01       | 7 %                | 1'400.00            | 18'600.01                 |
| K5       | 24'999          | 7 %                | 1'749.93            | 23'249.07                 |
| K6       | 25'000          | 8,5 %              | 2'125.00            | 22'875.00                 |
| K7       | 40'000          | 8,5 %              | 3'400.00            | 36'600.00                 |

## Übung 2 – Funktionale Black-Box-Tests für eine Autovermietung

Website: [sixt.ch](https://www.sixt.ch/mietwagen/schweiz/)

| ID | Beschreibung | Erwartetes Resultat | Effektives Resultat | Status | Mögliche Ursache |
|----|--------------|----------------------|------------------------|--------|--------------------|
| 1 | Startseite aufrufen | Formular mit Ort, Datum, Fahreralter, sinnvoll vorbefüllt | Alles korrekt vorbefüllt (Datum, Alter "30+") | Success | – |
| 2 | "Autos anzeigen" ohne Abholort | Fehlermeldung Pflichtfeld | Feld rot, Meldung "Bitte wählen Sie einen Abholort aus" | Success | – |
| 3 | "Jetzt buchen" bei Beispielauto, ohne eigene Suche | Hinweis oder sinnvolle Weiterleitung | Weiterleitung zu echter Angebotsliste mit Standardwerten | Success | – |
| 4 | Bei Stadt "Zürich" auf "Stationen anzeigen" | Liste der Stationen mit Adressen | Keine Stationsliste, startet direkt Fahrzeugsuche | Failed | Beschriftung irreführend |
| 5 | "Alter des Fahrers" ändern (z.B. 25) und suchen | Auswahl wird übernommen | Ergebnisliste zeigt Filter "Alter: 25" | Success | – |

## Übung 3 – Testfälle für die Bank-Software

Applikation lokal kompiliert (`javac`/`java`) und effektiv am Menü getestet. `ExchangeRateOkhttp` (okhttp/gson) offline durch Stub ersetzt, da keine Internetverbindung/JARs verfügbar – restlicher Code unverändert, Wechselkurs-Feature deshalb nicht getestet.

| ID | Test-Art | Beschreibung | Erwartetes Resultat | Effektives Resultat | Status | Mögliche Ursache |
|----|----------|--------------|----------------------|------------------------|--------|--------------------|
| B1 | Blackbox | "$" im Hauptmenü | Fehlermeldung, Menü erneut | Meldung erscheint korrekt | Success | – |
| B2 | Blackbox | "12a" im Hauptmenü | Fehlermeldung | Keine Meldung, Menü einfach erneut | Failed | Nur führende Ziffer geprüft, Rest ignoriert |
| B3 | Blackbox | Negativ einzahlen ("-50") | Fehlermeldung, kein Effekt | Wird verrechnet: 1500 → 1450 USD, keine Meldung | Failed | Kein Vorzeichen-Check in `deposit()` |
| B4 | Blackbox | Mehr abheben als vorhanden | Fehlermeldung | "Kontostand zu niedrig (1450.0 USD)" | Success | – |
| B5 | Blackbox | Überweisung aufs eigene Konto | Fehlermeldung | "Bitte ein anderes Konto auswählen!" | Success | – |
| B6 | Blackbox | Überweisung auf Konto 999 (existiert nicht) | Fehlermeldung | "Konto nicht vorhanden!" | Success | – |
| B7 | Blackbox | Überweisung über Guthaben hinaus | Fehlermeldung, erneute Eingabe | "Kontostand zu niedrig! (1350.0 USD)" | Success | – |
| B8 | Blackbox | Überweisung USD → EUR, 50 USD | Kursumrechnung | Gates: 2000 → 2045.50 EUR (50 × 0.91) | Success | – |
| B9 | Blackbox | Konto mit "chf" (klein) anlegen | Normalisierung oder Meldung | Wird korrekt zu "CHF" | Success | – |
| B10 | Blackbox | Konto löschen, "j" bestätigen | Konto entfernt | "Konto Nr. 6 gelöscht", nicht mehr gelistet | Success | – |
| B11 | Blackbox | Shortcut "ü" ohne UTF-8-Konsole | Transfer startet | Wird nicht erkannt, "Ungültige Eingabe" | Failed (umgebungsabh.) | Nicht-ASCII-Shortcut, encoding-abhängig |
| B12 | Blackbox | Kontonummer 999 im Hauptmenü | Fehlermeldung | "Konto nicht vorhanden!" | Success | – |

**Mögliche White-Box-Testfälle:**
`Account.withdraw()` Grenzfall `amount == balance` · `Account.deposit()` fehlender Vorzeichen-Check (siehe B3) · `Bank.getAccount()` Rückgabe `null` bei unbekanntem Konto · Regex-Prüfungen in `Counter` (Menü, Kontonummer, Währungscode) je 1 gültiger/ungültiger Fall · `convertCurrency()` nur 3 von 6 Kombinationen hinterlegt

**Änderungen:** Regex fürs Hauptmenü verankern (statt `find()`), Vorzeichen bei Ein-/Auszahlung prüfen, Menü-Shortcuts auf ASCII beschränken oder UTF-8 fix setzen, `convertCurrency()` um fehlende Kombinationen ergänzen, `AccountExeption` zu `AccountException` korrigieren, API-Key auslagern, Geschäftslogik von Konsolen-I/O trennen.
