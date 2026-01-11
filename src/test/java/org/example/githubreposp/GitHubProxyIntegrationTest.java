package org.example.githubreposp;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GitHubProxyIntegrationTest {

    private static final WireMockServer WIREMOCK =
            new WireMockServer(WireMockConfiguration.options().dynamicPort());

    static {
        WIREMOCK.start();
    }

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("github.api.base-url", WIREMOCK::baseUrl);
    }

    @LocalServerPort
    int port;

    private final HttpClient http = HttpClient.newHttpClient();

    @BeforeEach
    void reset() {
        WIREMOCK.resetAll();
    }

    @AfterAll
    static void stop() {
        WIREMOCK.stop();
    }

    @Test
    void shouldReturnOnlyNonForkRepositoriesWithBranches() throws Exception {
        WIREMOCK.stubFor(get(urlPathEqualTo("/users/john/repos"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                [
                                  {"name":"repo-nonfork","fork":false,"owner":{"login":"john"}},
                                  {"name":"repo-fork","fork":true,"owner":{"login":"john"}}
                                ]
                                """)));

        WIREMOCK.stubFor(get(urlPathEqualTo("/repos/john/repo-nonfork/branches"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                [
                                  {"name":"main","commit":{"sha":"abc123"}},
                                  {"name":"dev","commit":{"sha":"def456"}}
                                ]
                                """)));

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/users/john/repositories"))
                .GET()
                .build();

        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());

        assertThat(res.statusCode()).isEqualTo(200);

        String body = res.body();

        assertThat(body).contains("\"repositoryName\":\"repo-nonfork\"");
        assertThat(body).contains("\"ownerLogin\":\"john\"");
        assertThat(body).doesNotContain("repo-fork");

        assertThat(body).contains("\"name\":\"main\"");
        assertThat(body).contains("\"lastCommitSha\":\"abc123\"");
        assertThat(body).contains("\"name\":\"dev\"");
        assertThat(body).contains("\"lastCommitSha\":\"def456\"");
    }

    @Test
    void shouldReturn404InRequiredFormatWhenUserDoesNotExist() throws Exception {
        WIREMOCK.stubFor(get(urlPathEqualTo("/users/missing/repos"))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"message\":\"Not Found\"}")));

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/users/missing/repositories"))
                .GET()
                .build();

        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());

        assertThat(res.statusCode()).isEqualTo(404);

        String body = res.body();
        assertThat(body).contains("\"status\":404");
        assertThat(body).contains("\"message\":\"GitHub user 'missing' not found\"");
    }
}
