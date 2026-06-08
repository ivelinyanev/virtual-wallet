# Architecture Review — Virtual Wallet

**Date:** 2026-06-08  
**Scope:** Backend (Spring Boot) + Frontend (Vue 3)  
**Goal:** Surface deepening opportunities — refactors that turn shallow modules into deep ones, improving testability and locality.

---

## Candidate 1 — Authorization is split across three different places

**Strength:** Strong  
**Area:** Backend

### Files involved
- `backend/src/main/java/example/backend/annotations/RequiresVerifiedAccountAspect.java`
- `backend/src/main/java/example/backend/utils/AuthUtils.java`
- `backend/src/main/java/example/backend/services/implementations/TransferServiceImpl.java`
- `backend/src/main/java/example/backend/services/implementations/WalletServiceImpl.java`
- `backend/src/main/java/example/backend/services/implementations/CardServiceImpl.java`

### Problem
Authorization rules live in three disconnected places simultaneously:
- The `@RequiresVerifiedAccount` aspect enforces "is verified + not blocked".
- `@PreAuthorize` Spring Security annotation enforces role membership.
- Individual service methods do ownership checks inline, each calling `AuthUtils.getAuthenticatedUser()`, which hits the database every time.

There is **no test for the aspect itself**. Authorization failures are tested through service tests that mock the wrong layer. If a service is added and the developer forgets to annotate it, there is no safety net.

**Deletion test:** Delete `AuthUtils`. The complexity doesn't vanish — it reappears verbatim in every service method that needs to know who is acting.

### Solution
Introduce a single `AuthorizationService` that is the only place authorization decisions are made. It caches the current user in a request-scoped bean (eliminating repeated DB hits). Services call `authz.assertCanAccessWallet(wallet)` instead of fetching the user themselves. The aspect and `@PreAuthorize` annotations route through this service too.

### Benefits
- **Locality:** All authorization rules in one file. Adding "shared wallets" means one change, not three.
- **Leverage:** Services don't need to know how authorization works. One call, full enforcement.
- **Testability:** Test `AuthorizationService` in isolation. Service tests stop mocking `AuthUtils`.
- **Performance:** Request-scoped caching eliminates N DB hits per request (currently 2–3 per transfer operation).

---

## Candidate 2 — Transaction recording is called from three different services

**Strength:** Strong  
**Area:** Backend

### Files involved
- `backend/src/main/java/example/backend/services/implementations/TransactionServiceImpl.java`
- `backend/src/main/java/example/backend/services/implementations/TransferServiceImpl.java`
- `backend/src/main/java/example/backend/services/implementations/WalletServiceImpl.java`
- `backend/src/main/java/example/backend/services/implementations/CardServiceImpl.java`

### Problem
`TransactionService.recordTransaction()` is a package-private method called from `TransferService`, `WalletService`, and `CardService`. Each caller constructs the transaction differently — some pass a card, some don't; some record two transactions (debit + credit) for a transfer, some record one.

Tests for write paths live in `TransferServiceTests` and `WalletServiceTests`, not in `TransactionServiceTests`. Adding an audit field (e.g., fee, description) requires touching all three callers.

**Deletion test:** Delete `recordTransaction`. The complexity scatters across the three callers. The method was earning its keep, but the interface is too thin — callers have to know too much about how to call it correctly.

### Solution
Introduce a `LedgerService` that owns the transaction record seam completely. Instead of callers invoking `recordTransaction()` with raw fields, they call semantically-named methods: `recordTransfer(from, to, amount, rate)`, `recordTopUp(wallet, card, amount)`. The ledger handles atomicity, audit fields, and ensures debits balance credits.

### Benefits
- **Locality:** Adding a "fee" field to all transactions is one change in `LedgerService`, not three.
- **Leverage:** Transfer atomicity (debit + credit) is guaranteed by `LedgerService`, not by hoping callers get it right.
- **Testability:** `TransactionServiceTests` can test all write paths directly. Service tests only verify they called the ledger correctly.
- **Audit trail:** One place to add idempotency keys, event publishing, or compliance logging.

---

## Candidate 3 — ConversionService is a bare HTTP call with no abstraction

**Strength:** Strong  
**Area:** Backend

### Files involved
- `backend/src/main/java/example/backend/services/implementations/ConversionServiceImpl.java`
- `backend/src/main/java/example/backend/services/implementations/TransferServiceImpl.java`
- `backend/src/main/java/example/backend/config/RestClientConfig.java`

### Problem
`ConversionServiceImpl` is 51 lines: one method that calls `exchangerate-api.com` via `RestClient`, unwraps the response, and returns a rate. There is no caching, no retry, no fallback. If the external API is slow, every transfer is slow. If it is down, transfers fail.

The test for `ConversionService` doesn't exist — conversion is tested only as a side-effect inside `TransferServiceTests`, where `RestClient` is mocked. This is a **phantom seam**: the interface (`ConversionService`) exists, but there is only one adapter (the live API), so the seam has no real leverage.

### Solution
Give `ConversionService` a second adapter — a `CachedExchangeRateAdapter` that wraps the live one with a short TTL (e.g., 5 minutes). The seam becomes real: tests inject a stub adapter with fixed rates, without any HTTP mocking. The live adapter can add retries and a stale-rate fallback without touching transfer logic.

### Benefits
- **Real seam:** Two adapters (cached + live) make the seam testable. No HTTP mocking in service tests.
- **Resilience:** Stale-rate fallback means transfers don't fail when the external API is briefly unavailable.
- **Performance:** Rates cached for 5 minutes eliminate hundreds of HTTP calls during peak transfer times.

---

## Candidate 4 — Transfer and wallet validation rules are duplicated across services

**Strength:** Worth exploring  
**Area:** Backend

### Files involved
- `backend/src/main/java/example/backend/services/implementations/TransferServiceImpl.java`
- `backend/src/main/java/example/backend/services/implementations/WalletServiceImpl.java`

### Problem
`TransferServiceImpl` embeds 15+ private validation helpers: `validatePositiveAmount`, `validateWalletOwner`, `validateDifferentWallets`, `validateSufficientFunds`, etc. `WalletServiceImpl` duplicates at least `validatePositiveAmount` and ownership checks independently.

These validators aren't reusable because they are private methods with no interface. Each service tests them through its own test class. If the "positive amount" rule changes (e.g., "must be ≥ 0.01"), it must be updated in two places.

### Solution
Extract a `WalletOperationValidator` that centralizes all business rules for wallet operations. Both `TransferService` and `WalletService` inject it rather than implementing the rules inline. The validator is testable in isolation without standing up a full service.

### Benefits
- **Locality:** All transfer/wallet business rules in one place.
- **Testability:** Validator tests cover all rule combinations in isolation. Services test that they call the validator, not the rules themselves.

---

## Candidate 5 — Error handling is copy-pasted across every composable

**Strength:** Worth exploring  
**Area:** Frontend

### Files involved
- `frontend/src/composables/useTransfer.ts`
- `frontend/src/composables/useInternalTransfer.ts`
- `frontend/src/composables/useWallets.ts`
- `frontend/src/api/client.ts`

### Problem
Every composable that makes an API call contains the same deeply nested error extraction:

```ts
(e as { response?: { data?: { message?: string } } }).response?.data?.message
```

This pattern appears verbatim in `useTransfer`, `useInternalTransfer`, and `useWallets`. The `api/` modules are thin wrappers — they pass the raw Axios error through unchanged.

**Deletion test:** Delete the error extraction from one composable. Users see an unreadable `[object Object]`. The logic was earning its keep, but it is in the wrong place.

### Solution
Add a response interceptor to `api/client.ts` that converts all error responses to a typed `ApiError`. Composables catch `ApiError` — a typed exception — instead of casting `unknown` through nested optional chains. Changing the API's error format is one change in one file.

### Benefits
- **Locality:** API error format lives in one adapter. Adding a global "show toast on 401" is one interceptor line.
- **Leverage:** All composables get typed errors automatically. No casting, no optional chains.
- **Testability:** Composables can be tested with a simple `throw new ApiError('...')` rather than mocking Axios response shape.

---

## Candidate 6 — Side effects (email, wallet creation) are hardwired into business flows

**Strength:** Speculative  
**Area:** Backend

### Files involved
- `backend/src/main/java/example/backend/services/implementations/VerificationServiceImpl.java`
- `backend/src/main/java/example/backend/services/implementations/AuthServiceImpl.java`
- `backend/src/main/java/example/backend/services/implementations/WalletServiceImpl.java`
- `backend/src/main/java/example/backend/config/EmailConfig.java`

### Problem
Registration triggers a 4-deep synchronous call chain: `AuthService → UserService → VerificationService → EmailService`. If email sending fails, the transaction rolls back, and the user must retry — but the verification code may already be burned. There is no decoupling between "user created" and "send email".

Adding SMS verification, push notifications, or a welcome wallet on registration requires modifying `VerificationService` or `AuthService` directly.

### Solution
Use Spring's `ApplicationEventPublisher` to publish a `UserRegisteredEvent` after the user is saved. An async listener (`@EventListener` + `@Async`) handles email sending. The user is saved regardless of email success. Adding SMS later is a new listener, not a service change.

> **Note:** This is the most significant change in scope. Spring's built-in event system is lightweight, but it introduces async complexity. Only tackle this after candidates 1–3 are in place.

---

## Top Recommendation

### Start with Candidate 2: LedgerService
Transaction recording is the most concrete deepening available — the problem is visible (3 callers, scattered write tests), the solution is bounded (one new service class, 3 call sites updated), and the payoff is immediate (write tests move to a single test class, audit fields have one owner). It is also a prerequisite for Candidate 6: domain events are much cleaner when the ledger is already a deep module.

### Then Candidate 1: AuthorizationService
Authorization fragmentation affects every service and has a security implication: a new service endpoint could accidentally bypass ownership checks. An `AuthorizationService` with request-scoped user caching also eliminates repeated DB hits — measurable, testable, safe to add incrementally.

### Then Candidate 3: ExchangeRate adapter + caching
Giving `ConversionService` a second adapter is what makes the seam real — and it unblocks writing fast, deterministic transfer tests without HTTP mocking. The cache is a bonus that eliminates real latency.
