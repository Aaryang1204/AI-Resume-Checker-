package com.airesume.middleware.dto.response;

import com.airesume.middleware.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
@Schema(description = "Response returned after successful registration")
public class UserRegisterResponse {

    @Schema(description = "Generated UUID of the newly created user")
    private UUID userId;

    @Schema(description = "Email of the registered user")
    private String email;

    @Schema(description = "Full name of the registered user")
    private String fullName;

    @Schema(description = "Role assigned — always CANDIDATE on registration")
    private Role role;

    @Schema(description = "JWT token — store this and send as Bearer on every request")
    private String token;
}
