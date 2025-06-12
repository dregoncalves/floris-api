package br.com.floris.dto;

import jakarta.validation.constraints.NotBlank;

public record AuthRequest(
        @NotBlank String login,
        @NotBlank String password
) {
}
