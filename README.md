# HTWahl-O-Mat - Backend

Spring-Boot-Backend für den HTWahl-O-Mat.  
Stellt REST-Endpunkte für Authentifizierung (JWT), Rollen (Admin/Kandidat:in) und die Wahl-/Matching-Funktionen bereit.

## Tech
- Spring Boot 3.5.6
- Java 21
- PostgreSQL
- Maven Wrapper (mvnw)

## Voraussetzungen
- Java 21
- PostgreSQL-Datenbank

## Konfiguration (lokal)
Setze vor dem Start die folgenden Umgebungsvariablen. Ersetze die Platzhalter durch deine eigenen Werte:

macOS/Linux:
```bash
export DB_URL="jdbc:postgresql://localhost:5432/<DBNAME>"
export DB_USERNAME="<USER>"
export DB_PASSWORD="<PASSWORD>"
export ADMIN_PASSWORD="<ADMIN_PASSWORD>"
