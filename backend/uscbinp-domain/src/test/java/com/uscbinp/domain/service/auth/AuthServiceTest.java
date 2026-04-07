package com.uscbinp.domain.service.auth;

import com.uscbinp.common.exception.BusinessException;
import com.uscbinp.domain.service.auth.impl.AuthServiceImpl;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AuthServiceTest {

    @Test
    void loginShouldSupportFallbackAdminCredentials() {
        AuthService authService = new AuthServiceImpl(null);

        AuthService.AuthLoginResult result = authService.login("admin", "admin123");

        assertEquals(1L, result.userId());
        assertEquals("admin", result.username());
    }

    @Test
    void loginShouldUseProvidedDataAccessWhenAvailable() {
        AuthDataAccess authDataAccess = new StubAuthDataAccess();
        AuthService authService = new AuthServiceImpl(authDataAccess);

        AuthService.AuthLoginResult result = authService.login("dbuser", "db-pass");

        assertEquals(10L, result.userId());
        assertEquals("dbuser", result.username());
    }

    @Test
    void loginShouldRejectInvalidCredentials() {
        AuthService authService = new AuthServiceImpl(null);

        BusinessException ex = assertThrows(BusinessException.class, () -> authService.login("admin", "bad"));

        assertEquals("AUTH_4001", ex.getCode());
    }

    @Test
    void getCurrentMenusShouldFallbackWhenNoDataAccess() {
        AuthService authService = new AuthServiceImpl(null);

        List<String> menus = authService.getCurrentMenus(1L, "admin");

        assertEquals(List.of("system:dashboard", "system:user", "system:role"), menus);
    }

    private static class StubAuthDataAccess implements AuthDataAccess {

        @Override
        public Optional<AuthUserSnapshot> findUserByUsername(String username) {
            if ("dbuser".equals(username)) {
                return Optional.of(new AuthUserSnapshot(10L, "dbuser", "db-pass", 1));
            }
            return Optional.empty();
        }

        @Override
        public Optional<AuthUserSnapshot> findUserById(Long userId) {
            if (Long.valueOf(10L).equals(userId)) {
                return Optional.of(new AuthUserSnapshot(10L, "dbuser", "db-pass", 1));
            }
            return Optional.empty();
        }

        @Override
        public List<String> findMenuCodesByUserId(Long userId) {
            if (Long.valueOf(10L).equals(userId)) {
                return List.of("db:menu");
            }
            return List.of();
        }
    }
}
