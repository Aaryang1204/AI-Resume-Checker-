package com.airesume.middleware.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Request body for user login")
public class UserLoginRequest {
    @Schema(description = "Registered email address")
    @NotBlank(message = "Email is required")
    @Email
    private String email;

    @Schema(description = "Registered password")
    @NotBlank(message = "Password is required")
    private String password;
}
