# Personal Budget Manager

A simple REST API for managing personal finances. This application allows tracking incomes and expenses associated with specific accounts.

## Features

The application operates on two main resources:

### Account
- Name (e.g., "Main Account", "Savings")
- Balance — automatically updated after each transaction

### Transaction
- Amount (must be strictly > 0)
- Type: `INCOME` or `EXPENSE`
- Category (e.g., "Food", "Transport", "Salary")
- Optional description
- Transaction date
- Associated account

## Endpoints

### Accounts
- `GET /api/accounts` - List all accounts
- `POST /api/accounts` - Create a new account
- `GET /api/accounts/{id}` - Get account details with the current balance
- `DELETE /api/accounts/{id}` - Delete an account (only if it has no transactions)
- `GET /api/accounts/{id}/transactions` - Get all transactions for a specific account

### Transactions
- `GET /api/transactions` - List transactions. Supports optional query parameters: `?from=YYYY-MM-DD`, `?to=YYYY-MM-DD`, `?category=CATEGORY_NAME`
- `POST /api/transactions` - Add a transaction (automatically updates the associated account balance)
- `DELETE /api/transactions/{id}` - Delete a transaction (automatically reverts the associated account balance)
- `GET /api/accounts/{id}/transactions/export` - Export account transactions to a CSV file.

### Summary
- `GET /api/summary` - Retrieves total incomes, total expenses, and expenses grouped by category.

### Budget Limits
- `GET /api/budget-limits` - Get all budget limits.
- `POST /api/budget-limits` - Set a budget limit for a category.
- `DELETE /api/budget-limits/{category}` - Delete a budget limit for a category.

## Technologies

- **Backend:** Java 21, Spring Boot
- **Frontend:** Angular
- **Database:** PostgreSQL
- **Containerization:** Docker & Docker Compose

## Getting Started

### Prerequisites

- Docker and Docker Compose

### Running the Application

The project includes a `docker-compose.yml` file to start the entire stack (Database, Backend API, and Frontend GUI) with a single command.

1. Clone the repository and navigate to the project root directory.
2. Run the following command to build and start all services:

```bash
docker-compose up --build
```

The services will be available at:
- **Frontend GUI:** `http://localhost:80`
- **Backend API:** `http://localhost:8080`
- **PostgreSQL Database:** `localhost:5432`

To stop the application, press `Ctrl+C` in the terminal where it's running, or execute:
```bash
docker-compose down
```

## API Documentation (Swagger / OpenAPI)

The backend includes Swagger for API documentation. Once the application is running via Docker, the UI can be accessed at:
- `http://localhost:8080/api/swagger-ui.html`

## Testing

The project includes tests for core business logic.

### Backend Tests
Navigate to the project root and run:
```bash
./mvnw test
```

## AI Usage

Tools like GitHub Copilot, ChatGPT, or Claude might have been used during the development of this application. Please refer to `AI_NOTES.md` for details regarding how AI was utilized.