# E-Commerce

For the architecture, module boundaries, domain decisions, API design, import flow, and checkout flow, see [Architecture.md](Architecture.md).

## Run locally

Docker Compose is the recommended way to run the full stack.

Right now values are harcoded for simplicity you will find them in UI or in configuration

Before starting it, create a local `.env` file from the template and set the two secrets. 

```bash
cp .env.example .env
```

Generate a JWT secret with `openssl rand -base64 32`, then set `JWT_SECRET` and a long
`BOOTSTRAP_ADMIN_PASSWORD` in `.env`. `BOOTSTRAP_ADMIN_EMAIL` defaults to
`admin@your-company.com`. The server creates that account once with `ADMIN` and `CUSTOMER`
roles; later startups do not overwrite its password or roles.

From the repository root, start the application with the H2 profile:

```bash
docker compose up --build
```

Once the containers are healthy:

- Client: [http://localhost:3000](http://localhost:3000)
- API: [http://localhost:8080](http://localhost:8080)
- Swagger UI: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

Stop the application with:

```bash
docker compose down
```

To run the same application with PostgreSQL instead of H2:

```bash
docker compose -f compose.yaml -f compose.postgres.yaml up --build
```

## Run without Docker

Install Java 21 and Node.js with npm. This mode uses the H2 profile, so PostgreSQL is not required.

Start the API in one terminal:

```bash
cd ecommerce-api
export JWT_SECRET="$(openssl rand -base64 32)"
export BOOTSTRAP_ADMIN_PASSWORD='choose-a-password'
SPRING_PROFILES_ACTIVE=h2 ./mvnw spring-boot:run
```

Start the client in a second terminal:

```bash
cd ecommerce-client
npm ci
npm run dev
```

Open the client at [http://localhost:5173](http://localhost:5173). Vite forwards API requests to the local API at [http://localhost:8080](http://localhost:8080).

## Sample CSV

The provided `Code Challenge E-Commerce.csv` file was downloaded on **2026-07-21**.

## Authentication

Product browsing is public. Cart operations, checkout, and order queries require a `CUSTOMER`
bearer token. Product administration and CSV imports require `ADMIN`. Register customers with
`POST /api/v1/auth/register` and log in with `POST /api/v1/auth/login`; both return a 15-minute
JWT access token. The complete API, persistence, address-snapshot, and security decisions are in
[docs/Architecture.md](docs/Architecture.md).
