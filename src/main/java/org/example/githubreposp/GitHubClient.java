package org.example.githubreposp;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import tools.jackson.databind.JsonNode;

@Component
class GitHubClient {

    private final RestClient restClient;

    GitHubClient(@Value("${github.api.base-url}") String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.ACCEPT, "application/vnd.github+json")
                .defaultHeader(HttpHeaders.USER_AGENT, "github-repos-proxy")
                .build();
    }

    JsonNode listUserRepos(String username) {
        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/users/{username}/repos")
                            .build(username))
                    .retrieve()
                    .body(JsonNode.class);
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 404) {
                throw new GitHubUserNotFoundException("GitHub user '" + username + "' not found");
            }
            throw ex;
        }
    }

    JsonNode listBranches(String owner, String repo) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/repos/{owner}/{repo}/branches")
                        .build(owner, repo))
                .retrieve()
                .body(JsonNode.class);
    }
}
