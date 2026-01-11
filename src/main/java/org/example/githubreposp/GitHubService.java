package org.example.githubreposp;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;

@Service
class GitHubService {

    private final GitHubClient client;

    GitHubService(GitHubClient client) {
        this.client = client;
    }

    List<RepositoryInfo> listNonForkReposWithBranches(String username) {
        JsonNode repos = client.listUserRepos(username);
        List<RepositoryInfo> result = new ArrayList<>();

        if (repos == null || !repos.isArray()) {
            return result;
        }

        for (JsonNode repoNode : repos) {
            boolean fork = repoNode.path("fork").asBoolean(false);
            if (fork) continue;

            String repoName = repoNode.path("name").asText();
            String ownerLogin = repoNode.path("owner").path("login").asText();

            List<BranchInfo> branches = mapBranches(ownerLogin, repoName);
            result.add(new RepositoryInfo(repoName, ownerLogin, branches));
        }

        return result;
    }

    private List<BranchInfo> mapBranches(String owner, String repo) {
        JsonNode branchesNode = client.listBranches(owner, repo);
        List<BranchInfo> branches = new ArrayList<>();

        if (branchesNode == null || !branchesNode.isArray()) {
            return branches;
        }

        for (JsonNode b : branchesNode) {
            String name = b.path("name").asText();
            String sha = b.path("commit").path("sha").asText();
            branches.add(new BranchInfo(name, sha));
        }

        return branches;
    }
}
