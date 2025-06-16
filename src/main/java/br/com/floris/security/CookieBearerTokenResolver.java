package br.com.floris.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;

import java.util.Arrays;


public class CookieBearerTokenResolver implements BearerTokenResolver {

    // O nome do cookie que armazena o Access Token
    private static final String ACCESS_TOKEN_COOKIE_NAME = "accessToken";

    @Override
    public String resolve(HttpServletRequest request) {
        // Se não houver cookies na requisição, não há token a ser resolvido
        if (request.getCookies() == null) {
            return null;
        }

        // Procura pelo cookie com o nome esperado
        return Arrays.stream(request.getCookies())
                .filter(cookie -> ACCESS_TOKEN_COOKIE_NAME.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null); // Retorna null se o cookie não for encontrado
    }
}