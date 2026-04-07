package com.uscbinp.infra.security;

import com.uscbinp.infra.jwt.JwtTokenProvider;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String TOKEN_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(authorization)
            && authorization.startsWith(TOKEN_PREFIX)
            && SecurityContextHolder.getContext().getAuthentication() == null) {
            String token = authorization.substring(TOKEN_PREFIX.length()).trim();
            if (StringUtils.hasText(token)) {
                try {
                    String subject = jwtTokenProvider.parseSubject(token);
                    AuthenticatedUser authenticatedUser = resolveAuthenticatedUser(subject);
                    UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                        authenticatedUser,
                        null,
                        Collections.emptyList()
                    );
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                } catch (JwtException | IllegalArgumentException ex) {
                    SecurityContextHolder.clearContext();
                }
            }
        }
        filterChain.doFilter(request, response);
    }

    private AuthenticatedUser resolveAuthenticatedUser(String subject) {
        if (!StringUtils.hasText(subject)) {
            return new AuthenticatedUser(null, "unknown");
        }
        String[] parts = subject.split(":", 2);
        if (parts.length == 2) {
            return new AuthenticatedUser(parseLong(parts[0]), parts[1]);
        }
        return new AuthenticatedUser(null, subject);
    }

    private Long parseLong(String value) {
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
