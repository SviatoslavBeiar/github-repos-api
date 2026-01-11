package org.example.githubreposp;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
class GitHubController {

    private final GitHubService service;

    GitHubController(GitHubService service) {
        this.service = service;
    }

    @GetMapping("/users/{username}/repositories")
    List<RepositoryInfo> listRepositories(@PathVariable String username) {
        return service.listNonForkReposWithBranches(username);
    }
}
