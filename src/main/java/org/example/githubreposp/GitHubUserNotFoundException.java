package org.example.githubreposp;

class GitHubUserNotFoundException extends RuntimeException {
    GitHubUserNotFoundException(String message) {
        super(message);
    }
}
