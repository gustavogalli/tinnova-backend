package com.galli.tinnova.config.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    private static final String SECRET = "minha-secret-super-segura-com-mais-de-256-bits!!!";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET, 3600000); // 1 hora
    }

    @Test
    void deveGerarTokenNaoNulo() {
        String token = jwtService.gerarToken("user1", "USER");

        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    void deveExtrairUsernameDoToken() {
        String username = "user1";
        String token = jwtService.gerarToken(username, "USER");

        String extractedUsername = jwtService.getUsername(token);

        assertEquals(username, extractedUsername);
    }

    @Test
    void deveExtrairRoleDoToken() {
        String role = "ADMIN";
        String token = jwtService.gerarToken("admin", role);

        String extractedRole = jwtService.getRole(token);

        assertEquals(role, extractedRole);
    }

    @Test
    void tokenDeveConterDataDeExpiracaoValida() {
        String token = jwtService.gerarToken("user1", "USER");

        Date expiration = Jwts.parserBuilder()
                .setSigningKey(jwtService.key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getExpiration();

        assertNotNull(expiration);
        assertTrue(expiration.after(new Date()));
    }

    @Test
    void deveFalharAoLerTokenComAssinaturaInvalida() {
        JwtService outroJwtService = new JwtService("outra-secret-diferente-e-256-bits!!!", 3600000);

        String token = outroJwtService.gerarToken("user1", "USER");

        assertThrows(
                SignatureException.class,
                () -> jwtService.getUsername(token)
        );
    }

    @Test
    void deveFalharComTokenExpirado() throws InterruptedException {
        JwtService jwtCurto = new JwtService(SECRET, 1);

        String token = jwtCurto.gerarToken("user1", "USER");

        Thread.sleep(5);

        assertThrows(
                ExpiredJwtException.class,
                () -> jwtCurto.getUsername(token)
        );
    }
}
