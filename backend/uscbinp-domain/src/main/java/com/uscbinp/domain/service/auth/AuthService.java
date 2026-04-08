package com.uscbinp.domain.service.auth;

import java.util.List;

public interface AuthService {

    AuthLoginResult login(String username, String password);

    AuthCurrentUser getCurrentUser(Long userId, String username);

    List<String> getCurrentMenus(Long userId, String username);

    record AuthLoginResult(Long userId, String username) {
    }

    record AuthCurrentUser(Long userId, String username) {
    }
}
