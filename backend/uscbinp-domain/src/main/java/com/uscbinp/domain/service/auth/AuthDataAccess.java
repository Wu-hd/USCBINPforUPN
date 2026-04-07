package com.uscbinp.domain.service.auth;

import java.util.List;
import java.util.Optional;

public interface AuthDataAccess {

    Optional<AuthUserSnapshot> findUserByUsername(String username);

    Optional<AuthUserSnapshot> findUserById(Long userId);

    List<String> findMenuCodesByUserId(Long userId);

    record AuthUserSnapshot(Long userId, String username, String passwordHash, Integer accountStatus) {
    }
}
