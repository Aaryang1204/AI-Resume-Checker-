package com.airesume.middleware.service;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.airesume.middleware.dto.response.BaseResponse;
import com.airesume.middleware.entity.User;
import com.airesume.middleware.enums.Role;
import com.airesume.middleware.repository.UserRepository;
import com.airesume.middleware.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        try {
            // Spring gives you OidcUser — Google's profile data
            OidcUser oidcUser = (OidcUser) authentication.getPrincipal();

            String email = oidcUser.getEmail();
            String name  = oidcUser.getFullName();

            // Find existing user OR create a new one — this is YOUR User entity
            User user = userRepository.findByEmail(email)
                .orElseGet(() -> userRepository.save(
                    User.builder()
                        .email(email)
                        .fullName(name)
                        .role(Role.CANDIDATE)
                        .provider("google")   // <-- that field we kept
                        .build()              // no password — null is fine
                ));

            // Now user IS a UserDetails — same generateToken() call as local login
            String token = jwtUtil.generateToken(user, user.getId());

            // Instead of redirect with token in URL
            // Cookie cookie = new Cookie("accessToken", token);
            // cookie.setHttpOnly(true);
            // cookie.setSecure(true);   // HTTPS only in prod
            // cookie.setPath("/");
            // cookie.setMaxAge(900);    // 15 min
            // response.addCookie(cookie);
            // getRedirectStrategy().sendRedirect(request, response,
            //     "http://localhost:3000/dashboard");

            // Redirect frontend with token as query param
            getRedirectStrategy().sendRedirect(request, response,
                "http://localhost:3000/oauth2/success?token=" + token);

        } catch (Exception e) {
            // Write a JSON error body instead of crashing — the redirect never fired,
            // so we own the response at this point
            BaseResponse<Void> errorBody = BaseResponse.error(500, "OAuth2 login failed: " + e.getMessage());

            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            new ObjectMapper().writeValue(response.getWriter(), errorBody);
        }
    }
}
