package com.airesume.middleware.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Standard envelope for every API response")
public class BaseResponse<T> {

    @Schema(description = "HTTP status code — e.g. 200, 404, 500")
    private int status;

    @Schema(description = "Human-readable message describing the result")
    private String message;

    @Schema(description = "Response payload — null on error responses")
    private T data;

    // --- static factory helpers so callers never build the object manually ---

    public static <T> BaseResponse<T> success(String message, T data) {
        return BaseResponse.<T>builder()
                .status(200)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> BaseResponse<T> error(int status, String message) {
        return BaseResponse.<T>builder()
                .status(status)
                .message(message)
                .data(null)
                .build();
    }
}
