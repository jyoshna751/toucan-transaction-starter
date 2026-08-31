
## *Customer Transactions REST API*
This project is a small Spring Boot REST API for managing customer transactions.

It was developed as part of the Toucan Payments Engineering Challenge.

The application supports four operations:
1. Create a transaction
2. Get a transaction by Transaction ID
3. Update the transaction status
4. Get all transactions for a customer

## *Technology Used*

- Java 17
- Spring Boot 3.5.5
- Spring Web
- Spring Data JPA
- H2 Database
- Maven
- JUnit 5
- MockMvc
- Jakarta Bean Validation

## *Project Structure*
src/main/java/com/example/transactionstarter/
│
├── TransactionStarterApplication.java
│
└── transaction/
    ├── controller/
    │   └── TransactionController.java
    ├── entity/
    │   └── Transaction.java
    ├── enums/
    │   ├── TransactionStatus.java
    │   └── TransactionType.java
    ├── exception/
    │   ├── DuplicateTransactionException.java
    │   ├── TransactionNotFoundException.java
    │   └── GlobalExceptionHandler.java
    ├── repository/
    │   └── TransactionRepository.java
    └── service/
        └── TransactionService.java

Tests are located under:src/test/java/com/example/transactionstarter/


## *Transaction Fields*
Each transaction contains:
transactionId - unique ID of the transaction
customerId - ID of the customer
amount - transaction amount
currency - currency used
transactionType - type of transaction
status - current transaction status

Example:

{
  "transactionId": "TXN100",
  "customerId": "CUST100",
  "amount": 5000,
  "currency": "INR",
  "transactionType": "PAYMENT",
  "status": "PENDING"
}
transactionType and status are represented using Java enums.
A newly created transaction always starts with PENDING status.



## *API Endpoints*

GET-api/sample--Sample endpoint from the starter project
POST-api/transactions--Create a transaction
GET-api/transactions/{transactionId}--Get a transaction
PATCH-api/transactions/{transactionId}/status--Update transaction status
GET-api/customers/{customerId}/transactions--Get all transactions for a customer


*1. Create Transaction*
Request:
POST /api/transactions
Content-Type: application/json

Request body:

{
  "transactionId": "TXN100",
  "customerId": "CUST100",
  "amount": 5000,
  "currency": "INR",
  "transactionType": "PAYMENT"
}
Successful Response
201 Created
{
  "transactionId": "TXN100",
  "customerId": "CUST100",
  "amount": 5000,
  "currency": "INR",
  "transactionType": "PAYMENT",
  "status": "PENDING"
}

The status is set to PENDING by the application when the transaction is created.

*2. Get Transaction*
Request:
GET /api/transactions/TXN100
Successful Response
200 OK
{
  "transactionId": "TXN100",
  "customerId": "CUST100",
  "amount": 5000.00,
  "currency": "INR",
  "transactionType": "PAYMENT",
  "status": "PENDING"
}

If the transaction does not exist:404 Not Found

*3. Update Transaction Status*
Request:
PATCH /api/transactions/TXN100/status
Content-Type: application/json

Request body:

{
  "status": "COMPLETED"
}
Successful Response
200 OK
{
  "transactionId": "TXN100",
  "customerId": "CUST100",
  "amount": 5000.00,
  "currency": "INR",
  "transactionType": "PAYMENT",
  "status": "COMPLETED"
}

The transaction must exist before its status can be updated.

*4. Get Customer Transactions*
Request:
GET /api/customers/CUST100/transactions
Successful Response
200 OK
[
  {
    "transactionId": "TXN100",
    "customerId": "CUST100",
    "amount": 5000.00,
    "currency": "INR",
    "transactionType": "PAYMENT",
    "status": "COMPLETED"
  },
  {
    "transactionId": "TXN101",
    "customerId": "CUST100",
    "amount": 2000.00,
    "currency": "INR",
    "transactionType": "PAYMENT",
    "status": "PENDING"
  }
]
The endpoint returns all transactions belonging to the given customer.



## *Validation Rules*
1.Transaction ID
    Required
    Cannot be empty
    Must be unique
2.Customer ID
    Required
    Cannot be empty
3.Amount
    Required
    Must be greater than zero
    Must not be greater than 100000

For example:

{
  "amount": 150000
}

is rejected with:400 Bad Request
4.Currency
    Required
    Cannot be empty
5.Transaction Type
    Required
    Must contain a valid TransactionType enum value
6.Initial Status
The client does not need to send the initial status.
When a transaction is created, the application sets:PENDING


## *Business Validations*

Apart from the basic field validation, I added the following business rules.

1. Duplicate Transaction ID
A Transaction ID must be unique.
If the same Transaction ID is already present, the request is rejected with:409 Conflict
2. Transaction Amount Limit
The maximum allowed transaction amount is:100000
An amount above this limit results in:400 Bad Request
3. Transaction Must Exist
A transaction must exist before it can be retrieved or updated.
If it is not found:404 Not Found


## *HTTP Status Codes*

200 OK--->Request completed successfully
201 Created--->	Transaction created successfully
400 Bad Request--->	Invalid input or validation failure
404 Not Found--->	Transaction does not exist
409 Conflict--->	Transaction ID already exists
500 Internal Server Error--->	Unexpected server error

Business exceptions are handled using a global exception handler.


## *Design Approach*

I kept the implementation simple and separated the main responsibilities into different layers.

Controller
    |
    v
Service
    |
    v
Repository
    |
    v
H2 Database


*Controller:*Handles HTTP requests and returns HTTP responses.

*Service:*Contains the main transaction logic and business validations.

*Repository:*Uses Spring Data JPA to interact with the database.

*Entity:*Represents a transaction in the database.

*Enums:*Used for transaction type and transaction status.

*Exception Handler:*Handles application exceptions and converts them into appropriate HTTP status codes.

## *Database*

The application uses an H2 in-memory database.

Database URL:*jdbc:h2:mem:transactions*

The H2 console is available at:/h2-console

Since the database is in-memory, the data is cleared when the application is restarted.



## *Running the Application*

The project uses the Maven wrapper, so Maven does not need to be installed separately.

From the directory containing pom.xml, run:.\mvnw.cmd spring-boot:run

The default Spring Boot port is 8080.

During my local testing, port 8080 was already in use, so I also tested the application on port 8082.

To run it on port 8082 in PowerShell:
*.\mvnw.cmd spring-boot:run "-Dspring-boot.run.arguments=--server.port=8082"*
If the port is already in use, another available port can be used.

## *Running Tests*

From the directory containing pom.xml, run:.\mvnw.cmd clean test

The project was successfully tested with the following result:

Tests run: 8
Failures: 0
Errors: 0
Skipped: 0
BUILD SUCCESS


## *Tests*
The automated tests cover the following cases:

*1.Successful cases*
Application context loads
Create a transaction
Get a transaction
Update transaction status
Get transactions for a customer
*2.Error cases*
Reject transaction when amount is greater than 100000
Reject duplicate Transaction ID
Return 404 Not Found when the transaction does not exist

*The controller tests use:*
JUnit 5
Spring Boot Test
MockMvc
H2 database

## *Manual Testing*

I also tested the running application using PowerShell.
Example, the sample endpoint was verified using:

Invoke-WebRequest `
    -Uri "http://localhost:8082/api/sample" `
    -Method GET `
    -UseBasicParsing

The response was:StatusCode : 200
The transaction APIs were also tested during development.


## *AI Usage Disclosure*

AI tools were used during development, as permitted by the challenge.

Understanding the requirements
Reviewing validation and exception-handling approaches
Checking test scenarios
Troubleshooting Maven and Spring Boot issues

I did not treat the generated suggestions as final without checking them.
During development, one test initially expected a duplicate Transaction ID to return 400, while the implementation returned 409. I reviewed the requirement and chose 409 Conflict for the duplicate resource case. The test was then updated to match the chosen API contract.

I also tested the application manually and ran the complete automated test suite to verify the final result.

## *Final test result:*

Tests run: 8
Failures: 0
Errors: 0
Skipped: 0
BUILD SUCCESS


## *Final Verification*

Implements all four required operations
Validates transaction input
Handles duplicate Transaction IDs
Handles missing transactions
Uses service and repository layers
Contains automated tests
Passes all 8 tests
Builds successfully with Maven