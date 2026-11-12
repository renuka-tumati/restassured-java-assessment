# Candidate assessment — REST Assured + Java

**Timebox:** 90 minutes. **Level:** Intermediate–advanced. **Stack:** Java 17, JUnit 5, REST Assured, Maven, local HTTP API.

## Scenario
You inherited an API test suite for a small order-management service. A CI run is failing after a refactor. Diagnose failures, correct the test code, and expand coverage. The application contract below is authoritative; do not change production API code unless you can demonstrate a genuine contract defect.

## API contract
- `POST /api/auth/login` with JSON `{"username":"qa","password":"secret"}` returns **200**, `token` (nonblank), `expiresIn` (positive integer). Invalid credentials return **401** with `error.code="INVALID_CREDENTIALS"`.
- Protected endpoints require `Authorization: Bearer <token>`; missing/invalid tokens return **401**.
- `GET /api/products` returns **200** and `products` array containing `{id, name, price, stock}`.
- `POST /api/orders` with JSON `{"productId":"p-100","quantity":2}` and a valid token returns **201**, `orderId` (nonblank), `status="CREATED"`, `total` (numeric), and `Location: /api/orders/{orderId}`. Quantity must be **1–5** inclusive. Invalid quantity returns **400** with `error.code="INVALID_QUANTITY"`. Unknown product returns **404** with `error.code="PRODUCT_NOT_FOUND"`.
- `GET /api/orders/{orderId}` with a valid token returns **200** and the created order; unknown IDs return **404**.
- `POST /api/orders` is not idempotent: repeating a successful request creates another order.
- Product `p-100` costs **12.50** per unit; `p-200` costs **7.00** per unit. Monetary amounts are represented as JSON numbers.

## Part A — Debug existing tests (40 pts)
Run `mvn test`. Inspect stack traces, HTTP responses, and the request specifications. Repair the four existing tests in `src/test/java/com/interview/tests/ApiRegressionTest.java`. Avoid fixed sleeps and avoid changing expected values merely to silence genuine defects.

## Part B — Add test coverage (30 pts)
Implement at least these tests:
1. Invalid credentials → 401 and exact error code.
2. Missing bearer token → 401.
3. Parameterized quantity boundaries: **0, 1, 5, 6**; assert appropriate status and error contract.
4. Unknown product → 404 with exact error code.
5. Create order, then retrieve it; assert ID, status, quantity, product, and total.
6. Two identical successful order requests generate distinct order IDs.

## Part C — Framework engineering (20 pts)
- Make request specifications reusable without leaking authentication state between tests.
- Ensure order tests are independent of test execution order.
- Add failure-only request/response logging (without printing secrets in ordinary successful runs).
- Add at least one useful schema/type assertion, not just HTTP status.
- Make tests deterministic and suitable for CI.

## Part D — Findings and explanation (10 pts)
In `FINDINGS.md`, document each failure, root cause, evidence, fix, and verification command. Include a short explanation of why `POST` retry behavior is risky.

## Interview rules
- You may inspect `src/main` to confirm the service contract, but first debug using test output and `ASSESSMENT.md`.
- Do not remove or disable tests, hardcode returned order IDs, or modify the service to fit broken tests.
- Commit your changes when finished.
- Optional stretch: implement a safe reusable API client abstraction, plus an assertion that the `Location` header resolves to the created resource.

## Suggested time allocation
10 min inspect, 25 min debug, 30 min new coverage, 15 min refactor, 10 min findings.

## Scoring
Debugging 40, new tests 30, framework 20, explanation 10.
