package com.airesume.middleware.util;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.airesume.middleware.entity.User;
import com.airesume.middleware.repository.UserRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/*
 * JwtAuthenticationFilter runs on every incoming HTTP request, exactly once.
 *
 * OncePerRequestFilter → Spring's base class that guarantees the filter executes
 *   only once per request, even if the request is dispatched internally (e.g. forwards).
 *
 * Responsibility: read the JWT from the Authorization header, validate it, and if valid,
 *   load the matching user from DB and place them in the SecurityContext so that
 *   Spring Security knows who is making the request for the rest of the request lifecycle.
 *
 * If no token is present, or the token is invalid/expired, the filter simply passes
 *   the request along unauthenticated. Spring's authorization rules then decide
 *   whether that unauthenticated request is allowed (public route) or rejected (401).
 *
 * @Component → registers this as a Spring bean so it can be injected into SecurityConfig.
 * @RequiredArgsConstructor → Lombok generates a constructor injecting all final fields.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /*
     * JwtUtil → handles all JWT operations: parsing, signature verification, claim extraction.
     * UserRepository → needed to load the full User entity from DB using the email in the token.
     */
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        /*
         * Every authenticated request must include an Authorization header in the format:
         *   Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.<payload>.<signature>
         *
         * If the header is absent or doesn't start with "Bearer ", this is either a
         * public request or a malformed one — pass it through without doing anything.
         * Spring's authorization rules will handle the rest.
         */
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        /*
         * Strip the "Bearer " prefix (7 characters) to get the raw JWT string.
         * Format: "Bearer eyJ..." → token = "eyJ..."
         */
        String token = authHeader.substring(7);

        /*
         * extractEmail() internally calls extractAllClaims(), which:
         *   1. Parses the JWT
         *   2. Verifies the HMAC-SHA256 signature using our secret key
         *   3. Returns the claims payload
         *
         * If the token is tampered with, expired, or malformed, JJWT throws an exception.
         * We catch it and pass the request through unauthenticated — no crash, no leak.
         */
        String email;
        try {
            email = jwtUtil.extractEmail(token);
        } catch (Exception e) {
            filterChain.doFilter(request, response);
            return;
        }

        /*
         * Only proceed if:
         *   - email was successfully extracted from the token
         *   - the SecurityContext doesn't already have an authenticated user
         *     (avoids redundant DB lookups if auth was already set earlier in the chain)
         */
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            /*
             * Load the User entity from the database.
             * We need the full User object (not just the email) because:
             *   1. isTokenValid() needs UserDetails.getUsername() to cross-check the email
             *   2. getAuthorities() on the User gives Spring Security the roles for this request
             */
            User user = userRepository.findByEmail(email).orElse(null);

            /*
             * isTokenValid() performs two checks:
             *   1. Token email matches the DB user's email (prevents token reuse across accounts)
             *   2. Token has not expired
             *
             * Only if both pass do we trust this request as authenticated.
             */
            if (user != null && jwtUtil.isTokenValid(token, user)) {

                /*
                 * UsernamePasswordAuthenticationToken is Spring Security's standard
                 * authentication object. Arguments:
                 *   principal   → the authenticated User (available via SecurityContextHolder later)
                 *   credentials → null (no password needed — JWT already proved identity)
                 *   authorities → the user's roles (e.g. ROLE_CANDIDATE) for authorization checks
                 */
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(user, null,
                        user.getAuthorities());

                /*
                 * Attach request metadata (IP address, session ID) to the auth token.
                 * Used by Spring Security's audit and logging infrastructure.
                 */
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                /*
                 * Place the authenticated user into the SecurityContext.
                 * From this point on, any call to SecurityContextHolder.getContext().getAuthentication()
                 * in a controller or service will return this user.
                 */
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }

        /*
         * Always continue the filter chain regardless of outcome.
         * If authentication was set above, protected routes will pass.
         * If not, Spring's authorization layer will return 401 for protected routes.
         */
        filterChain.doFilter(request, response);

    }

}
