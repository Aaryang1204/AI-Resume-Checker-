package com.airesume.middleware.dto.response;

import java.util.UUID;

import com.airesume.middleware.enums.Role;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Response returned after login")
public class UserLoginResponse {
    
    @Schema(description = "UUID of logged user")
    private UUID userId;

    @Schema(description = "Email of logged user")
    private String email;

    @Schema(description = "Full name of the user")
    private String fullName;

    @Schema(description = "Role of the authenticated user — determines which UI they see")
    private Role role;

    @Schema(description = "JWT token — store this and send as Bearer on every request")
    private String token;
}
