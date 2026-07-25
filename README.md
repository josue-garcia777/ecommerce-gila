# E-Commerce

For the architecture, module boundaries, domain decisions, API design, import flow, and checkout flow, see [Architecture.md](Architecture.md).

## Run locally

Docker Compose is the recommended way to run the full stack.

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
SPRING_PROFILES_ACTIVE=h2 ./mvnw spring-boot:run
```

Start the client in a second terminal:

```bash
cd ecommerce-client
npm ci
npm run dev
```

Open the client at [http://localhost:5173](http://localhost:5173). Vite forwards API requests to the local API at [http://localhost:8080](http://localhost:8080).
