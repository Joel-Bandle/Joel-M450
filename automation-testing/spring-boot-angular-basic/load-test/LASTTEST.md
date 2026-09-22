# Übung 3 – Lasttest der REST-Schnittstelle

**Tools:** [Postman](https://www.postman.com/) (Collection erkundet/erstellt, importierbar), [Newman](https://www.npmjs.com/package/newman) (Postmans eigene CLI, zum funktionalen Validieren der Collection), [autocannon](https://www.npmjs.com/package/autocannon) (für den eigentlichen Lasttest mit echter Nebenläufigkeit).

## Warum nicht nur Postman/Newman für die Last?

Newman spielt eine Collection **sequenziell** ab (eine Anfrage nach der anderen) – gut zum Validieren, aber kein echter Lasttest mit vielen gleichzeitigen Nutzern. Für die eigentliche Belastung mit "grösserem Traffic" (mehrere parallele Verbindungen gleichzeitig) haben wir deshalb `autocannon` verwendet – ein leichtgewichtiges, npm-installierbares Tool, das genau das kann (vergleichbar mit JMeter, aber ohne grosse GUI-Installation). Die Postman-Collection ([students-api.postman_collection.json](students-api.postman_collection.json)) bleibt trotzdem Teil der Lösung – sie lässt sich in Postman importieren, dort erkunden und mit dem eingebauten Collection Runner ebenfalls manuell laufen lassen.

## Postman-Collection erkundet

Die Collection enthält drei Requests mit eingebauten Tests (`pm.test(...)`):
- `GET /students` – prüft Status 200, Antwortzeit < 500 ms, Antwort ist ein Array
- `POST /students` (gültig) – prüft Status 200
- `POST /students` (ungültig) – prüft Status 400 (testet das Bonus-Feature Validierung)

Funktional validiert mit `npx newman run students-api.postman_collection.json`: **5/5 Assertions grün**, durchschnittliche Antwortzeit 18 ms.

## Lasttest-Ergebnisse (autocannon)

| Test | Verbindungen | Dauer | Requests gesamt | Req/Sek (Ø) | Latenz (Ø / 99%) | Fehler |
|---|---|---|---|---|---|---|
| GET /students | 10 | 10 s | 127'000 | 11'447 | 50 ms / 155 ms | 271 (0,21 %) |
| GET /students | 20 | 10 s | 176'000 | 15'825 | 52 ms / 196 ms | 595 (0,34 %) |
| POST /students | 10 | 5 s | 44'000 | 8'679 | 21 ms / 126 ms | 114 (0,26 %) |

Alle tatsächlich beantworteten Requests kamen mit Status 200 zurück (siehe `--renderStatusCodes`) – die "Fehler" in der Tabelle sind **keine** HTTP-Fehlerstatus, sondern abgebrochene/zurückgesetzte Verbindungen auf TCP-Ebene. Das deutet darauf hin, dass der eingebettete Tomcat mit seiner Standard-Konfiguration (Thread-/Connection-Pool) bei sehr hohem Durchsatz (>10'000 Req/Sek) an eine Kapazitätsgrenze stösst – genau das, wofür Stress-/Lasttests da sind: das Limit der Software kennenzulernen. Bei 20 statt 10 Verbindungen steigt sowohl der Durchsatz als auch die Fehlerrate leicht an.

**Hinweis:** Nach dem Lasttest wurde das Backend neu gestartet, um die während der POST-Last erzeugten Test-Datensätze wieder zu entfernen (In-Memory-H2 wird bei jedem Start neu geseedet).

## Ausführen

```bash
cd automation-testing/spring-boot-angular-basic/load-test
npm install
npx newman run students-api.postman_collection.json          # funktionale Validierung
npx autocannon -c 20 -d 10 --renderStatusCodes http://localhost:8081/students   # Lasttest
```

Voraussetzung: Backend läuft (`mvn spring-boot:run` im Projekt-Hauptordner, Port 8081).
