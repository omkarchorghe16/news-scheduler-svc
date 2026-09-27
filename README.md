# Stock News Scheduler Service

A complete Java Spring Boot application that fetches daily stock market news for a configurable watchlist (US and India markets) and sends formatted digests to Slack, Telegram, and WhatsApp on a schedule.

## Features

- **Scheduled Digest**: Automatic daily news digest (configurable cron, default: 9 AM weekdays)
- **Multi-Market Support**: Separate fetches for US (NYSE/NASDAQ) and India (NSE) markets
- **Multiple News Sources**: Integrates Finnhub and NewsAPI.org
- **Smart Deduplication**: SHA-256 based dedup with 3-day history stored in H2 database
- **Multi-Channel Notifications**: Slack, Telegram, WhatsApp (Twilio)
- **Portfolio Tracking**: Separate digest for configured portfolio symbols (Slack-only)
- **On-Demand Triggers**: REST endpoints to manually trigger digests without waiting for schedule
- **Stock Profiles**: Fetch Finnhub and Yahoo Finance company profiles and persist them separately in H2
- **Development Standards**: Repository-specific Copilot instructions for coding, architecture, Spring Boot, and logging
- **Configurable Everything**: @ConfigurationProperties binds all settings from environment variables

## Tech Stack

- **Java 21**
- **Spring Boot 3.5.16**
- **Maven**
- **Spring Data JPA + H2 Database**
- **Spring RestClient (6.1+)**
- **Lombok**
- **SLF4J Logging**

## Project Structure

```
com/stocknews/
├── StockNewsApplication.java
├── config/
│   ├── AppProperties.java          # @ConfigurationProperties for all external config
│   └── RestClientConfig.java       # RestClient bean with timeout/retry config
├── scheduler/
│   └── DigestScheduler.java        # Scheduled cron job
├── digest/
│   ├── DigestService.java          # Orchestrates fetch → dedup → format → notify
│   ├── DigestFormatter.java        # Formats messages for each channel
│   └── Watchlist.java              # Manages US/NSE symbol lists
├── news/
│   ├── NewsItem.java               # Record: title, url, source, publishedAt, symbol, market
│   ├── NewsSourceClient.java       # Interface
│   ├── FinnhubNewsClient.java      # Finnhub API integration
│   └── NewsApiClient.java          # NewsAPI.org integration
├── notify/
│   ├── Notifier.java               # Interface
│   ├── SlackNotifier.java          # Slack Webhook
│   ├── TelegramNotifier.java       # Telegram Bot API
│   └── WhatsAppNotifier.java       # Twilio WhatsApp API
├── persistence/
│   ├── SentArticle.java            # JPA entity
│   ├── FinnhubStockProfile.java    # Finnhub profile entity
│   ├── YahooStockProfile.java      # Yahoo profile entity
│   └── *Repository.java            # Spring Data JPA repositories
├── stockprofile/
│   ├── StockProfileService.java    # Profile fetch and persistence workflow
│   └── *StockProfileClient.java    # Provider integrations
└── web/
    ├── DigestTriggerController.java
    └── StockProfileController.java # REST endpoints
```

Repository coding guidance lives in `.github/copilot-instructions.md` and the focused files in
`.github/instructions/`. These set the expected coding, architecture, Spring Boot, and logging
standards for future Copilot changes.

## Prerequisites

### Required API Keys & Credentials

1. **Finnhub API** (optional but recommended for US market news)
   - Sign up: https://finnhub.io/
   - Get API key from dashboard

2. **NewsAPI.org** (optional but recommended for general market news)
   - Sign up: https://newsapi.org/
   - Get API key from dashboard

3. **Slack** (optional for Slack notifications)
   - Create an Incoming Webhook: https://api.slack.com/apps/
   - Webhook URL format: `https://hooks.slack.com/services/YOUR/WEBHOOK/URL`
   - Optionally create a second webhook for portfolio channel

4. **Telegram** (optional for Telegram notifications)
   - Create a bot via @BotFather on Telegram
   - Get Bot Token and Chat ID

5. **Twilio WhatsApp** (optional for WhatsApp notifications)
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
The import is optional, so the application can still start when the file is absent. This is suitable
for local development only; for deployed environments, use the platform secret store or environment
variables rather than copying this file into the container/image.

For deployment, save each credential in the deployment platform's secret store (or a cloud secret
manager such as AWS Secrets Manager, Azure Key Vault, or Google Secret Manager), then expose it to
the application as an environment variable. Configure `FINNHUB_API_KEY`, `NEWSAPI_KEY`, and any
notification credentials there; Spring resolves them via the `${VARIABLE:}` placeholders in
`application.yml`. Keep different credentials for dev, test, and production, and never put secrets
in the image, source repository, or plain deployment manifests. For CI tests, supply test credentials
through the CI platform's protected secrets; tests that mock providers do not need a live key.

Credentials pasted into chat or other shared systems should be treated as exposed. Revoke/regenerate
credentials immediately if they are accidentally exposed.

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

The default Spring profile is `local`. The H2 web console is enabled only for that profile and is
available at `http://localhost:8080/h2-console`. Deployments should explicitly set a non-local profile
(for example, `SPRING_PROFILES_ACTIVE=prod`) so the development console is not enabled.

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

### Stock Profiles

Fetch one or more symbols by POSTing a JSON list. Each provider has a separate endpoint and H2 table:

```bash
curl -X POST http://localhost:8080/api/stocks/finnhub/profiles \
  -H "Content-Type: application/json" \
  -d '{"symbols":["AAPL","MSFT"]}'

curl -X POST http://localhost:8080/api/stocks/yahoo/profiles \
  -H "Content-Type: application/json" \
  -d '{"symbols":["AAPL","MSFT"]}'
```

The Finnhub response includes company name, industry, IPO date, market capitalization (in millions),
logo, country, currency, exchange, shares outstanding, phone, and website. Finnhub's `stock/profile2`
does not provide a business summary. Yahoo Finance provides business summary, sector, industry,
employee count, company/address details, market capitalization, and the raw provider payload.
Profiles are upserted by symbol in `finnhub_stock_profiles` and `yahoo_stock_profiles`.

Finnhub requests are spaced to stay within the provider's 60-calls-per-minute limit for this endpoint.
Yahoo Finance is an unofficial API and may rate-limit requests or change its response format.
The Postman collection includes success/data assertions for both providers and a validation test for
an empty symbol list. Import `postman/stock-news-scheduler.postman_collection.json`, select the local
environment, start the app with `FINNHUB_API_KEY` set to run the Finnhub request, then run the
Stock Profiles folder. The Yahoo request does not require a key.

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
    finnhub-key: ${FINNHUB_API_KEY}
    newsapi-key: ${NEWSAPI_KEY}
    finnhub-base-url: ${FINNHUB_BASE_URL:https://finnhub.io/api/v1}
    yahoo-base-url: ${YAHOO_FINANCE_BASE_URL:https://query1.finance.yahoo.com}
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
mvn spring-boot:run
```

In IntelliJ IDEA, open **Run | Edit Configurations**, select `StockNewsApplication`, and add
`FINNHUB_API_KEY=...` (and optionally `NEWSAPI_KEY=...`) under **Environment variables**.
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

The service stores SHA-256 hashes of sent article URLs in the persistent H2 database. Articles sent
within the last **3 days** are excluded from future digests.

Old records are automatically cleaned up on application startup. To manually clean up:

Edit `DigestService.java` and adjust:
```java
private static final int DEDUP_DAYS = 3;  // Change to desired number
```

## H2 Database

The default datasource is a persistent H2 file database at `./data/news-scheduler` (relative to the
directory from which the application is started). H2 stores the database files under `data/`,
including `news-scheduler.mv.db`. The `data/` directory is ignored by Git.

The profile endpoints create/update these tables:

| Table | Contents |
| --- | --- |
| `FINNHUB_STOCK_PROFILES` | Finnhub company profile fields, market capitalization, and raw provider JSON |
| `YAHOO_STOCK_PROFILES` | Yahoo company summary, sector, industry, employee count, and raw provider JSON |
| `SENT_ARTICLES` | Article URL hashes used for digest deduplication |

### Browse the database in IntelliJ IDEA

1. Open **View → Tool Windows → Database**, then add a data source of type **H2**.
2. Set the JDBC URL to `jdbc:h2:file:./data/news-scheduler;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;MODE=PostgreSQL`.
   Use `org.h2.Driver`, username `sa`, and an empty password. Resolve/download the H2 driver if prompted.
3. Set the data source working directory to the project root (the directory containing `pom.xml`) if
   IntelliJ does not resolve the relative `./data` path there.
4. Click **Test Connection**, apply, and refresh the schema. Expand the `PUBLIC` schema and open either
   stock profile table to view the saved rows and `UPDATED_AT` timestamps.

You can also run SQL from the IDE console:

```sql
SELECT SYMBOL, NAME, INDUSTRY, MARKET_CAPITALIZATION, UPDATED_AT
FROM FINNHUB_STOCK_PROFILES
ORDER BY UPDATED_AT DESC;

SELECT SYMBOL, NAME, SECTOR, INDUSTRY, FULL_TIME_EMPLOYEES, MARKET_CAPITALIZATION, UPDATED_AT
FROM YAHOO_STOCK_PROFILES
ORDER BY UPDATED_AT DESC;
```

### Local H2 web console

The default `local` profile loads `src/main/resources/application-local.yml`, which enables the
console at `http://localhost:8080/h2-console`. Log in with JDBC URL
`jdbc:h2:file:./data/news-scheduler;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;MODE=PostgreSQL`,
user `sa`, and a blank password. If the app is running with another explicitly active profile, the
console is off. Do not enable or expose it in shared or production environments.

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
- Restart the application to clear H2 in-memory database
- Check that `SentArticleRepository` is saving records

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
          │ vs H2 Database   │       │
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

## License

MIT

## Support

For issues and feature requests, open an issue on GitHub.
