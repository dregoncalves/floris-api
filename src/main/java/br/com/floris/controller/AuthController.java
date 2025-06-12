package br.com.floris.controller;

import br.com.floris.dto.AuthRequest;
import br.com.floris.dto.AuthResponse;
import br.com.floris.model.User;
import br.com.floris.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid AuthRequest request) {
        AuthResponse response = authService.login(request.login(), request.password());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody @Valid User user) {
        AuthResponse response = authService.registerAndLogin(user);
        return ResponseEntity.ok(response);
    }
}
