package com.airesume.middleware.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.airesume.middleware.dto.request.UserLoginRequest;
import com.airesume.middleware.dto.request.UserRegisterRequest;
import com.airesume.middleware.dto.response.BaseResponse;
import com.airesume.middleware.dto.response.UserLoginResponse;
import com.airesume.middleware.dto.response.UserRegisterResponse;
import com.airesume.middleware.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Register and login endpoints — no token required")
public class AuthController {

    private final AuthService authService;

    // Accepts email + fullName + password, always creates the account as CANDIDATE.
    // @Valid triggers the field-level constraints defined in UserRegisterRequest;
    // any violation is caught by GlobalExceptionHandler before this method body runs.
    @PostMapping("/register")
    @Operation(summary = "Register a new account", description = "Creates a CANDIDATE account and returns a JWT token")
    public ResponseEntity<BaseResponse<UserRegisterResponse>> register(
            @Valid @RequestBody UserRegisterRequest request) {

        UserRegisterResponse data = authService.register(request);
        return ResponseEntity.ok(BaseResponse.success("Registration successful", data));
    }

    // Accepts email + password, verifies against the DB, returns a fresh JWT with the
    // user's current role — frontend should read the role here to decide which UI to show.
    @PostMapping("/login")
    @Operation(summary = "Login to an existing account", description = "Verifies credentials and returns a JWT token with the user's role")
    public ResponseEntity<BaseResponse<UserLoginResponse>> login(
            @Valid @RequestBody UserLoginRequest request) {

        UserLoginResponse data = authService.login(request);
        return ResponseEntity.ok(BaseResponse.success("Login successful", data));
    }
}
