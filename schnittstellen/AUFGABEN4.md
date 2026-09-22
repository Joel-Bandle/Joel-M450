# Lösungen zu den Übungen "Schnittstellen" (Test Doubles)

Vorgabe: [addressbook-backend/](addressbook-backend/) (aus `addressbook-backend-v1-1.zip`, Spring Boot 3.5.4 / Java 21, unverändert übernommen bis auf `AddressComparator`).

## Aufgabe 1 – Tests für alle Klassen + Service ohne echte DB testen

| Testklasse | Was getestet wird | Test-Double-Art |
|---|---|---|
| [AddressTest](addressbook-backend/src/test/java/ch/tbz/m450/repository/AddressTest.java) | Entity: Konstruktoren, Getter/Setter | keine (reines POJO) |
| [AddressComparatorTest](addressbook-backend/src/test/java/ch/tbz/m450/util/AddressComparatorTest.java) | Sortierlogik | keine |
| [AddressServiceTest](addressbook-backend/src/test/java/ch/tbz/m450/service/AddressServiceTest.java) | `AddressService` | `AddressRepository` **gemockt** (Mockito) |
| [AddressControllerTest](addressbook-backend/src/test/java/ch/tbz/m450/controller/AddressControllerTest.java) | `AddressController` | `AddressService` **gemockt** (`@MockitoBean` + MockMvc) |

**H2 wegmocken:** `AddressServiceTest` nutzt `@Mock AddressRepository` + `@InjectMocks AddressService` (Mockito) – es findet nie ein echter Datenbankzugriff statt. Zwei Testarten drin, passend zur Theorie:
- **Stub-artig** (Zustand prüfen): `when(repository.findById(1)).thenReturn(...)`, dann das Ergebnis von `addressService.getAddress(1)` prüfen.
- **Mock-artig** (Verhalten prüfen): `verify(addressRepository).save(mueller)` – war der Aufruf am Repository korrekt?

**`AddressComparator` korrigiert:** war `return -1;` (immer "kleiner", keine echte Sortierung). Jetzt: sortiert nach Nachname, bei Gleichstand nach Vorname (`compareTo` auf den jeweiligen Strings).

## Aufgabe 2 – Comparator um weitere Attribute erweitern

`AddressComparator` nimmt jetzt beliebig viele `SortField`s im Konstruktor entgegen (`LASTNAME`, `FIRSTNAME`, `PHONENUMBER`, `REGISTRATION_DATE`), die der Reihe nach angewendet werden:

```java
new AddressComparator()                                    // Standard: Nachname, dann Vorname
new AddressComparator(SortField.REGISTRATION_DATE)          // nach Registrierungsdatum
new AddressComparator(SortField.FIRSTNAME, SortField.LASTNAME) // Vorname, dann Nachname
```

Getestet in `AddressComparatorTest`: Sortierung nach `REGISTRATION_DATE`, nach `PHONENUMBER`, und Kombination mehrerer Kriterien.

## Ausführen

```bash
cd schnittstellen/addressbook-backend
mvn test              # alle 19 Tests, alle grün
mvn spring-boot:run   # App starten (H2 In-Memory-DB, Port 8080)
```

App manuell geprüft: `POST /address`, `GET /address`, `GET /address/{id}` (200 bei Treffer, 404 wenn nicht vorhanden) – alles funktioniert wie erwartet.
