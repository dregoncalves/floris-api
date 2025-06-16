package br.com.floris.controller;

import br.com.floris.dto.UserResponseDTO;
import br.com.floris.model.User;
import br.com.floris.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    // GET /users - Lista todos os usuários (agora usando DTO)
    @GetMapping
    public List<UserResponseDTO> getAllUsers() {
        return service.findAll().stream()
                .map(UserResponseDTO::fromUser)
                .toList();
    }

    // GET /users/{id} - Busca usuário por ID (DTO)
    @GetMapping("/{id}")
    public UserResponseDTO getUserById(@PathVariable Long id) {
        return UserResponseDTO.fromUser(service.findById(id));
    }

    // POST /users - Cria usuário (DTO na resposta)
    @PostMapping
    public UserResponseDTO createUser(@RequestBody User user) {
        return UserResponseDTO.fromUser(service.create(user));
    }

    // PUT /users/{id} - Atualiza usuário (DTO na resposta)
    @PutMapping("/{id}")
    public UserResponseDTO updateUser(@PathVariable Long id, @RequestBody User user) {
        return UserResponseDTO.fromUser(service.update(id, user));
    }

    // DELETE /users/{id} - Remove usuário
    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        service.delete(id);
    }

    // GET /users/me - Retorna usuário autenticado pelo token
    @GetMapping("/me")
    public UserResponseDTO getCurrentUser(Authentication authentication) {
        String username = authentication.getName();
        User user = service.findByUsername(username);
        return UserResponseDTO.fromUser(user);
    }

    @PutMapping("/me/password")
    public ResponseEntity<Void> changePassword(@RequestBody Map<String, String> passwordRequest, Authentication authentication) {
        String senhaAntiga = passwordRequest.get("senhaAntiga");
        String novaSenha = passwordRequest.get("novaSenha");

        if (senhaAntiga == null || novaSenha == null) {
            return ResponseEntity.badRequest().build();
        }

        service.changePassword(authentication.getName(), senhaAntiga, novaSenha);
        return ResponseEntity.noContent().build();
    }
}
