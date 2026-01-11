package org.example.githubreposp;

public record ErrorResponse(
        int status,
        String message
) {}
