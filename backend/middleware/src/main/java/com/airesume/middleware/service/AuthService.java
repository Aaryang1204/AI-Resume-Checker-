package com.airesume.middleware.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.airesume.middleware.dto.request.UserLoginRequest;
import com.airesume.middleware.dto.request.UserRegisterRequest;
import com.airesume.middleware.dto.response.UserLoginResponse;
import com.airesume.middleware.dto.response.UserRegisterResponse;
import com.airesume.middleware.entity.User;
import com.airesume.middleware.enums.Role;
import com.airesume.middleware.exception.EmailAlreadyExistsException;
import com.airesume.middleware.exception.InvalidCredentialsException;
import com.airesume.middleware.repository.UserRepository;
import com.airesume.middleware.util.JwtUtil;

import lombok.RequiredArgsConstructor;

// @RequiredArgsConstructor generates a constructor with all final fields —
// Spring sees one constructor and auto-wires it without needing @Autowired
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    // Registers a new user: checks for duplicate email, hashes the password,
    // saves the user as CANDIDATE (default), then returns a JWT alongside the profile.
    // Role is never taken from the request — CANDIDATE is the only self-service entry point.
    public UserRegisterResponse register(UserRegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }

        User user = User.builder()
                .email(request.getEmail())
                .fullName(request.getFullName())
                // encode() turns the plain-text password into a BCrypt hash before it ever hits the DB
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.CANDIDATE)
                .provider("local")
                .build();

        // Inserts into DB
        user = userRepository.save(user);

        String token = jwtUtil.generateToken(user, user.getId());

        return UserRegisterResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .token(token)
                .build();
    }

    // Looks up the user by email, verifies the password against the stored hash,
    // then returns a fresh JWT — role is read from the DB, never from the request
    public UserLoginResponse login(UserLoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                // Intentionally vague message — don't reveal whether the email exists
                .orElseThrow(InvalidCredentialsException::new);

        // matches(rawPassword, encodedPassword) — BCrypt re-hashes the input and compares
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        String token = jwtUtil.generateToken(user, user.getId());

        return UserLoginResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .token(token)
                .build();
    }
}
