package com.taskflow.security;

import com.taskflow.entity.User;
import com.taskflow.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

/**
 * Validates the Authorization: Bearer <jwt> header on every request.
 *
 * Only ACCESS-purpose tokens are accepted for normal API calls. PRE_AUTH
 * tokens (issued after password check but before OTP verification) are
 * deliberately rejected here - they are only read manually inside the OTP
 * verification controller methods, never through this filter, so a stolen
 * pre-auth token cannot be used to call any other endpoint.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                     @NonNull HttpServletResponse response,
                                     @NonNull FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                Claims claims = jwtUtil.parseClaims(token);
                if (jwtUtil.isAccessToken(claims)) {
                    String username = claims.getSubject();
                    Optional<User> userOpt = userRepository.findByUsername(username);
                    if (userOpt.isPresent() && SecurityContextHolder.getContext().getAuthentication() == null) {
                        UserPrincipal principal = new UserPrincipal(userOpt.get());
                        var auth = new UsernamePasswordAuthenticationToken(
                                principal, null, principal.getAuthorities());
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    }
                }
                // PRE_AUTH tokens: silently ignored here, leaving the request
                // unauthenticated for any endpoint other than OTP verification,
                // which reads/validates the raw token itself.
            } catch (JwtException | IllegalArgumentException ex) {
                // Invalid/expired token -> proceed unauthenticated; entry point handles 401.
            }
        }

        filterChain.doFilter(request, response);
    }
}
