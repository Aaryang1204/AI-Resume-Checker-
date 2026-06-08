package com.airesume.middleware.config;

import com.airesume.middleware.util.JwtAuthenticationFilter;
import com.airesume.middleware.service.OAuth2SuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/*
 * @Configuration  → tells Spring this class contains bean definitions (methods annotated @Bean).
 * @EnableWebSecurity → activates Spring Security's web support and disables its default
 *                      auto-configuration so we can provide our own SecurityFilterChain below.
 * @RequiredArgsConstructor → Lombok generates a constructor that injects all final fields,
 *                            replacing the need for @Autowired on each dependency.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    /*
     * Injected by Lombok's constructor.
     *
     * JwtAuthenticationFilter → our custom filter that reads the Authorization header,
     *   validates the JWT, and loads the user into the SecurityContext on every request.
     *
     * OAuth2SuccessHandler → called after a successful Google login. It creates or
     *   updates the user in our DB and issues a JWT to the client.
     */
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;

    /*
     * SecurityFilterChain is the central Spring Security bean.
     * It defines WHAT is protected, HOW authentication works, and WHICH filters run.
     * Spring Boot will use this bean instead of its default auto-configured chain.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            /*
             * Disable CSRF (Cross-Site Request Forgery) protection.
             * CSRF tokens are needed for browser-session-based auth (cookies).
             * Our API uses stateless JWT in the Authorization header, so CSRF is not a threat here.
             */
            .csrf(AbstractHttpConfigurer::disable)

            /*
             * Set session policy to STATELESS.
             * Spring will never create an HttpSession to store authentication.
             * Every request must carry its own JWT — there is no server-side session.
             * This is required for a REST API; without it Spring would fall back to sessions.
             */
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            /*
             * Define which endpoints are public (no token needed) and which require auth.
             *
             * permitAll() routes:
             *   /api/auth/**       → login and register endpoints (user isn't authenticated yet)
             *   /oauth2/**         → Spring's endpoint that starts the Google OAuth2 flow
             *   /login/oauth2/**   → Spring's internal callback that receives the code from Google
             *   /swagger-ui/**     → Swagger UI static assets
             *   /v3/api-docs/**    → OpenAPI JSON spec consumed by Swagger UI
             *
             * anyRequest().authenticated() → every other endpoint requires a valid JWT.
             */
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/api/auth/**",
                    "/oauth2/**",
                    "/login/oauth2/**",
                    "/swagger-ui/**",
                    "/v3/api-docs/**"
                ).permitAll()
                .anyRequest().authenticated()
            )

            /*
             * Enable the OAuth2 login flow.
             * Spring auto-registers /oauth2/authorization/{provider} which starts the redirect.
             * successHandler → after Google returns and the user is verified, control passes
             *   to OAuth2SuccessHandler which issues a JWT and redirects the client.
             */
            .oauth2Login(oauth2 -> oauth2
                .successHandler(oAuth2SuccessHandler)
                // TODO: Add failure handler — on OAuth2 error, redirect to frontend login-failed screen
                // e.g. .failureHandler((req, res, ex) -> res.sendRedirect("http://localhost:3000/login?error"))
            )

            /*
             * Insert JwtAuthenticationFilter before Spring's default username/password filter.
             * This ensures JWT validation runs first on every request so that by the time
             * Spring checks authorization rules, the SecurityContext is already populated
             * (if a valid token was present).
             */
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
