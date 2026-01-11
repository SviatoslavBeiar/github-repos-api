package org.example.githubreposp;

import java.util.List;

public record RepositoryInfo(
        String repositoryName,
        String ownerLogin,
        List<BranchInfo> branches
) {}
