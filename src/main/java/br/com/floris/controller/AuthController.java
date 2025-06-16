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

    // Constantes para os nomes dos cookies
    private static final String ACCESS_TOKEN_COOKIE_NAME = "accessToken";
    private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";

    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<UserResponseDTO> login(@RequestBody @Valid AuthRequest request, HttpServletResponse httpResponse) {
        // 1. Autentica e gera os tokens
        AuthResponse authResponse = authService.login(request.login(), request.password());

        // 2. Adiciona os cookies de token à resposta
        addTokenCookiesToResponse(authResponse, httpResponse);

        // 3. Busca e retorna os dados públicos do usuário no corpo da resposta
        User user = userService.findByUsernameOrEmail(request.login(), request.login());
        return ResponseEntity.ok(UserResponseDTO.fromUser(user));
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> register(@RequestBody @Valid User user, HttpServletResponse httpResponse) {
        // 1. Registra o usuário e já faz o login, gerando os tokens
        AuthResponse authResponse = authService.registerAndLogin(user);

        // 2. Adiciona os cookies de token à resposta
        addTokenCookiesToResponse(authResponse, httpResponse);

        // 3. Retorna os dados do usuário recém-criado
        return ResponseEntity.ok(UserResponseDTO.fromUser(user));
    }

    @PostMapping("/refresh")
    public ResponseEntity<Void> refresh(@RequestBody @Valid RefreshRequest request, HttpServletResponse httpResponse) {
        AuthResponse authResponse = authService.refreshToken(request.refreshToken());
        addTokenCookiesToResponse(authResponse, httpResponse);
        return ResponseEntity.noContent().build();
    }

    // Logout com invalidação do cookie
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletResponse httpResponse) {
        // Cria cookies "vazios" com tempo de expiração 0 para invalidar os existentes no navegador.
        ResponseCookie accessTokenCookie = ResponseCookie.from(ACCESS_TOKEN_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(true) // obs: não esquecer de usar https em produção
                .path("/")
                .maxAge(0) // expira o token
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


    //Metodo que cria e adiciona os cookies de token na resposta HTTP

    private void addTokenCookiesToResponse(AuthResponse authResponse, HttpServletResponse httpResponse) {
        long accessTokenMaxAge = TimeUnit.MINUTES.toSeconds(15);
        long refreshTokenMaxAge = TimeUnit.DAYS.toSeconds(7);

        ResponseCookie accessTokenCookie = ResponseCookie.from(ACCESS_TOKEN_COOKIE_NAME, authResponse.accessToken())
                .httpOnly(true)
                .secure(false)      // <-- AJUSTE CRÍTICO: 'false' para rodar em HTTP local
                .path("/")
                .maxAge(accessTokenMaxAge)
                .sameSite("Lax")    // <-- AJUSTE RECOMENDADO: "Lax" é mais compatível para dev
                .build();

        ResponseCookie refreshTokenCookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, authResponse.refreshToken())
                .httpOnly(true)
                .secure(false)      // <-- AJUSTE CRÍTICO: 'false' também
                .path("/")
                .maxAge(refreshTokenMaxAge)
                .sameSite("Lax")    // <-- AJUSTE RECOMENDADO: "Lax" também
                .build();

        httpResponse.addHeader(HttpHeaders.SET_COOKIE, accessTokenCookie.toString());
        httpResponse.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());
    }
}