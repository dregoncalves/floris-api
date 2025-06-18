package br.com.floris.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;

import java.util.Arrays;


public class CookieBearerTokenResolver implements BearerTokenResolver {

    // Nome do cookie do Access Token
    private static final String ACCESS_TOKEN_COOKIE_NAME = "accessToken";

    @Override
    public String resolve(HttpServletRequest request) {
        // Se não tiver cookies, não tem token
        if (request.getCookies() == null) {
            return null;
        }

        // Procura o cookie do Access Token
        return Arrays.stream(request.getCookies())
                .filter(cookie -> ACCESS_TOKEN_COOKIE_NAME.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null); // Retorna null se não achar
    }
}