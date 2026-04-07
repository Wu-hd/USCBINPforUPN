package com.uscbinp.domain.service.auth;

import com.uscbinp.common.exception.BusinessException;
import com.uscbinp.domain.service.auth.impl.AuthServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthServiceTest {

    private static final String DB_PASSWORD = "db-pass";
    private static final String DB_PASSWORD_HASH = new BCryptPasswordEncoder().encode(DB_PASSWORD);

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

        AuthService.AuthLoginResult result = authService.login("dbuser", DB_PASSWORD);

        assertEquals(10L, result.userId());
        assertEquals("dbuser", result.username());
    }

    @Test
    void fallbackUsersShouldStoreHashedPasswords() throws Exception {
        Field fallbackUsersField = AuthServiceImpl.class.getDeclaredField("FALLBACK_USERS");
        fallbackUsersField.setAccessible(true);

        @SuppressWarnings("unchecked")
        Map<String, ?> fallbackUsers = (Map<String, ?>) fallbackUsersField.get(null);
        Object adminUser = fallbackUsers.get("admin");
        Method passwordMethod = adminUser.getClass().getDeclaredMethod("passwordHash");
        passwordMethod.setAccessible(true);
        String storedPassword = (String) passwordMethod.invoke(adminUser);

        assertNotEquals("admin123", storedPassword);
        assertTrue(BCrypt.checkpw("admin123", storedPassword));
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
                return Optional.of(new AuthUserSnapshot(10L, "dbuser", DB_PASSWORD_HASH, 1));
            }
            return Optional.empty();
        }

        @Override
        public Optional<AuthUserSnapshot> findUserById(Long userId) {
            if (Long.valueOf(10L).equals(userId)) {
                return Optional.of(new AuthUserSnapshot(10L, "dbuser", DB_PASSWORD_HASH, 1));
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
