# RideLink

RideLink is a student ride-hailing backend implemented as four Java Spring Boot REST microservices. Each service owns its MongoDB database. Services communicate over HTTP; account tokens are used to authorize user requests and service-to-service calls.

## Services and local addresses

| Service | Responsibility | Port | Swagger UI | OpenAPI JSON |
|---|---|---:|---|---|
| Account | Passenger/driver registration and login, JWT issuance, roles, profiles, account status | 8081 | http://localhost:8081/swagger-ui.html | http://localhost:8081/v3/api-docs |
| Driver & Vehicle | Driver operational profile, vehicle, service area, simulated location, availability, eligible driver search | 8082 | http://localhost:8082/swagger-ui.html | http://localhost:8082/v3/api-docs |
| Ride Management | Ride requests, driver assignment, lifecycle transitions and ride retrieval | 8083 | http://localhost:8083/swagger-ui.html | http://localhost:8083/v3/api-docs |
| Fare & Payment | Fare estimates, simulated charges, payment status and receipts | 8084 | http://localhost:8084/swagger-ui.html | http://localhost:8084/v3/api-docs |

All routes use the `/api/v1` prefix. See each service's `openapi.yaml` and Postman collection for request/response schemas and runnable examples.

## Prerequisites

- Java 21 (JDK, with `java` available in PowerShell)
- Git
- MongoDB Atlas account and a database user allowed to access the cluster, or another reachable MongoDB deployment
- Postman (optional, for the supplied API collections)
- Internet access for Maven to download dependencies on the first build

The repository contains a Maven wrapper (`mvnw.cmd`) in each service; a separate Maven installation is not required.

## Configuration and secrets

Each service reads its MongoDB connection string from an environment variable. Configure a **separate database name** in each URI:

| Service | Required Mongo URI environment variable | Suggested database |
|---|---|---|
| Account | `SPRING_MONGODB_URI` | `ridelink_accounts` |
| Driver & Vehicle | `DRIVER_MONGODB_URI` | `ridelink_drivers` |
| Ride Management | `RIDE_MONGODB_URI` | `ridelink_rides` |
| Fare & Payment | `PAYMENT_MONGODB_URI` | `ridelink_payments` |

Use this URI shape, replacing the placeholders with your own Atlas values:

```text
mongodb+srv://<database-user>:<URL-encoded-password>@<cluster-host>/<database-name>?retryWrites=true&w=majority
```

URL-encode special characters in the database password. Do not commit connection strings, passwords, real JWT secrets, or populated `.env` files. Spring Boot does not automatically load a plain `.env` file; set the variables in the terminal that starts each service, or configure them in your IDE's run configuration.

Account Service additionally requires `JWT_SECRET_BASE64`. Generate a local 32-byte secret in PowerShell:

```powershell
$bytes = New-Object byte[] 32
[System.Security.Cryptography.RandomNumberGenerator]::Fill($bytes)
$env:JWT_SECRET_BASE64 = [Convert]::ToBase64String($bytes)
```

Set the same `JWT_SECRET_BASE64` value for Account Service and any service configuration that validates account JWTs. Account Service can create an admin account at startup when both `ACCOUNT_ADMIN_EMAIL` and `ACCOUNT_ADMIN_PASSWORD` are set. Choose your own local values; do not use the examples from CI as real credentials.

Inter-service URLs default to the following local addresses and can be overridden with environment variables:

| Variable | Default |
|---|---|
| `ACCOUNT_SERVICE_BASE_URL` | `http://localhost:8081` |
| `DRIVER_SERVICE_BASE_URL` | `http://localhost:8082` |
| `RIDE_SERVICE_BASE_URL` | `http://localhost:8083` |

MongoDB Atlas must allow network access from your current public IP. If a service cannot connect, check its URI, database user's password/permissions, Atlas network access list, and the startup log.

## Start all services (Windows PowerShell)

Open **four PowerShell terminals**, one for each service. Start them in this order so dependent services can reach Account and Driver services:

1. Account Service
2. Driver & Vehicle Service
3. Ride Management Service
4. Fare & Payment Service

### Terminal 1 — Account Service

```powershell
Set-Location D:\RideLink\account-service
$env:SPRING_MONGODB_URI = 'mongodb+srv://<user>:<encoded-password>@<cluster-host>/ridelink_accounts?retryWrites=true&w=majority'
$bytes = New-Object byte[] 32
[System.Security.Cryptography.RandomNumberGenerator]::Fill($bytes)
$env:JWT_SECRET_BASE64 = [Convert]::ToBase64String($bytes)
$env:ACCOUNT_ADMIN_EMAIL = 'admin@example.com'
$env:ACCOUNT_ADMIN_PASSWORD = 'Choose-A-Local-Admin-Password-123!'
.\mvnw.cmd spring-boot:run
```

Keep this terminal open. Copy the generated `JWT_SECRET_BASE64` value from this terminal if you configure JWT validation in the other service terminals. The example admin credentials are placeholders: replace the password and keep it private.

### Terminal 2 — Driver & Vehicle Service

```powershell
Set-Location D:\RideLink\driver-vehicle-service
$env:DRIVER_MONGODB_URI = 'mongodb+srv://<user>:<encoded-password>@<cluster-host>/ridelink_drivers?retryWrites=true&w=majority'
$env:ACCOUNT_SERVICE_BASE_URL = 'http://localhost:8081'
.\mvnw.cmd spring-boot:run
```

### Terminal 3 — Ride Management Service

```powershell
Set-Location D:\RideLink\ride-management-service
$env:RIDE_MONGODB_URI = 'mongodb+srv://<user>:<encoded-password>@<cluster-host>/ridelink_rides?retryWrites=true&w=majority'
$env:ACCOUNT_SERVICE_BASE_URL = 'http://localhost:8081'
$env:DRIVER_SERVICE_BASE_URL = 'http://localhost:8082'
.\mvnw.cmd spring-boot:run
```

### Terminal 4 — Fare & Payment Service

```powershell
Set-Location D:\RideLink\fare-payment-service
$env:PAYMENT_MONGODB_URI = 'mongodb+srv://<user>:<encoded-password>@<cluster-host>/ridelink_payments?retryWrites=true&w=majority'
$env:ACCOUNT_SERVICE_BASE_URL = 'http://localhost:8081'
$env:RIDE_SERVICE_BASE_URL = 'http://localhost:8083'
.\mvnw.cmd spring-boot:run
```

Wait for each terminal to report that the application has started. Keep all four running while testing integrated workflows. Stop an individual service with `Ctrl+C` in its terminal. MongoDB creates the database/collections after the service first writes data; an empty Atlas database before the first write is expected.

## Build and test

Run tests separately from each service directory:

```powershell
Set-Location D:\RideLink\account-service
.\mvnw.cmd --batch-mode clean test

Set-Location D:\RideLink\driver-vehicle-service
.\mvnw.cmd --batch-mode clean test

Set-Location D:\RideLink\ride-management-service
.\mvnw.cmd --batch-mode clean test

Set-Location D:\RideLink\fare-payment-service
.\mvnw.cmd --batch-mode clean test
```

Maven test summaries and detailed reports are written under each service's `target/surefire-reports/` directory. A test command succeeds only when Maven ends with `BUILD SUCCESS`. GitHub Actions runs the four services' `clean test` commands on pushes and pull requests that change a service or the CI workflow; it can also be started manually from the Actions tab. CI uses an isolated MongoDB service and CI-only credentials.

## Main REST endpoints

Requests requiring a user must include `Authorization: Bearer <access-token>` from Account Service login. Use the correct passenger, driver, or admin account for role-protected operations.

### Account Service — `http://localhost:8081`

- `POST /api/v1/auth/register` — register a passenger or driver
- `POST /api/v1/auth/login` — authenticate and receive a JWT
- `GET /api/v1/accounts/me` — view own profile
- `PATCH /api/v1/accounts/me` — update own profile
- `PATCH /api/v1/accounts/me/email` — change own email
- `PATCH /api/v1/accounts/me/password` — change own password
- `DELETE /api/v1/accounts/me` — close own account
- `GET /api/v1/admin/accounts` and `GET /api/v1/admin/accounts/{id}` — admin account lookup
- `PATCH /api/v1/admin/accounts/{id}/role` and `/status` — admin role/status management
- `DELETE /api/v1/admin/accounts/{id}` — admin account deletion

### Driver & Vehicle Service — `http://localhost:8082`

- `GET /api/v1/drivers/me` — view own driver profile
- `PUT /api/v1/drivers/me/profile` — save operational profile and service area
- `PUT /api/v1/drivers/me/vehicle` — add/update vehicle
- `PUT /api/v1/drivers/me/location` — update simulated location
- `PATCH /api/v1/drivers/me/availability` — set availability
- `GET /api/v1/drivers/eligible?pickupLatitude=6.9271&pickupLongitude=79.8612&maxDistanceKm=10` — find eligible available drivers

### Ride Management Service — `http://localhost:8083`

- `POST /api/v1/rides` — create ride request
- `POST /api/v1/rides/{id}/assign` — assign an eligible driver
- `POST /api/v1/rides/{id}/accept`, `/start`, `/complete`, `/cancel` — lifecycle actions
- `GET /api/v1/rides/{id}` — retrieve a ride
- `GET /api/v1/rides/me` — list rides visible to the signed-in user

### Fare & Payment Service — `http://localhost:8084`

- `POST /api/v1/fare-estimates` — estimate fare from pickup and destination coordinates
- `POST /api/v1/payments` — record a simulated payment after ride completion
- `GET /api/v1/payments/me` — list the user's payments
- `GET /api/v1/payments/by-ride/{rideId}` — retrieve payment for a ride
- `GET /api/v1/payments/{paymentId}/receipt` — retrieve successful payment receipt
- `GET /api/v1/payments/{paymentId}` — retrieve payment by ID

Swagger UI provides interactive schemas and endpoint details. `openapi.yaml` files are also provided in each service root.

## Postman and sample test data

Import the collection and environment JSON files below into Postman. Select the matching environment and set its `baseUrl` to the service's local address. Use the collections in the documented order because later requests may use IDs and tokens captured by earlier requests.

- Account: `account-service/Postman_collection_Account_Service/`
- Driver & Vehicle: `driver-vehicle-service/Postman_collection_Driver_Vehicle/`
- Ride Management: `ride-management-service/Postman_collection_ride_management_service/`
- Fare & Payment: `fare-payment-service/Postman_collection_fare_payment_service/`

Use fresh example identities such as `passenger1@example.com`, `driver1@example.com`, and a unique password chosen for local testing. These are example test identities, not pre-created accounts. Register passenger and driver accounts through Account Service first, then log in and use the returned tokens. The admin account is only available if the bootstrap environment variables are configured before Account Service starts. Never store real account or MongoDB credentials in a shared Postman environment.

For a full demonstration: register/login passenger and driver; create the driver's operational profile, vehicle and service area; set simulated location and availability; obtain a fare estimate; request a ride; assign, accept, start and complete it; simulate payment; retrieve payment and receipt. Also demonstrate negative cases such as an invalid lifecycle transition, a request by the wrong role, no eligible driver, or a simulated payment decline.

## Repository layout

```text
RideLink/
├── .github/workflows/all-services.yml
├── account-service/
├── driver-vehicle-service/
├── ride-management-service/
└── fare-payment-service/
```

Each service is an independent Maven/Spring Boot application with its own `pom.xml`, Maven wrapper, source code, tests, `application.properties`, `openapi.yaml`, and Postman evidence. Do not add secrets or generated `target/` output to Git.

## Security and academic use

Only use test accounts and data you are authorized to create. Keep admin passwords, database credentials and JWT secrets private; rotate any secret that has been shared or committed. This project simulates payment and location; it does not charge a real card or track a real driver. Follow your course's rules for attribution, permitted collaboration and explaining the work during assessment.