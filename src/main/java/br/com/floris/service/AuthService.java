package br.com.floris.service;

import br.com.floris.dto.auth.AuthResponse;
import br.com.floris.enums.Role;
import br.com.floris.model.User;
import br.com.floris.repository.UserRepository;
import br.com.floris.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    public AuthResponse login(String login, String password) {
        // Busca por username ou email
        User user = userRepository.findByUsernameOrEmail(login, login)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        // Autenticação
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(user.getUsername(), password));

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        return new AuthResponse(accessToken, refreshToken, "Bearer", 3600);
    }

    public AuthResponse registerAndLogin(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Usuário já cadastrado");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(Role.USER); // sempre USER, vamos alterar pelo banco ou conta admin
        User savedUser = userRepository.save(user);

        // Geração de tokens
        String accessToken = jwtService.generateAccessToken(savedUser);
        String refreshToken = jwtService.generateRefreshToken(savedUser);

        return new AuthResponse(accessToken, refreshToken, "Bearer", 3600);
    }

    public AuthResponse refreshToken(String refreshToken) {
        // Valida se o token é realmente refresh e se está válido
        if (!jwtService.isValid(refreshToken) || !jwtService.isRefreshToken(refreshToken)) {
            throw new RuntimeException("Refresh token inválido ou expirado.");
        }

        String username = jwtService.extractSubject(refreshToken);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        String newAccessToken = jwtService.generateAccessToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(user);

        return new AuthResponse(newAccessToken, newRefreshToken, "Bearer", 3600);
    }
}