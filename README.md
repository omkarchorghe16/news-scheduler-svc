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
- **AWS Deployment**: GitHub Actions OIDC, Amazon ECR, ECS Fargate, private RDS PostgreSQL, and Secrets Manager
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

`application.yml` reads secrets from environment variables (for example, `FINNHUB_API_KEY`);
do not put real values in the committed file. For local development, export the values in your
shell before starting the app:

```bash
export FINNHUB_API_KEY="your-finnhub-key"
export NEWSAPI_KEY="your-newsapi-key"
export ALPHA_VANTAGE_API_KEY="your-alpha-vantage-key"
export FMP_API_KEY="your-fmp-key"
export POSTGRES_URL="jdbc:postgresql://localhost:5432/news_scheduler"
export POSTGRES_USER="news_scheduler"
export POSTGRES_PASSWORD="your-local-postgres-password"
mvn spring-boot:run
```

To keep them across terminal sessions, add these exports to a local shell profile that is not
committed, or use your IDE's **Run Configuration → Environment variables**. Do not paste secrets
into Postman collection/environment exports or commit terminal/IDE configuration files that contain them.

An optional local YAML override is also supported: copy
`src/main/resources/application-local.yml.example` to the repository root as
`application-local.yml`, then run with the `local` Spring profile. That filename is in `.gitignore`.
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
The import is optional for direct local runs. The local Docker Compose setup below mounts this file
read-only into the running container; it is excluded from the Docker build context and image. For
deployed environments, use the platform secret store or environment variables instead.

For deployment, save each credential in the deployment platform's secret store (or a cloud secret
manager such as AWS Secrets Manager, Azure Key Vault, or Google Secret Manager), then expose it to
the application as an environment variable. Configure `FINNHUB_API_KEY`, `NEWSAPI_KEY`, and any
notification credentials there; Spring resolves them via the `${VARIABLE:}` placeholders in
`application.yml`. Keep different credentials for dev, test, and production, and never put secrets
in the image, source repository, or plain deployment manifests. For CI tests, supply test credentials
through the CI platform's protected secrets; tests that mock providers do not need a live key.

Credentials pasted into chat or other shared systems should be treated as exposed. Revoke/regenerate
credentials immediately if they are accidentally exposed.

### Run Spring Boot and PostgreSQL in Docker for development

Compose automatically reads `.env` from the repository root; it does not read `.env.example`.
`.env.example` is a tracked template so the required local settings are clear without committing a
password. Create the ignored runtime file from the template:

```bash
cp .env.example .env
```

Then set a development-only `POSTGRES_PASSWORD` in `.env`, as well as the
`FMP_API_KEY`, `FINNHUB_API_KEY`, `NEWSAPI_KEY`, `ALPHA_VANTAGE_API_KEY`, and notification
credentials you use.
Compose passes these environment variables to Spring Boot, where `application.yml` already maps them
to the corresponding settings. Database credentials also come from `.env`. The app container no
longer needs `application-secrets.yml`; that file remains an optional approach for direct local
Spring Boot runs and the separate Kubernetes setup. Never commit `.env`; it is ignored by Git.
The template also includes provider URLs/limits, WhatsApp enablement, provider timeout/retries, and
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
   syncs the local secrets file into the cluster, applies the manifests, and restarts/waits for the
   Deployment:

   ```bash
   bash scripts/deploy-local-k8s.sh local
   kubectl get pods,services,ingress -n dev
   kubectl logs -n dev deployment/news-scheduler-svc
   ```

   Rebuild the image before redeploying after application changes. On every deployment, the helper
   refreshes the cluster Secret from the local file and restarts the pod, so updates to credentials
   are picked up. The Actuator dependency and health probe settings are already enabled in `pom.xml`
   and `application.yml`.

5. The ingress manifest expects an NGINX Ingress Controller with class `nginx`. If one is installed,
   map `dev.news-scheduler.example.com` to `127.0.0.1` in `/etc/hosts` on this Mac and open
   `http://dev.news-scheduler.example.com`. Otherwise, access the service without ingress:

   ```bash
   kubectl port-forward -n dev service/news-scheduler-svc 8080:80
   ```

   Then use `http://localhost:8080`. Readiness and liveness endpoints are at
   `/actuator/health/readiness` and `/actuator/health/liveness`.

### Deploy to AWS ECS and RDS

`infra/aws/cloudformation.yml` provisions an ECR repository, an ECS Fargate service, a public ALB
with HTTPS, private RDS for PostgreSQL, Secrets Manager secrets, CloudWatch logs, and a GitHub OIDC
deployment role in `us-east-2`. The Fargate service starts with zero tasks until the first successful
CI deployment. It is intentionally kept at one task because each instance runs the scheduled digest.
The RDS instance is private and encrypted. Fargate tasks run in public subnets with public IPs so they
can call news/notification providers without a NAT gateway; their security group accepts application
traffic only from the ALB. This avoids NAT gateway charges but is a cost-conscious starting topology,
not a substitute for a reviewed production network design.

Before provisioning:

1. Request and validate an ACM certificate in `us-east-2` for the public API hostname. The DNS name
   must resolve to the ALB created by the stack.
2. Ensure the AWS account does not already have a GitHub Actions OIDC provider for
   `token.actions.githubusercontent.com`. If it exists, pass its ARN through
   `ExistingGitHubOidcProviderArn`; otherwise CloudFormation creates it. The provider must list
   `sts.amazonaws.com` as a client ID.
3. Deploy the stack, substituting your ACM certificate ARN:

   ```bash
   aws cloudformation deploy \
     --template-file infra/aws/cloudformation.yml \
     --stack-name news-scheduler-svc \
     --region us-east-2 \
     --capabilities CAPABILITY_NAMED_IAM \
     --parameter-overrides \
       GitHubOwner=omkarchorghe16 \
       GitHubOwnerId=75207496 \
       GitHubRepositoryName=news-scheduler-svc \
       GitHubRepositoryId=1368822070 \
       CertificateArn=arn:aws:acm:us-east-2:ACCOUNT_ID:certificate/CERTIFICATE_ID \
       CreateMigrationHost=true \
       ScheduleCronExpression="0 0 9 * * MON-FRI" \
       ScheduleTimeZone=America/Chicago
   ```

If the account already has the GitHub OIDC provider, add this parameter to the deployment command,
using the existing provider ARN:

```text
ExistingGitHubOidcProviderArn=arn:aws:iam::ACCOUNT_ID:oidc-provider/token.actions.githubusercontent.com
```

This repository was created after July 15, 2026, so GitHub issues OIDC tokens with immutable
subjects that include the owner ID (`75207496`) and repository ID (`1368822070`). The package job's
subject is `repo:omkarchorghe16@75207496/news-scheduler-svc@1368822070:ref:refs/heads/main`; the
deploy job's subject uses the same prefix followed by `:environment:staging`. Name-only trust
subjects do not match this repository's tokens.

The CI workflow assumes the role named `GitHubActionsRole`. For an existing role with that name,
update its trust policy to match
[`infra/aws/github-oidc-trust-policy.json`](infra/aws/github-oidc-trust-policy.json). That policy
allows the package job's `main` branch token and the deploy job's `staging` environment token, and
only the GitHub OIDC provider in AWS account `221934031392`. Apply it to the existing role with:

```bash
aws iam update-assume-role-policy \
  --role-name GitHubActionsRole \
  --policy-document file://infra/aws/github-oidc-trust-policy.json
```

If `GitHubActionsRole` is managed separately from this CloudFormation stack, grant it the ECR
authentication and repository-scoped image-publish actions used by CI:

```bash
aws iam put-role-policy \
  --role-name GitHubActionsRole \
  --policy-name PublishNewsSchedulerImage \
  --policy-document file://infra/aws/github-actions-ecr-policy.json
```

Confirm the provider ARN and its `sts.amazonaws.com` client ID match the trust policy. Set GitHub's
repository variable `AWS_ROLE_ARN` to
`arn:aws:iam::221934031392:role/GitHubActionsRole` (or the `GitHubActionsRoleArn` stack output if
CloudFormation manages the role). The ECR repository must also exist before CI can push an image.
If creating the stack around an already existing role, CloudFormation cannot adopt that role
automatically; apply the trust and permission policies above, or arrange to import the role into
the stack before deploying the template.

The temporary migration host is an SSM-managed EC2 instance with no inbound ports. It allows the
Compose PostgreSQL data to be restored into private RDS without making the database internet
accessible. Install the AWS CLI, Session Manager plugin, and PostgreSQL 17 client tools locally.
Export the Compose data before connecting:

```bash
pg_dump --host localhost --port 5433 --username news_scheduler \
  --dbname news_scheduler --format=custom --no-owner --no-acl \
  --file news_scheduler.dump
```

Read the `MigrationHostInstanceId`, `DatabaseEndpoint`, `DatabaseSecretArn`, and
`ApplicationDatabaseSecretArn` stack outputs. Retrieve the generated master and application
credentials from Secrets Manager without placing them in source control or command history. Start
an SSM port-forward (leave this terminal open):

```bash
aws ssm start-session \
  --target MIGRATION_INSTANCE_ID \
  --document-name AWS-StartPortForwardingSessionToRemoteHost \
  --parameters '{"host":["RDS_ENDPOINT"],"portNumber":["5432"],"localPortNumber":["15432"]}' \
  --region us-east-2
```

In another terminal, connect as the generated RDS master user. Create the restricted runtime role
using the generated password from `ApplicationDatabaseSecretArn`, then make it the database owner:

```bash
export PGSSLMODE=require
psql --host 127.0.0.1 --port 15432 --username newsadmin \
  --dbname postgres --set ON_ERROR_STOP=1
```

At the `psql` prompt, run:

```sql
CREATE ROLE news_scheduler_app LOGIN;
\password news_scheduler_app
ALTER DATABASE news_scheduler OWNER TO news_scheduler_app;
\connect news_scheduler
ALTER SCHEMA public OWNER TO news_scheduler_app;
\q
```

Restore the dump as `news_scheduler_app` so the imported tables are owned by the runtime role. For a
fresh database, skip the dump/restore but still create the runtime role:

```bash
pg_restore --host 127.0.0.1 --port 15432 --username news_scheduler_app \
  --dbname news_scheduler --no-owner --no-acl news_scheduler.dump
```

In Secrets Manager, replace the empty values in the `ApplicationSecretArn` JSON with the provider
and notification credentials you use. The generated application database secret's password must
match the value entered with `\password`; set `WHATSAPP_ENABLED` to `"true"` only when WhatsApp
delivery should be active. `ScheduleCronExpression` and `ScheduleTimeZone` are CloudFormation
parameters if the default schedule is not appropriate. A running task does not automatically reload
changed secret values; force a new ECS deployment after rotating them:

```bash
aws ecs update-service --cluster news-scheduler --service news-scheduler-svc \
  --force-new-deployment --region us-east-2
```

After confirming the restored data, redeploy the stack with `CreateMigrationHost=false` to remove
the temporary host and its RDS security-group access:

```bash
aws cloudformation deploy \
  --template-file infra/aws/cloudformation.yml \
  --stack-name news-scheduler-svc \
  --region us-east-2 \
  --capabilities CAPABILITY_NAMED_IAM \
  --parameter-overrides \
    GitHubOwner=omkarchorghe16 \
    GitHubOwnerId=75207496 \
    GitHubRepositoryName=news-scheduler-svc \
    GitHubRepositoryId=1368822070 \
    GitHubOwner=omkarchorghe16 \
    GitHubOwnerId=75207496 \
    GitHubRepositoryName=news-scheduler-svc \
    GitHubRepositoryId=1368822070 \
    CertificateArn=arn:aws:acm:us-east-2:ACCOUNT_ID:certificate/CERTIFICATE_ID \
    CreateMigrationHost=false \
    DatabaseInstanceClass=db.t4g.micro \
    DatabaseMultiAZ=false \
    ScheduleCronExpression="0 0 9 * * MON-FRI" \
    ScheduleTimeZone=America/Chicago
```

In GitHub repository **Settings → Secrets and variables → Actions → Variables**, set:

| Variable | Value |
| --- | --- |
| `AWS_ROLE_ARN` | The `GitHubActionsRoleArn` CloudFormation output (`arn:aws:iam::221934031392:role/GitHubActionsRole` for this account) |
| `AWS_REGION` | `us-east-2` |
| `PUBLIC_BASE_URL` | The HTTPS URL for the DNS name covered by the ACM certificate |

Create a GitHub Environment named `staging`, restrict its deployment branches to `main`, and configure
required reviewers if deployments need approval. The CloudFormation role trusts only this
repository's `main` branch and the `staging` environment. GitHub Actions exchanges its OIDC token
for short-lived AWS credentials; do not add
long-lived AWS access keys to GitHub. On pushes to `main`, the workflow tests the app, publishes a
commit-tagged image to ECR, updates the ECS service, waits for stability, and checks health,
metrics, and Prometheus endpoints over HTTPS. Pull requests build and test without AWS credentials.

The HTTPS ALB makes the existing application routes publicly reachable; HTTPS encrypts transport
but does not add application authentication. Review endpoint access and add the intended
authentication or network restrictions before using this as a public production API.

The Docker Desktop Kubernetes manifests and `scripts/deploy-local-k8s.sh` remain available for
single-user local testing only. They are no longer used by the AWS CI/CD workflow.

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

**Run with the default local profile**
```bash
mvn spring-boot:run
```

The default Spring profile is `local`. PostgreSQL must be running and the `news_scheduler`
database/user must exist; see [Database setup](#database-setup).

**Optional local schedule override (every 5 minutes)**
```bash
cp src/main/resources/application-local.yml.example application-local.yml
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
`ALPHA_VANTAGE_API_KEY` in your environment or secret store.
The Postman collection includes profile requests for all four providers and a validation test for an
empty symbol list. Import `postman/stock-news-scheduler.postman_collection.json`, select the local
environment, configure Finnhub, Alpha Vantage, and FMP API keys in the running application to run
those requests, then run the Stock Profiles folder. The Yahoo request does not require a key.

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

Never commit this local file. For deployed dev/test environments, add these as environment
variables/secrets in the deployment platform's secret manager or CI/CD environment configuration;
do not package `application-secrets.yml` into the deployment artifact.

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

Set `POSTGRES_URL`, `POSTGRES_USER`, and `POSTGRES_PASSWORD` in the shell/IDE before running. For
example, use `jdbc:postgresql://localhost:5432/news_scheduler` if you created the database above.
The application writes to the database in the effective `POSTGRES_URL` (including overrides in
`application-secrets.yml`); inspect that exact database in pgAdmin or IntelliJ. Do not put credentials
into committed configuration. Hibernate updates PostgreSQL tables on startup.

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
