# RideLink Account Service

The RideLink Account Service manages user accounts for the RideLink platform.

It supports:

- Passenger and driver registration
- Login and JWT token issuance
- Passenger, driver, and admin roles
- Viewing and updating a personal profile
- Changing email and password
- Closing accounts
- Admin account listing and account management
- Account statuses: `ACTIVE`, `SUSPENDED`, and `CLOSED`

## Technology

- Java 21
- Spring Boot
- Spring Security with JWT
- MongoDB Atlas
- Maven
- Swagger / OpenAPI

## Run Locally

### 1. Open the service folder

```powershell
cd D:\RideLink\account-service
```

### 2. Set the environment variables

Use your own MongoDB connection string. The JWT secret must be a valid Base64 value and should be at least 32 bytes long.

```powershell
$env:SPRING_MONGODB_URI = "mongodb+srv://<username>:<password>@<cluster>/<database>?retryWrites=true&w=majority"

$bytes = New-Object byte[] 32
$rng = New-Object System.Security.Cryptography.RNGCryptoServiceProvider
$rng.GetBytes($bytes)
$env:JWT_SECRET_BASE64 = [Convert]::ToBase64String($bytes)
$rng.Dispose()

$env:ACCOUNT_ADMIN_EMAIL = "admin@example.com"
$env:ACCOUNT_ADMIN_PASSWORD = "ChangeThisAdminPassword123!"
```

### 3. Start the service

```powershell
.\mvnw.cmd clean spring-boot:run
```

The service runs at:

```text
http://localhost:8081
```

A successful startup log contains:

```text
Tomcat started on port 8081
Started AccountServiceApplication
```

## Swagger and OpenAPI

After the application starts, open these links in a browser:

- Swagger UI: http://localhost:8081/swagger-ui.html
- OpenAPI JSON document: http://localhost:8081/v3/api-docs

Swagger UI lets you view the API and try requests. For protected endpoints, first log in and use the returned JWT token as a Bearer token.

## API Endpoints

### Authentication

These endpoints do not need a token.

| Method | Endpoint | Purpose |
| --- | --- | --- |
| `POST` | `/api/v1/auth/register` | Register a passenger or driver and receive a token |
| `POST` | `/api/v1/auth/login` | Log in and receive a token |

### Personal account

These endpoints need:

```text
Authorization: Bearer <access-token>
```

| Method | Endpoint | Purpose |
| --- | --- | --- |
| `GET` | `/api/v1/accounts/me` | View your profile |
| `PATCH` | `/api/v1/accounts/me` | Update your name, phone, date of birth, or profile image |
| `PATCH` | `/api/v1/accounts/me/email` | Change your email after confirming your password |
| `PATCH` | `/api/v1/accounts/me/password` | Change your password |
| `DELETE` | `/api/v1/accounts/me?currentPassword=...` | Close your own account |

### Admin account management

These endpoints need an admin token.

| Method | Endpoint | Purpose |
| --- | --- | --- |
| `GET` | `/api/v1/admin/accounts` | List accounts with pagination |
| `GET` | `/api/v1/admin/accounts/{id}` | View one account |
| `PATCH` | `/api/v1/admin/accounts/{id}/role` | Change an account role |
| `PATCH` | `/api/v1/admin/accounts/{id}/status` | Change an account status |
| `DELETE` | `/api/v1/admin/accounts/{id}` | Close an account as an administrator |

## Basic Request Examples

### Register a passenger

`POST /api/v1/auth/register`

```json
{
  "fullName": "Test Passenger",
  "email": "passenger@example.com",
  "password": "ExamplePassphrase123!",
  "phone": "0771234567",
  "dateOfBirth": "1998-01-02",
  "profileImageUrl": null,
  "role": "PASSENGER"
}
```

Use `"role": "DRIVER"` to register a driver.

The response contains:

- `accessToken`: the JWT token
- `tokenType`: normally `Bearer`
- `expiresInSeconds`: token lifetime
- `account`: the created account details

### Login

`POST /api/v1/auth/login`

```json
{
  "email": "passenger@example.com",
  "password": "ExamplePassphrase123!"
}
```

Copy the returned `accessToken` and use it for protected requests.

### View your profile

`GET /api/v1/accounts/me`

Header:

```text
Authorization: Bearer <accessToken>
```

### Update your profile

`PATCH /api/v1/accounts/me`

```json
{
  "fullName": "Updated Passenger",
  "phone": "0712345678",
  "dateOfBirth": "1998-01-02",
  "profileImageUrl": null
}
```

## Postman Collection

The ready-made Postman files are in:

```text
Postman_collection_Account_Service/
```

Import both files into Postman:

1. `RideLink Account Service - Full Test Run.postman_collection.json`
2. `RideLink Account Service Environment - Local Example.postman_environment.json`

Select the environment named **RideLink Account Service - Local Example**.

The collection uses:

```text
baseUrl = http://localhost:8081
```

Run the collection requests in order with Postman Collection Runner. The collection automatically creates unique emails and stores tokens and account IDs for later requests.

The collection checks:

1. Passenger registration
2. Duplicate passenger registration rejection
3. Driver registration
4. Passenger login
5. Invalid password rejection
6. Passenger profile viewing
7. Passenger profile update
8. Email change
9. Login after email change
10. Password change
11. Login after password change
12. Admin login
13. Admin account listing
14. Admin account lookup
15. Admin role and status management

## Tests

Run the automated tests with:

```powershell
.\mvnw.cmd test
```

The service tests cover:

- Passenger registration
- Driver registration
- Duplicate email rejection
- Blocking self-registration as an admin
- Successful login
- Unknown email rejection
- Incorrect password rejection
- Suspended account rejection
- Profile updates

The Postman collection provides additional API-level checks for authentication, profiles, email and password changes, admin actions, and expected HTTP status codes.

## Common Problems

### `401 Unauthorized` during registration

Registration should not require a token. Make sure:

- The URL is exactly `http://localhost:8081/api/v1/auth/register`
- The request method is `POST`
- Authorization is set to `No Auth`
- The application was restarted after a code change
- The current application log shows the real controllers and security configuration were loaded

### Application does not start with a JWT Base64 error

Do not use a placeholder such as:

```text
your-random-base64-secret
```

Generate a real Base64 secret using the PowerShell command in the Run Locally section.

### `400 Bad Request` during registration

Check that the JSON includes these required fields:

- `fullName`
- `email`
- `password` with at least 10 characters
- `phone`
- `role` set to `PASSENGER` or `DRIVER`

### `409 Conflict` during registration

The email already exists. Use a new email address or run the Postman collection, which creates unique test emails automatically.

## Project Layout

```text
account-service/
  config/                         Application configuration
  controller/                     REST endpoints
  dto/                            Request and response objects
  exception/                      Error response handling
  model/                          Account, role, and status models
  repository/                     MongoDB repository
  security/                       JWT and Spring Security
  service/                        Account business logic
  src/main/java/...               Spring Boot application entry point
  src/main/resources/             Application properties
  src/test/java/...               Spring Boot test
  Postman_collection_Account_Service/
                                  Postman collection and environment
  openapi.yaml                    API contract
  pom.xml                         Maven build configuration
```
