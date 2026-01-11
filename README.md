

# GitHub Repos Proxy

Spring Boot (WebMVC) application that exposes an endpoint returning GitHub user repositories (non-forks) with their branches and last commit SHA.

## Tech stack

* **Java:** 25
* **Framework:** Spring Boot 4.0.1
* **Build Tool:** Gradle (Kotlin DSL)
* **Web Stack:** Spring WebMVC + Spring RestClient
* **Testing:** Integration tests using WireMock

## Requirements / Notes

* No WebFlux (blocking I/O).
* No pagination support (request uses `per_page=100`, no page iteration).
* No security, cache, or resilience patterns implemented.
* Minimal models, single package structure.
* **Architecture:** Controller -> Service -> Client.
* **Testing:** Only integration tests (no mocks).

## Running

### Build & test

To build the application and run integration tests:

```bash
./gradlew clean test

```

### Run application

To start the application locally:

```bash
./gradlew bootRun

```

The application starts on port **8080**.

## API

### List user repositories (non-forks) with branches

Returns a list of repositories for a given user, filtering out forks, and including branch details.

**Request:**
`GET /users/{username}/repositories`

**Example:**

```bash
curl http://localhost:8080/users/octocat/repositories

```

**Response Example (200 OK):**

```json
[
  {
    "repositoryName": "Hello-World",
    "ownerLogin": "octocat",
    "branches": [
      {
        "name": "main",
        "lastCommitSha": "7fd1a60b01f91b314f59955a4e4d4e80d8edf11d"
      }
    ]
  }
]

```

### User not found

If the GitHub user does not exist, the API returns a 404 status.

**Response Example (404 Not Found):**

```json
{
  "status": 404,
  "message": "GitHub user 'non-existent-user' not found"
}

```

## Backing API

This application consumes the GitHub REST API v3:

1. **List User Repositories:**
`GET /users/{username}/repos`
2. **List Repository Branches:**
`GET /repos/{owner}/{repo}/branches`
