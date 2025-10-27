# Repo Score Service

**Repo Score Service** is a backend Spring Boot application that fetches GitHub repositories and calculates a **popularity score** based on:

- Stars
- Forks
- Recency of updates

This project demonstrates clean, scalable code, REST API integration, and scoring logic with mock fallback for easy evaluation.

---

## **Features**

- Fetch repositories by **language** and **created date**.
- Calculate **popularity score** using stars, forks, and recency.
- Return **normalized score (0–100)** sorted by popularity.
- **Mock data fallback** if no GitHub token is provided.
- Fully **runnable via Docker** or locally.

---

## **Scoring Algorithm & Configuration**
The `repo-score-service` application assigns a **popularity score** to GitHub repositories based on three factors:

1. **Stars (`stargazers_count`)** – measures the repository’s popularity.
2. **Forks (`forks_count`)** – indicates community engagement and adoption.
3. **Recency of updates (`pushed_at`)** – favors actively maintained repositories.

### **Score Calculation Steps**

1. **Compute individual component scores:**

  - **Stars Score:** logarithmically scaled:
    ```text
    starsScore = log10(1 + stargazers_count)
    ```
  - **Forks Score:** logarithmically scaled:
    ```text
    forksScore = log10(1 + forks_count)
    ```
  - **Recency Score:** inverse function based on days since last push, normalized by a half-life:
    ```text
    recencyScore = 1 / (1 + days_since_push / recencyHalfLifeDays)
    ```

2. **Combine component scores into a weighted raw score:**
```text
   rawScore = (starsWeight * starsScore) +(forksWeight * forksScore) +(recencyWeight * recencyScore)
   ```
3. **Normalize the scores to a 0–100 range:**
```text
normalizedScore = 100 * rawScore / maxRawScore
```
- where `maxRawScore` is the highest raw score among the fetched repositories.

---

### **Configuration via `application.yml`**

- All scoring parameters are configurable in `src/main/resources/application.yml`:

  ```yaml
  scoring:
  stars-weight: 0.6          # Importance of stars in total score
  forks-weight: 0.25         # Importance of forks in total score
  recency-weight: 0.15       # Importance of recency in total score
  recency-half-life-days: 30 # Days it takes for recency contribution to halve
  ```
- These weights allow flexibility to tune the scoring formula without modifying code.
- recency-half-life-days controls how quickly a repository’s recency decays.
- Stars are weighted highest to emphasize popularity, forks contribute moderately, and recency has smaller influence.

### **Design Rationale**

- Stars (0.6) dominate because highly starred repositories are typically more popular.
- Forks (0.25) reflect adoption and engagement.
- Recency (0.15) slightly favors actively maintained repositories.
- This approach ensures a balanced popularity score, prioritizing popular repositories while still considering activity and community engagement.

---

## **Technologies Used**

- Java 17
- Spring Boot 3.5.7
- WebFlux for API calls
- Jackson for JSON processing
- Maven for project management
- Docker for containerization

---

## **Getting Started**

### **Prerequisites**

- Java 17 installed
- Maven installed
- Docker (optional, for containerized run)

---
## **Running Locally (with GitHub token)**

- Generate a GitHub personal access token with repo scope.
- Linux/Mac: 
```text 
export GITHUB_TOKEN=ghp_xxxxxxxxxxxxxxxxxxxxx 
```
- Windows (cmd): 
```text
 set GITHUB_TOKEN=ghp_xxxxxxxxxxxxxxxxxxxxx
 ```
- Run the jar: 
```bash
java -jar target/repo-score-service-0.0.1-SNAPSHOT.jar
````

- ### Configure Environment Variable in IntelliJ

  - If you want to use your GitHub token while running from IntelliJ:
  - Go to Run → Edit Configurations…
  - Click the + → select Spring Boot → choose your main class (RepoScoreServiceApplication)
  - Under Environment variables, add: GITHUB_TOKEN=ghp_your_generated_token_here
  - Click Apply and OK.

## **Running Locally (without GitHub token)**

```bash
# Build the project
mvn clean package

# Run the jar
java -jar target/repo-score-service-0.0.1-SNAPSHOT.jar
```
- The service will use mock data instead of live GitHub repositories if no token is provided.

## ** Run the Application in IntelliJ**
- Right-click on RepoScoreServiceApplication.java → Run 'RepoScoreServiceApplication'
- Or click the green Run button in the top-right corner.
- You will see the Spring Boot logs in IntelliJ’s console:
  - Tomcat started on port(s): 8080 (http)
  - Started RepoScoreServiceApplication in X.XXX seconds
- The app is now running locally on port 8080.
- open a browser and visit:
  ```bash
    http://localhost:8080/api/v1/repos/score?language=java&created_after=2024-01-01&per_page=5
  ```
- You will see the list of repos with fullName, htmlUrl, description and score in descending order.


## **Running with Docker**
```bash
# Build Docker image
docker build -t repo-score-service .

# Run Docker container (with GitHub token)
docker run -e GITHUB_TOKEN=ghp_xxxxxxxxxxxxxxxxxxxxx -p 8080:8080 repo-score-service
```