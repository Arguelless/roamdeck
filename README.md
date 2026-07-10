# RoamDeck

[![CI](https://github.com/Arguelless/roamdeck/actions/workflows/ci.yml/badge.svg)](https://github.com/Arguelless/roamdeck/actions/workflows/ci.yml)

AI-powered travel itinerary generator. The user provides a destination, dates, budget and preferences, and the application returns a day-by-day itinerary.

The project is built on a **hexagonal architecture (ports and adapters)**, with a strong focus on keeping the domain isolated from infrastructure. Itinerary generation relies on a **port** with three interchangeable implementations selected by Spring profile: a placeholder for development, a local LLM via Ollama, and DeepSeek in the cloud.

## Stack

**Backend**
- Java 21
- Spring Boot 3.5 (Web, Data JPA, Security, Validation)
- PostgreSQL 16
- Maven
- JUnit 5 + AssertJ

**Frontend**
- Angular 21
- TypeScript 5.9
- RxJS
- Tailwind CSS 4

## Architecture

The backend follows a strict three-layer separation, with dependencies always pointing inward toward the domain:

```
infrastructure  -->  application  -->  domain
```

- **`domain/`** — Pure business model (`Itinerary`, `ItineraryDay`, `Activity`, `TripRequest`). Immutable records, no framework dependencies.
- **`application/`** — Use cases (`ItineraryService`) and ports (`ItineraryGenerator`). Orchestrates the domain and declares what it needs from the outside world through interfaces.
- **`infrastructure/`** — Concrete adapters: REST controllers, port implementations (AI generators), persistence, security and exception handling.

### Interchangeable AI backends

The `ItineraryGenerator` port has three implementations, selected by Spring profile:

| Backend | Profile | Description |
|---------|---------|-------------|
| Placeholder | `dev` | Produces a fake itinerary. Lets you exercise the full flow without depending on an LLM. |
| Ollama | `local` | Local LLM (defaults to `qwen3:14b`), no cost or API key required. |
| DeepSeek | `deepseek` | Cloud LLM with strict tool calling (enforced JSON Schema). |

Only one AI profile should be active at a time.

## Getting started

### Prerequisites
- Java 21
- Node.js + npm
- Docker (for PostgreSQL)

### 1. Database

```bash
docker compose up -d
```

Starts PostgreSQL 16 on port `5432` (database `roamdeck`, user `admin`).

### 2. Backend

From `backend/`, pick the AI backend through the profile:

```bash
# Placeholder (default)
./mvnw spring-boot:run

# Local Ollama (requires Ollama running on localhost:11434)
./mvnw spring-boot:run "-Dspring-boot.run.profiles=local"

# DeepSeek (requires the DEEPSEEK_API_KEY environment variable)
./mvnw spring-boot:run "-Dspring-boot.run.profiles=deepseek"
```

The backend starts on `http://localhost:8080`.

### 3. Frontend

From `frontend/`:

```bash
npm install
npm start
```

The frontend starts on `http://localhost:4200` and proxies `/api` calls to the backend via `proxy.conf.json`.

## API

### Generate itinerary

```
POST /api/itineraries/generate
```

**Request**

```json
{
  "destination": "Roma",
  "startDate": "2026-08-01",
  "endDate": "2026-08-05",
  "budget": "1000",
  "preferences": "arte, comida local"
}
```

**Response** `201 Created`

```json
{
  "days": [
    {
      "date": "2026-08-01",
      "activities": [
        { "timeOfDay": "Mañana", "description": "Visitar el Coliseo" }
      ]
    }
  ]
}
```

Invalid requests (malformed dates, non-numeric budget, or `endDate` before `startDate`) are rejected with a uniform error response.

## Tests

```bash
cd backend
./mvnw test
```

The `ItineraryService` tests use a fake of the `ItineraryGenerator` port (a lambda), with no Spring or network, covering the DTO-to-domain mapping and the validation rules.

## Project structure

```
roamdeck/
├── backend/           # Spring Boot API (hexagonal architecture)
│   └── src/main/java/com/roamdeck/backend/
│       ├── domain/            # Business model
│       ├── application/       # Use cases and ports
│       └── infrastructure/    # Controllers, AI adapters, persistence
├── frontend/          # Angular SPA
│   └── src/app/features/itinerary/
└── docker-compose.yml # PostgreSQL
```

## License

Distributed under the MIT License. See the [LICENSE](LICENSE) file for details.
