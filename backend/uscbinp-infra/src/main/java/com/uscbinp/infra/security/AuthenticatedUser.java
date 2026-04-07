package com.uscbinp.infra.security;

public record AuthenticatedUser(Long userId, String username) {
}
