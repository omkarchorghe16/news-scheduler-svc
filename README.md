# Stock News Scheduler Service

A complete Java Spring Boot application that fetches daily stock market news for a configurable watchlist (US and India markets) and sends formatted digests to Slack, Telegram, and WhatsApp on a schedule.

## Features

- **Scheduled Digest**: Automatic daily news digest (configurable cron, default: 9 AM weekdays)
- **Multi-Market Support**: Separate fetches for US (NYSE/NASDAQ) and India (NSE) markets
- **Multiple News Sources**: Integrates Finnhub and NewsAPI.org
- **Smart Deduplication**: SHA-256 based dedup with 3-day history stored in PostgreSQL
- **Multi-Channel Notifications**: Slack, Telegram, WhatsApp (Twilio)
- **Portfolio Tracking**: Separate digest for configured portfolio symbols (Slack-only)
- **On-Demand Triggers**: REST endpoints to manually trigger digests without waiting for schedule
- **Stock Profiles**: Fetch Finnhub, Yahoo Finance, Alpha Vantage, and FMP company information and persist it in PostgreSQL
- **Sector and Stock Management**: CRUD endpoints for sectors and sector-associated stock tickers, including bulk stock creation
- **API Documentation**: Interactive Swagger UI and generated OpenAPI specification
- **Database**: PostgreSQL for application data; H2 is used only by tests
- **Local Development**: Docker Compose starts the Spring Boot service with local PostgreSQL
- **Development Standards**: Repository-specific Copilot instructions and reusable backend skills for architecture, REST APIs, JPA, provider integrations, scheduling/notifications, and testing
- **Configurable Everything**: @ConfigurationProperties binds all settings from environment variables

## Tech Stack

- **Java 21**
- **Spring Boot 3.5.16**
- **Maven**
- **Spring Data JPA + PostgreSQL** (H2 for tests)
- **Spring RestClient (6.1+)**
- **Lombok**
- **SLF4J Logging**

## Project Structure

```
com/stocknews/
├── StockNewsApplication.java
├── config/
│   ├── AppProperties.java
│   └── RestClientConfig.java
├── scheduler/
│   └── DigestScheduler.java        # Scheduled cron job
├── digest/
│   ├── DigestService.java          # Orchestrates fetch → dedup → format → notify
│   ├── DigestFormatter.java        # Formats messages for each channel
│   └── Watchlist.java              # Manages US/NSE symbol lists
├── client/
│   ├── NewsItem.java               # Record: title, url, source, publishedAt, symbol, market
│   ├── NewsSourceClient.java       # Interface
│   ├── FinnhubNewsClient.java      # Finnhub API integration
│   ├── NewsApiClient.java          # NewsAPI.org integration
│   └── FmpProfileClient.java       # FMP HTTP request and response parsing
├── notification/
│   ├── Notifier.java               # Interface
│   ├── SlackNotifier.java          # Slack Webhook
│   ├── TelegramNotifier.java       # Telegram Bot API
│   └── WhatsAppNotifier.java       # Twilio WhatsApp API
├── controller/
│   ├── DigestTriggerController.java
│   ├── SectorController.java
│   ├── StockController.java
│   └── StockProfileController.java # REST endpoints
├── dto/                            # Validated requests and API response records
├── model/                          # JPA entities and provider-specific records
├── repository/                     # Spring Data repositories (PostgreSQL)
├── alphavantage/
│   ├── AlphaVantageOverviewClient.java
│   ├── AlphaVantageOverviewService.java
│   └── AlphaVantageRateLimiter.java
├── service/                        # Stock/profile workflows, FMP quotas, normalization, and rate limits
└── exception/                      # API exceptions and centralized ProblemDetail mapping
```

Repository coding guidance lives in `.github/copilot-instructions.md` and focused standards in
`.github/instructions/`. Reusable workflows are in `.github/skills/`:

- `spring-boot-architecture`: cross-layer design, boundaries, and architecture changes
- `spring-boot-rest-api`: controllers, validation, OpenAPI, MockMvc, and Postman
- `spring-boot-jpa`: entities, repositories, transactions, and database-backed services
- `spring-boot-provider-integration`: provider clients, quotas, parsing, and safe errors
- `spring-boot-scheduling-notifications`: digest scheduling, orchestration, and delivery
- `spring-boot-testing`: focused JUnit, MockMvc, provider, and persistence tests

For every feature or behavior change, review **all** skills and update those affected by the code or
shared conventions. Keep this README and directly related API, configuration, and operational
documentation synchronized with the implementation. Do not leave relevant documentation stale or
make unrelated skill edits just to create churn.

## Prerequisites

### Required API Keys & Credentials

1. **Finnhub API** (optional but recommended for US market news)
   - Sign up: https://finnhub.io/
   - Get API key from dashboard

2. **NewsAPI.org** (optional but recommended for general market news)
   - Sign up: https://newsapi.org/
   - Get API key from dashboard

3. **Alpha Vantage** (optional, for company financial overviews)
   - Create an API key: https://www.alphavantage.co/support/#api-key
   - Free-tier quota used by this service: 25 requests/day and 5 requests/minute

4. **Slack** (optional for Slack notifications)
   - Create an Incoming Webhook: https://api.slack.com/apps/
   - Webhook URL format: `https://hooks.slack.com/services/YOUR/WEBHOOK/URL`
   - Optionally create a second webhook for portfolio channel

5. **Telegram** (optional for Telegram notifications)
   - Create a bot via @BotFather on Telegram
   - Get Bot Token and Chat ID

6. **Twilio WhatsApp** (optional for WhatsApp notifications)
   - Sign up: https://www.twilio.com/
   - Set up WhatsApp Sandbox: https://www.twilio.com/console/sms/whatsapp/learn
   - Get Account SID, Auth Token, and WhatsApp numbers
   - Note: Requires message template approval for production use

## Setup & Running Locally

### Keeping API keys and credentials out of Git

`application.yml` reads secrets from environment variables (for example, `FINNHUB_API_KEY`). The
repository `.env` starts with blank secret values; fill in local development values before starting
the PostgreSQL service:

```bash
docker compose up -d postgres
```

To run Spring Boot from a terminal on your Mac, set the datasource environment variables for that
shell and start Maven:

```bash
export POSTGRES_URL="jdbc:postgresql://localhost:5433/news_scheduler"
export POSTGRES_USER="news_scheduler"
export POSTGRES_PASSWORD="your-local-postgres-password"
mvn spring-boot:run
```

The host-run app defaults to `jdbc:postgresql://localhost:5433/news_scheduler` with user
`news_scheduler`; the password comes from `POSTGRES_PASSWORD`. For an IDE run configuration, set
`POSTGRES_URL`, `POSTGRES_USER`, and `POSTGRES_PASSWORD` from `.env` under **Environment variables**.
Compose itself reads `.env` automatically, but Maven and IDE run configurations do not. Use the same
local-only password in `.env` and the host app's environment. If you change `POSTGRES_HOST_PORT` in
`.env`, set `POSTGRES_URL` to that same host port. Do not commit `.env` or IDE run configuration
files containing credentials, or paste secrets into Postman exports.

An optional local YAML override is also supported: copy
`src/main/resources/application-local.yml` to the repository root as
`application-local.yml`, then run with the `local` Spring profile. The root override is ignored by Git.
Prefer referencing environment variables in the local file rather than writing literal secrets:

```yaml
app:
  apis:
    finnhub-key: ${FINNHUB_API_KEY}
```

If you specifically want a local secrets file, create `application-secrets.yml` in the repository
root (the same directory as `pom.xml`). `application.yml` imports this file at startup, and
`.gitignore` excludes it. Put credentials under the matching configuration keys:

```yaml
app:
  notifications:
    telegram:
      bot-token: "your-new-telegram-bot-token"
      chat-id: "your-telegram-chat-id"
  apis:
    finnhub-key: "your-finnhub-api-key"
    newsapi-key: "your-newsapi-key"
```

Create it locally without adding it to Git, restrict its filesystem permissions (for example,
`chmod 600 application-secrets.yml` on macOS/Linux), and check `git status` to confirm it is ignored.
The import is optional for direct local runs. Docker Compose passes configuration through `.env` and
does not mount this file. The optional Docker Desktop Kubernetes workflow can mount it as a local
Kubernetes Secret; it is excluded from the Docker build context and image.

Keep credentials in local environment variables or ignored local files; never put them in the image,
source repository, Postman exports, or committed manifests. Automated tests use mocks and do not
require live provider keys.

Credentials pasted into chat or other shared systems should be treated as exposed. Revoke/regenerate
credentials immediately if they are accidentally exposed.

### Run Spring Boot and PostgreSQL in Docker for development

Compose automatically reads `.env` from the repository root. The repository file contains blank
secret values; populate it with development-only credentials for local use.

Then set a development-only `POSTGRES_PASSWORD` in `.env`, as well as the
`FMP_API_KEY`, `FINNHUB_API_KEY`, `NEWSAPI_KEY`, `ALPHA_VANTAGE_API_KEY`, and notification
credentials you use. Do not commit real credentials in `.env`.
Compose passes these environment variables to Spring Boot, where `application.yml` already maps them
to the corresponding settings. Database credentials also come from `.env`. The app container does not need `application-secrets.yml`; that file remains optional for direct
local Spring Boot runs and the separate Kubernetes setup. `.env` is allowed by `.gitignore` so the
blank starter file can be tracked; take care not to commit it after adding credentials. It also
includes provider URLs/limits, WhatsApp enablement, provider timeout/retries, and
digest schedule/timezone settings. Leave defaults unchanged unless you want to override them.

Start both services from the repository root:

```bash
docker compose up --build -d
```

The app waits for PostgreSQL to become healthy. Inside Docker, the app connects to host `postgres`,
port `5432`. On your Mac, connect using `localhost`, port `5433`, database `news_scheduler`, user
`news_scheduler`, and the password from `.env`. The database port is bound to the Mac loopback only.
For example, enter those values in DBeaver, pgAdmin, or IntelliJ's Database tool. If you have the
PostgreSQL command-line client installed, connect with:

```bash
psql -h localhost -p 5433 -U news_scheduler -d news_scheduler
```

The database data persists in the Docker named volume `postgres_data`, including after
`docker compose down`. To delete this dev database permanently, use `docker compose down --volumes`;
that removes the volume and all data in it. View logs with `docker compose logs -f news-scheduler`,
stop containers with `docker compose down`, or restart with `docker compose up -d`. The host port can
be changed by setting `POSTGRES_HOST_PORT` in `.env`.

This Compose database is separate from any PostgreSQL already installed on the Mac. It is also
separate from the Kubernetes configuration below, which still targets PostgreSQL on the Mac at
`host.docker.internal:5432`.

### CI image build and Kubernetes deployment

The GitHub Actions workflow runs Maven package/tests and builds the Docker image on pushes, pull
requests, and manual dispatches. Non-PR runs publish commit-tagged and `latest` images to GHCR.
Pushes to `main` and manual workflow runs also deploy to the `dev` namespace. Configure these
repository secrets before enabling deployment:

- `KUBE_CONFIG`: kubeconfig contents for the target cluster, with permission to manage the `dev`
  namespace and its Deployment, Service, Ingress, and Secret.
- `APP_SECRETS_YAML`: contents of the Spring `application-secrets.yml` file, including database
  credentials and provider keys.

The deployed image must be pullable by the cluster. Make the GHCR package public or configure an
image pull secret on the `dev` namespace. The existing manifests target the `dev` namespace, use
`host.docker.internal:5432/postgres` for PostgreSQL, and use an example ingress host; update these
for the cluster and database before deploying remotely. Keep the deployment at one replica because
each instance runs the scheduled digest job.

### Deploy to Docker Desktop Kubernetes

The `k8s/` manifests target the **Docker Desktop Kubernetes cluster on this Mac** and keep PostgreSQL
on the Mac host. The pod connects to `host.docker.internal:5432`; this requires PostgreSQL to accept
connections from Docker Desktop's Kubernetes network. Do not use this database arrangement for a
remote cluster. Keep the deployment at one replica because every application instance runs the
scheduled digest job; increasing replicas can send duplicate digests.

1. Enable Kubernetes in Docker Desktop and make sure its context is active:

   ```bash
   kubectl config use-context docker-desktop
   kubectl get nodes
   kubectl apply -f k8s/00-namespace.yaml
   ```

2. Build the image locally; Docker Desktop Kubernetes uses the image from the local Docker engine:

   ```bash
   mvn --batch-mode -DskipTests package
   docker build -t news-scheduler-svc:local .
   ```

3. Add your actual PostgreSQL credentials and provider/notification keys to the ignored
   repository-root `application-secrets.yml`. The deploy helper uploads this file as the Kubernetes
   Secret `news-scheduler-secrets`, mounted read-only at `/app/application-secrets.yml`. Spring
   already imports that path from `application.yml`, so no secret values are baked into the image or
   passed on the command line. The Deployment sets `SPRING_DATASOURCE_URL` to
   `jdbc:postgresql://host.docker.internal:5432/postgres`; change that value in
   `k8s/deployment.yaml` if your database name or port differs.

   Put credentials under the same Spring property hierarchy used by the application, for example:

   ```yaml
   spring:
     datasource:
       username: "your-postgres-user"
       password: "your-postgres-password"
   fmp:
     api-key: "your-fmp-key"
   app:
     apis:
       finnhub-key: "your-finnhub-key"
       newsapi-key: "your-newsapi-key"
       alpha-vantage-key: "your-alpha-vantage-key"
   ```

   `application.yml` resolves environment variables such as `FMP_API_KEY` for local/Compose use;
   Kubernetes instead supplies the same Spring properties through the mounted secrets YAML file.

   Kubernetes Secrets are not encrypted just because their values are base64-encoded. This local
   workflow assumes a single-user Docker Desktop cluster; restrict cluster access and use a managed
   secret store for shared or production clusters.

4. Pass the local image tag to the helper. It verifies the Docker Desktop context and image,
   syncs the local secrets file into the cluster, applies the manifests, restarts/waits for the
   Deployment, and runs a readiness smoke test through a temporary port-forward on port `18080`:

   ```bash
   bash scripts/deploy-local-k8s.sh local
   kubectl get pods,services,ingress -n dev
   kubectl logs -n dev deployment/news-scheduler-svc
   ```

   Rebuild the image before redeploying after application changes. On every deployment, the helper
   refreshes the cluster Secret from the local file and restarts the pod, so updates to credentials
   are picked up. The deploy command fails if the readiness endpoint does not return a successful
   response. The Actuator dependency and health probe settings are already enabled in `pom.xml` and
   `application.yml`.

5. The ingress manifest expects an NGINX Ingress Controller with class `nginx`. If one is installed,
   map `dev.news-scheduler.example.com` to `127.0.0.1` in `/etc/hosts` on this Mac and open
   `http://dev.news-scheduler.example.com`. Otherwise, access the service without ingress:

   ```bash
   kubectl port-forward -n dev service/news-scheduler-svc 8080:80
   ```

   Then use `http://localhost:8080`. Readiness and liveness endpoints are at
   `/actuator/health/readiness` and `/actuator/health/liveness`.

### 1. Clone the Repository

```bash
git clone https://github.com/omkarchorghe16/news-scheduler-svc.git
cd news-scheduler-svc
```

### 2. Set Up Environment Variables

Export environment variables in your shell (Spring Boot does not automatically load a plain `.env` file):

```bash
# News APIs
export FINNHUB_API_KEY="your_finnhub_api_key"
export NEWSAPI_KEY="your_newsapi_key"

# Slack
export SLACK_WEBHOOK_URL="https://hooks.slack.com/services/YOUR/WEBHOOK/URL"
export SLACK_PORTFOLIO_WEBHOOK_URL="https://hooks.slack.com/services/YOUR/PORTFOLIO/URL"

# Telegram
export TELEGRAM_BOT_TOKEN="your_telegram_bot_token"
export TELEGRAM_CHAT_ID="your_telegram_chat_id"

# Twilio WhatsApp (optional; disabled by default)
export TWILIO_ACCOUNT_SID="your_account_sid"
export TWILIO_AUTH_TOKEN="your_auth_token"
export TWILIO_WHATSAPP_FROM="whatsapp:+1234567890"
export TWILIO_WHATSAPP_TO="whatsapp:+0987654321"
```

### 3. Build the Project

```bash
mvn clean install
```

### 4. Run Locally

**Run the application from Maven/IDE with PostgreSQL in Docker**
```bash
docker compose up -d postgres
export POSTGRES_URL="jdbc:postgresql://localhost:5433/news_scheduler"
export POSTGRES_USER="news_scheduler"
export POSTGRES_PASSWORD="your-local-postgres-password"
mvn spring-boot:run
```

The default Spring profile is `local`. Before starting Maven, set `POSTGRES_URL` to
`jdbc:postgresql://localhost:5433/news_scheduler`, `POSTGRES_USER` to `news_scheduler`, and
`POSTGRES_PASSWORD` to the local password configured in `.env`. For an IDE run, configure those
three environment variables in its run configuration. `.env` is read by Docker Compose, not
automatically by Maven or IDEs.

**Optional local schedule override (every 5 minutes)**
```bash
cp src/main/resources/application-local.yml application-local.yml
mvn spring-boot:run
```

### 5. Verify the Application

Check health endpoint:
```bash
curl http://localhost:8080/api/digest/health
```

Expected response:
```
Stock News Scheduler Service is running
```

## REST Endpoints

### Swagger / OpenAPI

With the application running, browse to `http://localhost:8080/swagger-ui/index.html` for interactive
API documentation. The generated OpenAPI JSON is available at `http://localhost:8080/v3/api-docs`.

### Stock Profiles

Fetch one or more symbols by POSTing a JSON list. Each provider has a separate endpoint:

```bash
curl -X POST http://localhost:8080/api/stocks/finnhub/profiles \
  -H "Content-Type: application/json" \
  -d '{"symbols":["AAPL","MSFT"]}'

curl -X POST http://localhost:8080/api/stocks/yahoo/profiles \
  -H "Content-Type: application/json" \
  -d '{"symbols":["AAPL","MSFT"]}'

curl -X POST http://localhost:8080/api/stocks/alphavantage/overview \
  -H "Content-Type: application/json" \
  -d '{"symbols":["AAPL","MSFT"]}'

curl -X POST http://localhost:8080/api/fmp/profiles \
  -H "Content-Type: application/json" \
  -d '{"symbols":["AAPL","MSFT"]}'
```

The Finnhub response includes company name, industry, IPO date, market capitalization (in millions),
logo, country, currency, exchange, shares outstanding, phone, and website. Finnhub's `stock/profile2`
does not provide a business summary. Yahoo Finance provides business summary, sector, industry,
employee count, company/address details, market capitalization, and the raw provider payload.
Alpha Vantage's OVERVIEW includes description, sector, industry, market capitalization, P/E, EPS,
margins, and other financial metrics when returned by the provider. Profiles are upserted by symbol
in provider-specific PostgreSQL tables.

FMP profiles use `FMP_API_KEY` and the `https://financialmodelingprep.com/stable` base URL by
default. You can override it with `FMP_BASE_URL`. The service enforces the configurable
`FMP_DAILY_REQUEST_LIMIT` (default 250 calls in a rolling 24-hour window), tracked in PostgreSQL.
Profiles are reused from PostgreSQL for `FMP_PROFILE_CACHE_HOURS` (default 24); set it to `0` to
fetch fresh data on every request.
FMP Basic/free accounts are US-exchange-only; requests containing `.NS` or `.BO` are rejected by
default. Set `FMP_US_EXCHANGES_ONLY=false` only when your FMP plan supports those symbols.
Every FMP profile record includes the complete raw provider record in `raw_payload`.

Finnhub requests are spaced to stay within the provider's 60-calls-per-minute limit for this endpoint.
Yahoo Finance is an unofficial API and may rate-limit requests or change its response format.
Alpha Vantage requests are limited to 25 per day and 5 per minute. The daily count is persisted in
PostgreSQL, and requests wait when the per-minute quota is reached. Each symbol consumes one call.
Create your own API key at [alphavantage.co](https://www.alphavantage.co/support/#api-key) and set
`ALPHA_VANTAGE_API_KEY` in your local environment or ignored `.env` file.
The Postman collection includes profile requests for all four providers and a validation test for an
empty symbol list. Import one collection, `postman/stock-news-scheduler.postman_collection.json`,
and the Local environment from `postman/`. The Local environment points to
`http://localhost:8080`; configure Finnhub, Alpha Vantage, and FMP API keys in the running application
to run Stock Profiles. The Yahoo request does not require a key.

The collection includes requests that create/update/delete database records, trigger digests and
notifications, and call provider APIs. Review the request before sending it because some requests
have database, notification, or provider side effects.

### Adding Stock Symbols

Add a list of ticker symbols to an existing sector. The service normalizes tickers, creates records
that do not already exist in that sector, and reports already-present tickers in `skipped`:

```bash
curl -X POST http://localhost:8080/api/stock-symbols \
  -H "Content-Type: application/json" \
  -d '{"sectorId":3,"tickers":["AAPL","MSFT","NVDA"]}'
```

The request requires a non-empty `tickers` array and an existing `sectorId`. The response contains
`created` stock records and a `skipped` ticker list. The existing `POST /api/stocks/bulk` endpoint
continues to support the same bulk-creation operation.

### On-Demand Triggers

#### Trigger Daily Digest
```bash
curl -X POST http://localhost:8080/api/digest/run
```

Response:
```json
{
  "message": "Daily digest executed successfully"
}
```

#### Trigger Portfolio Digest
```bash
curl -X POST http://localhost:8080/api/digest/portfolio
```

Response:
```json
{
  "message": "Portfolio digest executed successfully"
}
```

#### Health Check
```bash
curl http://localhost:8080/api/digest/health
```

Response:
```
Stock News Scheduler Service is running
```

## Configuration

All configuration is managed through `application.yml` and environment variables. Edit `src/main/resources/application.yml` to override defaults.

### Key Configuration Properties

```yaml
app:
  notifications:
    slack:
      webhook-url: ${SLACK_WEBHOOK_URL}  # Main channel
      portfolio-webhook-url: ${SLACK_PORTFOLIO_WEBHOOK_URL}  # Portfolio channel
    telegram:
      bot-token: ${TELEGRAM_BOT_TOKEN}
      chat-id: ${TELEGRAM_CHAT_ID}
    whatsapp:
      enabled: false  # Disabled by default
      twilio-sid: ${TWILIO_ACCOUNT_SID}
      twilio-token: ${TWILIO_AUTH_TOKEN}
      from-number: ${TWILIO_WHATSAPP_FROM}
      to-number: ${TWILIO_WHATSAPP_TO}
  apis:
    finnhub-key: ${FINNHUB_API_KEY:}
    newsapi-key: ${NEWSAPI_KEY:}
    finnhub-base-url: ${FINNHUB_BASE_URL:https://finnhub.io/api/v1}
    yahoo-base-url: ${YAHOO_FINANCE_BASE_URL:https://query1.finance.yahoo.com}
    alpha-vantage-key: ${ALPHA_VANTAGE_API_KEY:}
    alpha-vantage-base-url: ${ALPHA_VANTAGE_BASE_URL:https://www.alphavantage.co/query}
    timeout-seconds: 5  # HTTP timeout
    max-retries: 1  # Retry attempts
  watchlist:
    us-symbols:  # Top 10 US stocks fetched daily
      - AAPL
      - GOOGL
      - MSFT
      - TSLA
      - META
      - AMZN
      - NVDA
      - JPM
      - V
      - WMT
    nse-symbols:  # Top 10 India stocks fetched daily
      - RELIANCE.NS
      - INFY.NS
      - TCS.NS
      - WIPRO.NS
      - LT.NS
      - MARUTI.NS
      - BAJAJ-AUTO.NS
      - ICICIBANK.NS
      - SBIN.NS
      - BHARTIARTL.NS
    portfolio-symbols: []  # Optional: specific symbols for portfolio digest
  schedule:
    cron-expression: "0 0 9 * * MON-FRI"  # 9 AM weekdays
    timezone: "America/Chicago"
```

### Stock News API Keys

The service supports two news providers. At least one key is required to receive news:

- **Finnhub (recommended for stock-market news):** create a free account at
  [finnhub.io](https://finnhub.io/register), then copy the API key from the dashboard.
  This provider supplies general market news and company news for the configured watchlist.
- **NewsAPI (optional):** create an account at [newsapi.org](https://newsapi.org/register),
  then copy the key from the account page. This provider supplies search-based market news.

Do not paste keys into Git. Configure them as environment variables before starting Spring Boot:

```bash
export FINNHUB_API_KEY="paste-your-finnhub-key-here"
export NEWSAPI_KEY="paste-your-newsapi-key-here" # optional
export ALPHA_VANTAGE_API_KEY="paste-your-alpha-vantage-key-here" # optional
mvn spring-boot:run
```

For local development, you can instead create an untracked `application-secrets.yml` in the project
root. This filename is in `.gitignore` and is imported by the application. Example:

```yaml
app:
  apis:
    finnhub-key: "your-finnhub-key"
    newsapi-key: "your-newsapi-key"
    alpha-vantage-key: "your-alpha-vantage-key"
  notifications:
    telegram:
      bot-token: "your-telegram-bot-token"
      chat-id: "your-telegram-chat-id"
spring:
  datasource:
    username: "your-postgres-user"
    password: "your-postgres-password"
```

Never commit this local file or package it into the application image.

In IntelliJ IDEA, open **Run | Edit Configurations**, select `StockNewsApplication`, and add
`POSTGRES_USER=...`, `POSTGRES_PASSWORD=...`, and any provider variables such as
`FINNHUB_API_KEY=...`, `NEWSAPI_KEY=...`, or `ALPHA_VANTAGE_API_KEY=...` under **Environment variables**.
The placeholders in `src/main/resources/application.yml` read these values automatically.

The default schedule runs once at 09:00 America/Chicago on weekdays; it is not a continuous
real-time stream. Finnhub is the primary source for stock news, while NewsAPI free plans may
have provider-specific delays and usage limits.

## Scheduling

The service runs on a configurable cron expression. Default is **9:00 AM on weekdays (Mon-Fri) in America/Chicago timezone**.

To change the schedule, modify the cron expression in `application.yml`:

```yaml
app:
  schedule:
    cron-expression: "0 0 9 * * MON-FRI"  # Cron format: second, minute, hour, day, month, day-of-week
    timezone: "America/Chicago"
```

### Common Cron Examples

- `0 0 9 * * MON-FRI` - 9 AM on weekdays
- `0 0 9,17 * * MON-FRI` - 9 AM and 5 PM on weekdays
- `0 */30 * * * *` - Every 30 minutes (for testing)
- `0 0 8 * * *` - Daily at 8 AM

## Deduplication

The service stores SHA-256 hashes of sent article URLs in PostgreSQL.
Articles sent within the last **3 days** are excluded from future digests.

Old records are automatically cleaned up on application startup. To manually clean up:

Edit `DigestService.java` and adjust:
```java
private static final int DEDUP_DAYS = 3;  // Change to desired number
```

## Database setup

The application reads and writes data through Spring Data JPA repositories in
`model`, using the PostgreSQL datasource. Hibernate creates or updates
the PostgreSQL tables from the JPA entities at startup. H2 is used only for automated tests.

Create a PostgreSQL database and user using pgAdmin or `psql`, for example:

```sql
CREATE USER news_scheduler WITH PASSWORD 'set-a-local-password';
CREATE DATABASE news_scheduler OWNER news_scheduler;
```

For the Compose database, the host-run app defaults to
`POSTGRES_URL=jdbc:postgresql://localhost:5433/news_scheduler` and `POSTGRES_USER=news_scheduler`;
set `POSTGRES_PASSWORD` to the local password configured for the container. For a manually installed
PostgreSQL server on port 5432, override `POSTGRES_URL` to
`jdbc:postgresql://localhost:5432/news_scheduler`. The application writes to the database in the
effective `POSTGRES_URL` (including overrides in `application-secrets.yml`); inspect that exact
database in pgAdmin or IntelliJ. Do not put credentials into committed configuration. Hibernate
updates PostgreSQL tables on startup.

Hibernate creates or updates these PostgreSQL tables from the corresponding JPA entities:

| Table | Contents |
| --- | --- |
| `FINNHUB_STOCK_PROFILES` | Finnhub company profile fields, market capitalization, and raw provider JSON |
| `YAHOO_STOCK_PROFILES` | Yahoo company summary, sector, industry, employee count, and raw provider JSON |
| `ALPHA_VANTAGE_OVERVIEWS` | Alpha Vantage description and financial metrics |
| `ALPHA_VANTAGE_API_CALLS` | Persisted quota history for the daily/minute provider limits |
| `FMP_STOCK_PROFILES` | FMP profile fields and complete raw provider records |
| `FMP_API_CALLS` | FMP requests tracked for the rolling daily quota |
| `SENT_ARTICLES` | Article URL hashes used for digest deduplication |

### Browse PostgreSQL data in IntelliJ IDEA or pgAdmin

Add a PostgreSQL data source matching the effective `POSTGRES_URL`, username, and password from the
running application. Refresh its `public` schema, then open the profile
tables or use the SQL console:

```sql
SELECT * FROM finnhub_stock_profiles ORDER BY updated_at DESC;
SELECT * FROM yahoo_stock_profiles ORDER BY updated_at DESC;
SELECT * FROM alpha_vantage_overviews ORDER BY updated_at DESC;
SELECT * FROM fmp_stock_profiles ORDER BY updated_at DESC;
SELECT called_at FROM fmp_api_calls ORDER BY called_at DESC;
SELECT * FROM sent_articles ORDER BY sent_at DESC;
```

## Running Tests

```bash
mvn test
```

### Test Coverage

- **Unit Tests**:
  - `DigestFormatterTest`: Message formatting for each channel
  - `DigestServiceTest`: Deduplication logic
  - `SlackNotifierTest`, `TelegramNotifierTest`, `WhatsAppNotifierTest`: Notifier configuration

- **Integration Tests**:
  - `StockNewsApplicationTest`: Spring context loads successfully
  - `DigestTriggerControllerTest`: REST endpoints respond correctly

## Logging

The application writes logs to the console (stdout). Profile endpoint logs include provider, request
symbol count, and saved/returned record counts; provider failures log the symbol and exception type.
API credentials, authorization data, and full provider payloads must not be logged.

The digest workflow logs events similar to:

```
2024-09-13 09:00:00 - Starting daily digest job
2024-09-13 09:00:01 - Fetching US market news...
2024-09-13 09:00:02 - Fetched 15 US news items
2024-09-13 09:00:02 - Fetching India market news...
2024-09-13 09:00:03 - Fetched 12 India news items
2024-09-13 09:00:03 - After dedup: 10 US items, 8 India items
2024-09-13 09:00:04 - Sending US digest to Telegram
2024-09-13 09:00:04 - Telegram message sent successfully
2024-09-13 09:00:05 - Sending India digest to Telegram
2024-09-13 09:00:05 - Telegram message sent successfully
2024-09-13 09:00:06 - Daily digest job completed
```

Configure log levels in `application.yml`:

```yaml
logging:
  level:
    root: INFO
    com.stocknews: DEBUG  # Change to INFO for less verbose logs
```

## Troubleshooting

### Application won't start
- Check all required environment variables are set
- Verify Java 21 is installed: `java -version`
- Check Maven version: `mvn -version` (3.6+)

### Digests not sending
- Verify webhook URLs and API keys in environment variables
- Check logs for HTTP errors
- Confirm firewall allows outbound HTTPS connections

### No articles found
- Verify Finnhub and NewsAPI keys are valid
- Check API rate limits (free tiers have limits)
- Confirm market symbols in watchlist are correct

### Deduplication not working
- Check that `SentArticleRepository` is saving records to PostgreSQL

### WhatsApp messages not sending
- Confirm WhatsApp is enabled: `app.notifications.whatsapp.enabled=true`
- Verify Twilio credentials are correct
- Check if message template is approved (Twilio Sandbox requirement)
- Ensure both `from-number` and `to-number` are in WhatsApp format: `whatsapp:+1234567890`

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                   DigestScheduler (cron)                    │
└─────────────┬───────────────────────────────────────────────┘
              │
              ▼
┌─────────────────────────────────────────────────────────────┐
│                    DigestService                            │
│  (Orchestrates: Fetch → Dedup → Format → Notify)          │
└─────────┬──────────┬──────────────────┬──────────────────────┘
          │          │                  │
    ┌─────▼──┐  ┌────▼────┐       ┌────▼─────┐
    │Finnhub │  │ NewsAPI │       │Watchlist │
    │Client  │  │ Client  │       │  Symbols │
    └────────┘  └─────────┘       └──────────┘
          │          │                  │
          └─────────┬────────────────────┘
                    ▼
          ┌──────────────────┐
          │  NewsItem List   │
          └────────┬─────────┘
                   │
                   ▼
          ┌──────────────────┐
          │ Dedup (SHA-256)  │◄──────┐
          │ vs PostgreSQL    │       │
          └────────┬─────────┘       │
                   │         ┌───────┴────────┐
                   │         │  SentArticle   │
                   │         │  Repository    │
                   ▼         └────────────────┘
          ┌──────────────────┐
          │ DigestFormatter  │
          │ (Markdown/JSON)  │
          └────────┬─────────┘
                   │
         ┌─────────┼─────────┬──────────┐
         ▼         ▼         ▼          ▼
       Slack   Telegram  WhatsApp  REST Response
```

## Contributing

To extend the application:

1. **Add a new news source**: Implement `NewsSourceClient` interface
2. **Add a new notifier**: Implement `Notifier` interface
3. **Change schedule**: Modify cron expression in config
4. **Add new watchlist**: Extend `Watchlist` component
5. **Document changes**: Update the README and directly affected docs, and review all
   `.github/skills/*/SKILL.md` files; update every skill affected by the implementation or shared
   project conventions.

## License

MIT

## Support

For issues and feature requests, open an issue on GitHub.
