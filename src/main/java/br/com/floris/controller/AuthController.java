package br.com.floris.controller;

import br.com.floris.dto.UserResponseDTO;
import br.com.floris.dto.auth.AuthRequest;
import br.com.floris.dto.auth.AuthResponse;
import br.com.floris.model.User;
import br.com.floris.service.AuthService;
import br.com.floris.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import br.com.floris.dto.RefreshRequest;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    // Nomes dos cookies de token
    private static final String ACCESS_TOKEN_COOKIE_NAME = "accessToken";
    private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";

    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<UserResponseDTO> login(@RequestBody @Valid AuthRequest request, HttpServletResponse httpResponse) {
        // Autentica e pega os tokens
        AuthResponse authResponse = authService.login(request.login(), request.password());

        // Adiciona os tokens nos cookies da resposta
        addTokenCookiesToResponse(authResponse, httpResponse);

        // Retorna os dados do usuário
        User user = userService.findByUsernameOrEmail(request.login(), request.login());
        return ResponseEntity.ok(UserResponseDTO.fromUser(user));
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> register(@RequestBody @Valid User user, HttpServletResponse httpResponse) {
        // Registra o usuário e já gera os tokens
        AuthResponse authResponse = authService.registerAndLogin(user);

        // Adiciona os tokens nos cookies
        addTokenCookiesToResponse(authResponse, httpResponse);

        // Retorna o usuário criado
        return ResponseEntity.ok(UserResponseDTO.fromUser(user));
    }

    @PostMapping("/refresh")
    public ResponseEntity<Void> refresh(@RequestBody @Valid RefreshRequest request, HttpServletResponse httpResponse) {
        AuthResponse authResponse = authService.refreshToken(request.refreshToken());
        addTokenCookiesToResponse(authResponse, httpResponse);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletResponse httpResponse) {
        // Invalida os cookies de token
        ResponseCookie accessTokenCookie = ResponseCookie.from(ACCESS_TOKEN_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(true) // Usar HTTPS em produção
                .path("/")
                .maxAge(0) // Expira o token
                .sameSite("Strict")
                .build();

        ResponseCookie refreshTokenCookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .sameSite("Strict")
                .build();

        httpResponse.addHeader(HttpHeaders.SET_COOKIE, accessTokenCookie.toString());
        httpResponse.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());

        return ResponseEntity.ok("Logout realizado com sucesso.");
    }

    // Cria e adiciona os cookies com os tokens na resposta
    private void addTokenCookiesToResponse(AuthResponse authResponse, HttpServletResponse httpResponse) {
        long accessTokenMaxAge = TimeUnit.MINUTES.toSeconds(15);
        long refreshTokenMaxAge = TimeUnit.DAYS.toSeconds(7);

        ResponseCookie accessTokenCookie = ResponseCookie.from(ACCESS_TOKEN_COOKIE_NAME, authResponse.accessToken())
                .httpOnly(true)
                .secure(false)      // Usar 'true' pra HTTPS em produção
                .path("/")
                .maxAge(accessTokenMaxAge)
                .sameSite("Lax")    // 'Lax' é mais compatível pra dev
                .build();

        ResponseCookie refreshTokenCookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, authResponse.refreshToken())
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(refreshTokenMaxAge)
                .sameSite("Lax")
                .build();

        httpResponse.addHeader(HttpHeaders.SET_COOKIE, accessTokenCookie.toString());
        httpResponse.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());
    }
}