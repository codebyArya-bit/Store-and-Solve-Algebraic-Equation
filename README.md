# Store and Solve Algebraic Equation — Java Backend Assignment

![Java CI](https://github.com/codebyArya-bit/Store-and-Solve-Algebraic-Equation/actions/workflows/ci.yml/badge.svg)

A **REST-only** Java 21 Spring Boot API that stores algebraic expressions in **postfix expression trees**, lists them reconstructed from the trees, and evaluates them with supplied variable values. It uses an in-memory store, as required by the assignment.

## Verified public deployment

- **API:** https://algebra-equation-api.onrender.com/api/equations
- **Swagger UI:** https://algebra-equation-api.onrender.com/swagger-ui/index.html
- **OpenAPI JSON:** https://algebra-equation-api.onrender.com/v3/api-docs
- **Production smoke tests:** [GitHub Actions](https://github.com/codebyArya-bit/Store-and-Solve-Algebraic-Equation/actions/workflows/live-api-smoke.yml)

The deployed API has been checked with live GET, POST and error-path requests. Render's free tier may take some time to wake after inactivity. Equations are kept in memory and reset on restart.

## Run locally

Prerequisites: Java 21 and Maven 3.9+.

```sh
mvn clean verify
mvn spring-boot:run
```

Alternatively: `docker build -t algebra-equation-api . && docker run -p 8080:8080 algebra-equation-api`.

- API base: http://localhost:8080/api/equations
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

## API examples

**Store equation** — `POST /api/equations/store` (201 Created)

```json
{"equation":"3x + 2y - z"}
```

Response: `{"message":"Equation stored successfully","equationId":"1"}`.

**List equations** — `GET /api/equations`

Response:

```json
{"equations":[{"equationId":"1","equation":"(((3 * x) + (2 * y)) - z)"}]}
```

**Evaluate** — `POST /api/equations/1/evaluate`

```json
{"variables":{"x":2,"y":3,"z":1}}
```

Response:

```json
{"equationId":"1","equation":"3x + 2y - z","variables":{"x":2,"y":3,"z":1},"result":11.0}
```

**Note:** The problem PDF incorrectly gives 10 for that sample. Correct calculation is 3×2 + 2×3 − 1 = **11**.

## Implementation

- `parser/ExpressionParser`: tokenizer, implicit multiplication, shunting-yard infix-to-postfix conversion, postfix tree construction.
- `domain/ExpressionNode`: immutable number, variable, operator tree nodes (**Composite pattern**); recursive evaluation.
- `service/EquationService`: concurrent in-memory storage, atomic ID allocation, retrieval ordered by ID.
- `web/EquationController`: API layer, request DTO validation; `ApiExceptionHandler`: consistent error mapping.
- `src/test`: JUnit and MockMvc coverage; `.github/workflows/ci.yml`: GitHub Actions test workflow.

Supports `+ - * / ^`, parentheses, real-number literals, unary signs, one-letter variables, implicit multiplication (`3x`, `2(x+1)`, `3xy`). Exponentiation is right-associative; unary minus behaves as `-(2^2)` for `-2^2`.

Malformed input, division by zero, unbound variables, and non-finite results return **400**. Unknown equation IDs return **404**.

## Design assumptions

- Variables are single letters, matching provided examples; `xy` means `x*y`.
- `=` is not supported: the task expects arithmetic expressions to evaluate, not symbolic equation solving.
- In-memory data resets when the application restarts; no persistent database.
- Stored list is reconstructed from expression trees, so output is fully parenthesized rather than identical to input.

## Deployment

Deploy the repository using the provided `Dockerfile` and `render.yaml` on a Java/Docker hosting provider such as Render. The public domain is assigned by the hosting provider. After deployment, validate the live `/api/equations`, `/swagger-ui.html`, and `/v3/api-docs` URLs. Never submit speculative public URLs.

## Recording checklist

Make a **2–5 minute video with your screen and your actual face visible throughout**. Show architecture and code, live Swagger calls for store/list/evaluate, explain postfix conversion and Composite pattern, and describe solving implicit multiplication and operator precedence. Publish a link viewable without sign-in.
