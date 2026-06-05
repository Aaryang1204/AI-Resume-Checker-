package com.airesume.middleware.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Request body for user registration")
public class UserRegisterRequest {
    @Schema(description = "Full name of the user", example = "John Doe")
    @NotBlank(message = "Full name is required")
    private String fullName;
 
    @Schema(description = "Email address to register", example = "john@example.com")
    @NotBlank(message = "Email is required")
    @Email(message = "Enter a valid email address")
    private String email;
 
    @Schema(description = "Password — min 6 characters", example = "secret123")
    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;
}
