# Virtual Wallet

A secure fintech backend application that simulates a digital wallet platform where users can manage multiple currency wallets, top up balances, and transfer money securely between accounts.

The application focuses on secure architecture, proper authorization, transaction safety, and clean backend design using Spring Boot.

## Tech Stack

- Java 17
- Spring Boot
- Spring Security
- JWT Authentication
- Spring Data JPA / Hibernate
- MariaDB
- Docker & Docker Compose
- JUnit + Mockito (unit testing)

## Running the Application (Docker) + Configuration

Sensitive configuration values must be provided in:

```bash
resources/application-secrets.properties
```

Configure the following values:

```bash
exchange.api.key=YOUR_API_KEY               // required for currency conversion between wallets
spring.mail.username=YOUR_EMAIL             // required for OTP email verification
spring.mail.password=YOUR_EMAIL_PASSWORD
```
API key you can aquire here: https://app.exchangerate-api.com/sign-up.

In order for SMTP to work, you need to generate an app password here: https://support.google.com/accounts/answer/185833?hl=en, under "Create and manage your app passwords".

The application is containerized using **Docker Compose**.

Build the project:

```bash
./gradlew build
```

This generates the application JAR used by the container.

Start the container:

```bash
docker compose up --build
```

This will start:
-  MariaDB database container
-  Spring Boot backend container

Backend will be available at:

```bash
http://localhost:8080
```
You can start using the API via a client of your choice.

## Security Features

The application implements several security mechanisms typical for fintech systems.

### Authentication & Authorization

- JWT-based authentication
- Method-level authorization using `@PreAuthorize`
- Role-based access control
- Two roles supported:
  - `ROLE_ADMIN`
  - `ROLE_USER`
 
### Account Verification

Newly registered users must **verify their account via email** before accessing protected endpoints.

Verification emails are sent using **SMTP via JavaMailSender**.

### Secure Card Handling

Card information is **never stored in plain form**. Instead:

- Card fingerprints are used
- Duplicate card registrations are prevented
- Card validation is performed before processing top-ups
- Instead of sending card information on every top-up, a sha256 hashed token with all the needed information is passed

## Core Features

### User Management

- User registration with password hashed using BCrypt
- Email verification
- Role-based authorization
- Admin access for administrative endpoints

### Wallet System

Users can create and manage **multiple wallets with different currencies**.

Supported features:

- Create wallets
- View balances and manage wallets
- Transfer between wallets (user to user)
- Cross-currency wallet transfers

Currency conversion is powered by **ExchangeRateAPI**.

### Top Ups

Wallets can be funded via **card top-ups** using mock payment logic.

Features include:

- Card validation
- Card ownership verification
- Duplicate card prevention
- Simulated payment processing

### Transactions Between Users

Users can securely transfer funds to other users in the system.

Features include:

- Balance validation
- Secure transaction creation
- Transaction history storage
- Currency-aware transfers

## Testing

Coverage includes:

- Service layer logic
- Business validation
- Transaction operations
- Wallet operations
- User management

Testing tools used:

- JUnit
- Mockito

## Seeded Data

For easier testing, the application automatically seeds **three users** when the database is empty.

| Role | Email | Username | Password | Status |
|-----|-----|-----|-----|-----|
Admin | admin@example.com | `admin` | `admin` | Verified |
User | user@example.com | `user` | `user` | Verified |
User | unverified@example.com | `unverified` | `unverified` | Not Verified |

This allows quick testing of:

- Admin functionality
- Normal user flows
- Email verification logic

## Author

Developed by Me

Backend project built as part of my journey toward building secure, production-style backend systems with Spring Boot.

## License

[MIT](https://choosealicense.com/licenses/mit/)
