package br.com.floris.dto;

import br.com.floris.model.User;

public record UserResponseDTO(
        Long id,
        String name,
        String username,
        String email,
        Integer age,
        String role
) {
    public static UserResponseDTO fromUser(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getName(),
                user.getUsername(),
                user.getEmail(),
                user.getAge(),
                user.getRole().name()
        );
    }
}