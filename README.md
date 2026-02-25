# 💳 Virtual Wallet

**Status: Actively Under Development**

This project is currently evolving into a larger, production-style backend system.  
Core features are implemented, but the architecture and feature set are expanding.

I decided to make it public early to document the development process and showcase the system design as it grows.

## Overview

Virtual Wallet is a backend application that simulates a digital wallet system with:

- User authentication (JWT-based)
- Wallet management (multi-currency support)
- Sensitive card data not stored directly, working with tokenization and fingerprint validation to process top-ups and prevent duplicate card registrations per user
- Transfers between users
- Role-based authorization
- Account verification (OTP via email)
- Currency exchage using ExchageRateAPI wtih real time conversion rates
- Transaction handling

Built with:
- Java
- Spring Boot
- Spring Security
- JPA / Hibernate
- MySQL
- Mockito (unit testing)

## Current Focus

- Improving service-layer architecture
- Adding unit & integration tests
- Strengthening validation & error handling
- Refining transaction logic
- Improving API design consistency

## Planned Improvements

- Transaction history pagination & filtering
- Global exception handling refactor
- Testcontainers integration
- API documentation (Swagger)

## Note

This project is not production-ready yet.  
It is being developed as part of my journey toward building real-world backend systems with scalable architecture.

---

## Author

Built and maintained by @ivelinyanev.
