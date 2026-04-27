# Customer Management Frontend

React single-page frontend for the Customer Management System.

For full-stack setup (frontend + backend + MariaDB), see the root README:

- ../README.md

## Requirements

- Node.js 18+ and npm

## Running locally (development)

The frontend calls the backend through an API base URL.

By default, the app uses `/api` (see `src/services/api.js`). In local development, set it to the backend URL.

From `frontend/`:

```bash
npm install
REACT_APP_API_BASE_URL=http://localhost:8080/api npm start
```

Then open http://localhost:3000.

Alternatively, create `frontend/.env`:

```env
REACT_APP_API_BASE_URL=http://localhost:8080/api
```

## Running with Docker Compose (recommended)

When started via Docker Compose from the repository root, the frontend is served by Nginx and `/api` is proxied to the backend container.

From the repository root:

```bash
docker compose up -d --build
```

Then open http://localhost:3000.

## Scripts

From `frontend/`:

- `npm start`: Start the development server
- `npm test`: Run tests
- `npm run build`: Create a production build
