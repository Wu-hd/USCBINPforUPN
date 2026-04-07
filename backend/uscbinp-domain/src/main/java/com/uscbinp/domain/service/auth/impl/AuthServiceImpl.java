package com.uscbinp.domain.service.auth.impl;

import com.uscbinp.common.error.ErrorCode;
import com.uscbinp.common.exception.BusinessException;
import com.uscbinp.domain.service.auth.AuthDataAccess;
import com.uscbinp.domain.service.auth.AuthService;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AuthServiceImpl implements AuthService {

    private static final String DEFAULT_UNKNOWN_USERNAME = "unknown";

    private static final Map<String, FallbackUser> FALLBACK_USERS = Map.of(
        "admin", new FallbackUser(1L, "admin123", List.of("system:dashboard", "system:user", "system:role")),
        "demo", new FallbackUser(2L, "demo123", List.of("system:dashboard"))
    );

    private final AuthDataAccess authDataAccess;

    public AuthServiceImpl(@Nullable AuthDataAccess authDataAccess) {
        this.authDataAccess = authDataAccess;
    }

    @Override
    public AuthLoginResult login(String username, String password) {
        Optional<AuthDataAccess.AuthUserSnapshot> dbUserOpt = findUserByUsername(username);
        if (dbUserOpt.isPresent()) {
            AuthDataAccess.AuthUserSnapshot dbUser = dbUserOpt.get();
            if (!passwordMatches(password, dbUser.passwordHash()) || !isAccountEnabled(dbUser.accountStatus())) {
                throw loginFailed();
            }
            return new AuthLoginResult(dbUser.userId(), dbUser.username());
        }

        FallbackUser fallbackUser = FALLBACK_USERS.get(username);
        if (fallbackUser != null && fallbackUser.password().equals(password)) {
            return new AuthLoginResult(fallbackUser.userId(), username);
        }
        throw loginFailed();
    }

    @Override
    public AuthCurrentUser getCurrentUser(Long userId, String username) {
        Optional<AuthDataAccess.AuthUserSnapshot> dbUserOpt = findUserByPrincipal(userId, username);
        if (dbUserOpt.isPresent()) {
            AuthDataAccess.AuthUserSnapshot dbUser = dbUserOpt.get();
            return new AuthCurrentUser(dbUser.userId(), dbUser.username());
        }

        Optional<FallbackResolvedUser> fallbackResolvedUser = resolveFallbackUser(userId, username);
        if (fallbackResolvedUser.isPresent()) {
            FallbackResolvedUser resolved = fallbackResolvedUser.get();
            return new AuthCurrentUser(resolved.userId(), resolved.username());
        }

        if (StringUtils.hasText(username)) {
            return new AuthCurrentUser(userId, username);
        }
        return new AuthCurrentUser(userId, DEFAULT_UNKNOWN_USERNAME);
    }

    @Override
    public List<String> getCurrentMenus(Long userId, String username) {
        if (authDataAccess != null && userId != null) {
            List<String> dbMenus = authDataAccess.findMenuCodesByUserId(userId);
            if (!dbMenus.isEmpty()) {
                return dbMenus;
            }
        }

        Optional<FallbackResolvedUser> fallbackResolvedUser = resolveFallbackUser(userId, username);
        return fallbackResolvedUser
            .map(resolved -> resolved.user().menus())
            .orElse(List.of());
    }

    private Optional<AuthDataAccess.AuthUserSnapshot> findUserByUsername(String username) {
        if (authDataAccess == null || !StringUtils.hasText(username)) {
            return Optional.empty();
        }
        return authDataAccess.findUserByUsername(username);
    }

    private Optional<AuthDataAccess.AuthUserSnapshot> findUserByPrincipal(Long userId, String username) {
        if (authDataAccess == null) {
            return Optional.empty();
        }
        if (userId != null) {
            Optional<AuthDataAccess.AuthUserSnapshot> dbById = authDataAccess.findUserById(userId);
            if (dbById.isPresent()) {
                return dbById;
            }
        }
        if (StringUtils.hasText(username)) {
            return authDataAccess.findUserByUsername(username);
        }
        return Optional.empty();
    }

    private Optional<FallbackResolvedUser> resolveFallbackUser(Long userId, String username) {
        if (StringUtils.hasText(username) && FALLBACK_USERS.containsKey(username)) {
            return Optional.of(new FallbackResolvedUser(username, FALLBACK_USERS.get(username)));
        }
        if (userId != null) {
            return FALLBACK_USERS.entrySet().stream()
                .filter(entry -> entry.getValue().userId().equals(userId))
                .findFirst()
                .map(entry -> new FallbackResolvedUser(entry.getKey(), entry.getValue()));
        }
        return Optional.empty();
    }

    private boolean passwordMatches(String rawPassword, String persistedPasswordHash) {
        return StringUtils.hasText(persistedPasswordHash) && persistedPasswordHash.equals(rawPassword);
    }

    private boolean isAccountEnabled(Integer accountStatus) {
        return accountStatus == null || accountStatus == 1;
    }

    private BusinessException loginFailed() {
        return new BusinessException(ErrorCode.AUTH_LOGIN_FAILED.getCode(), ErrorCode.AUTH_LOGIN_FAILED.getMessage());
    }

    private record FallbackUser(Long userId, String password, List<String> menus) {
    }

    private record FallbackResolvedUser(String username, FallbackUser user) {
        private Long userId() {
            return user.userId();
        }
    }
}
