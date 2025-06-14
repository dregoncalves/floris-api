package br.com.floris.security;

import br.com.floris.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class JwtService {

    private final JwtEncoder encoder;
    private final JwtDecoder decoder;

    public JwtService(JwtEncoder encoder, JwtDecoder decoder) {
        this.encoder = encoder;
        this.decoder = decoder;
    }

    @Value("${jwt.access.expiration-minutes:15}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh.expiration-days:7}")
    private long refreshTokenExpiration;

    public String generateAccessToken(User user) {
        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(user.getUsername())
                .issuedAt(now)
                .expiresAt(now.plus(accessTokenExpiration, ChronoUnit.MINUTES))
                .claim("type", "access")
                .claim("authorities", List.of("ROLE_" + user.getRole().name()))
                .build();

        return encoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    public String generateRefreshToken(User user) {
        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(user.getUsername())
                .issuedAt(now)
                .expiresAt(now.plus(refreshTokenExpiration, ChronoUnit.DAYS))
                .claim("type", "refresh")
                .claim("authorities", List.of("ROLE_" + user.getRole().name()))
                .build();

        return encoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    public String extractSubject(String token) {
        return decoder.decode(token).getSubject();
    }

    public boolean isRefreshToken(String token) {
        return "refresh".equals(decoder.decode(token).getClaim("type"));
    }

    public boolean isValid(String token) {
        try {
            decoder.decode(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }
}