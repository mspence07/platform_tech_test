# claimline

An expense claim service. Employees submit claims, approvers approve them within
their spending limit, and finance runs a monthly report of approved spend.

## Requirements

- JDK 21 or newer
- [Maven](https://maven.apache.org/install.html) 3.9 or newer

## Building and running

```sh
mvn test                  # run the test suite
mvn compile exec:java     # start the server on http://localhost:8080
```

`mvn package` builds `target/claimline.jar`, which can be started with
`java -jar target/claimline.jar` if you would rather not go through Maven each
time.

Set `CLAIMLINE_PORT` to change the port.

All amounts are whole dollars. A claim may not exceed $20,000.

## Approvers

Seven approvers are seeded from
`src/main/resources/com/claimline/seed/approvers.json`, each with a limit on the
size of claim they may approve:

| approverId | limit    |
| ---------- | -------- |
| `alice`    | $500     |
| `bharat`   | $5,000   |
| `chen`     | $10,000  |
| `dana`     | $5,000   |
| `eshe`     | $20,000  |
| `farouk`   | $20,000  |
| `gita`     | $20,000  |

Approvers submit expense claims of their own, like anyone else.

## Existing data

The service does not start empty. It comes with three months of settled claims:

- `data/audit-log.txt` holds the record of those claims being submitted and
  approved. The service reads this file to build the monthly report, and appends
  to it as the service runs, so its contents grow with use. To start again from
  the original three months, copy `data/audit-log.seed.txt` over it.
- The claims themselves are seeded from
  `src/main/resources/com/claimline/seed/claims.json`, so every claim in that
  history can be read back with `GET /claims/{id}`. All of them are already
  approved.

## Endpoints

### `POST /claims`

Submit a claim. New claims start `pending`.

```sh
curl -s -X POST http://localhost:8080/claims \
  -H 'Content-Type: application/json' \
  -d '{"submitterId":"erin","amount":315,"category":"travel","description":"train to Berlin"}'
# {"id":"clm-1a2b3c4d","submitterId":"erin","amount":315,"category":"travel","status":"pending"}
```

### `POST /claims/{id}/approve`

Approve a claim. Returns `403` if the approver's limit is below the claim amount.

```sh
curl -s -X POST http://localhost:8080/claims/clm-1a2b3c4d/approve \
  -H 'Content-Type: application/json' \
  -d '{"approverId":"bharat"}'
# {"id":"clm-1a2b3c4d","submitterId":"erin","amount":315,"category":"travel","status":"approved","approvedBy":"bharat"}
```

### `GET /claims/{id}`

Read a claim back.

```sh
curl -s http://localhost:8080/claims/clm-4a1c9e02
# {"id":"clm-4a1c9e02","submitterId":"erin","amount":42,"category":"travel","status":"approved","approvedBy":"alice"}
```

### `GET /reports/monthly?month=YYYY-MM`

Approved spend for a month, totalled by category, counting each approved claim
once. Finance runs this to close the books each month.

```sh
curl -s 'http://localhost:8080/reports/monthly?month=2026-06'
# {"month":"2026-06","totalsByCategory":{"equipment":1964,"meals":169,"training":875,"travel":237},"total":3245}
```

## Layout

The code is split into three layers. `src/main/java/com/claimline/http/` holds
thin HTTP handlers that validate requests and map outcomes to status codes.
`src/main/java/com/claimline/service/` holds the business logic: submitting and
approving claims, and building the monthly report.
`src/main/java/com/claimline/audit/` records every change to a claim. Supporting
packages hold the approval policy (`policy/`), the in-memory claim store
(`store/`), the seed fixtures (`seed/`), and configuration (`config/`).
