# RoamDeck — Frontend

Angular 21 single-page app for RoamDeck. It lets the user submit a trip request (destination, dates, budget, preferences) and displays the generated day-by-day itinerary returned by the backend.

For the full project overview, architecture and stack, see the [root README](../README.md).

## Structure

The app is organized by feature. The itinerary feature keeps its concerns separated:

```
src/app/features/itinerary/
├── data-access/     # itinerary-api.service.ts (HTTP access)
├── models/          # request/response typings
└── itinerary-page/  # page component
```

## Development

```bash
npm install
npm start        # ng serve — runs on http://localhost:4200
```

API calls to `/api` are proxied to the backend at `http://localhost:8080` via `proxy.conf.json`, so the backend must be running (see the root README).

## Build and test

```bash
npm run build    # production build into dist/
npm test         # unit tests (Vitest)
```
