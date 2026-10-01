# Reward Points REST API - Spring Boot Application

## 1. Requirement
A retailer awards:
- 0 points for each dollar up to and including $50.
- 1 point for every dollar between $50 and $100.
- 2 points for every dollar above $100.

Example: $120 = (50 x 1) + (20 x 2) = 90 points.

The API calculates monthly reward points and the total for each customer for any requested date range. Months are generated from the request date range; they are not hard-coded in business logic.

## 2. Technology
- Java 17
- Spring Boot 3.4.5
- Spring Web
- Spring Data JPA
- H2 in-memory database
- Maven
- JUnit 5 + MockMvc

## 3. Project structure
```text
reward-points-api/
├── pom.xml
├── README.md
└── src/
    ├── main/
    │   ├── java/com/company/rewards/
    │   │   ├── RewardPointsApiApplication.java
    │   │   ├── config/DataInitializer.java
    │   │   ├── controller/RewardController.java
    │   │   ├── controller/GlobalExceptionHandler.java
    │   │   ├── dto/
    │   │   ├── entity/
    │   │   ├── exception/
    │   │   ├── repository/
    │   │   └── service/RewardService.java
    │   └── resources/application.properties
    └── test/
        ├── java/com/company/rewards/servicetest/RewardServiceTest.java
        ├── java/com/company/rewards/controllertest/RewardControllerIntegrationTest.java
        └── resources/application.properties
```

## 4. Run
From the project root:
```bash
mvn clean test
mvn spring-boot:run
```

Or:
```bash
mvn clean package
java -jar target/reward-points-api-1.0.0.jar
```

## 5. API
```http
GET http://localhost:8080/api/rewards?from=2026-07-01&to=2026-09-30
```

Customer-specific:
```http
GET http://localhost:8080/api/rewards/1?from=2026-07-01&to=2026-09-30
```

## 6. Expected sample response
```json
{
  "from": "2026-07-01",
  "to": "2026-09-30",
  "customers": [
    {
      "customerId": 1,
      "customerName": "Alice",
      "monthlyRewards": [
        {"month": "2026-07", "points": 90},
        {"month": "2026-08", "points": 25},
        {"month": "2026-09", "points": 0}
      ],
      "totalPoints": 115
    },
    {
      "customerId": 2,
      "customerName": "Bob",
      "monthlyRewards": [
        {"month": "2026-07", "points": 250},
        {"month": "2026-08", "points": 0},
        {"month": "2026-09", "points": 150}
      ],
      "totalPoints": 400
    },
    {
      "customerId": 3,
      "customerName": "Charlie",
      "monthlyRewards": [
        {"month": "2026-07", "points": 70},
        {"month": "2026-08", "points": 30},
        {"month": "2026-09", "points": 50}
      ],
      "totalPoints": 150
    }
  ]
}
```

## 7. Negative scenarios covered
- Missing date parameters -> Spring validation/binding error.
- `from` later than `to` -> HTTP 400.
- Unknown customer -> HTTP 404.
- Negative/null transaction amount -> IllegalArgumentException.
- Boundary values $50 and $100 are tested.
- Multiple customers and multiple transactions/months are tested.

## 8. Git commands
```bash
git init
git add .
git commit -m "Implement customer reward points REST API"
git branch -M main
git remote add origin <YOUR_PERSONAL_GITHUB_REPOSITORY_URL>
git push -u origin main
```

Do not commit passwords, access tokens, or other secrets.
