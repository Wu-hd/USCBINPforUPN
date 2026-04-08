package com.uscbinp.app.controller.auth;

import com.uscbinp.app.controller.auth.dto.LoginRequest;
import com.uscbinp.app.controller.auth.dto.LoginResponse;
import com.uscbinp.common.api.ApiResponse;
import com.uscbinp.common.error.ErrorCode;
import com.uscbinp.domain.service.auth.AuthService;
import com.uscbinp.infra.jwt.JwtTokenProvider;
import com.uscbinp.infra.security.AuthenticatedUser;
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

import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthController(AuthService authService, JwtTokenProvider jwtTokenProvider) {
        this.authService = authService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthService.AuthLoginResult loginResult = authService.login(request.username(), request.password());
        String tokenSubject = loginResult.userId() == null
            ? loginResult.username()
            : loginResult.userId() + ":" + loginResult.username();
        String token = jwtTokenProvider.generateToken(tokenSubject);
        return ApiResponse.ok(new LoginResponse(token));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<String>> me(Authentication authentication) {
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(ErrorCode.AUTH_UNAUTHORIZED.getCode(), ErrorCode.AUTH_UNAUTHORIZED.getMessage()));
        }
        AuthenticatedUser principal = resolvePrincipal(authentication);
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(ErrorCode.AUTH_UNAUTHORIZED.getCode(), ErrorCode.AUTH_UNAUTHORIZED.getMessage()));
        }
        AuthService.AuthCurrentUser currentUser = authService.getCurrentUser(principal.userId(), principal.username());
        return ResponseEntity.ok(ApiResponse.ok(currentUser.username()));
    }

    @GetMapping("/menus")
    public ResponseEntity<ApiResponse<List<String>>> menus(Authentication authentication) {
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(ErrorCode.AUTH_UNAUTHORIZED.getCode(), ErrorCode.AUTH_UNAUTHORIZED.getMessage()));
        }
        AuthenticatedUser principal = resolvePrincipal(authentication);
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(ErrorCode.AUTH_UNAUTHORIZED.getCode(), ErrorCode.AUTH_UNAUTHORIZED.getMessage()));
        }
        return ResponseEntity.ok(ApiResponse.ok(authService.getCurrentMenus(principal.userId(), principal.username())));
    }

    private AuthenticatedUser resolvePrincipal(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof AuthenticatedUser authenticatedUser) {
            return authenticatedUser;
        }
        if (principal instanceof String name && !"anonymousUser".equals(name)) {
            return new AuthenticatedUser(null, name);
        }
        return null;
    }
}
