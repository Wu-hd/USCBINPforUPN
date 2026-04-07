package com.uscbinp.app.controller.auth;

import com.uscbinp.app.controller.auth.dto.LoginRequest;
import com.uscbinp.app.controller.auth.dto.LoginResponse;
import com.uscbinp.common.api.ApiResponse;
import com.uscbinp.common.error.ErrorCode;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok(new LoginResponse("mock-token"));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<String>> me(Authentication authentication) {
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(ErrorCode.AUTH_UNAUTHORIZED.getCode(), ErrorCode.AUTH_UNAUTHORIZED.getMessage()));
        }
        return ResponseEntity.ok(ApiResponse.ok(authentication.getName()));
    }
}
