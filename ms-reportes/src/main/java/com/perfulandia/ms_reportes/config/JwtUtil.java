package com.perfulandia.ms_reportes.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

/**
 * Valida los tokens JWT emitidos por ms-usuarios. Este microservicio no
 * genera tokens (eso es responsabilidad exclusiva de ms-usuarios), solo
 * verifica que las peticiones entrantes traigan un token válido firmado
 * con la misma clave secreta del ecosistema Perfulandia.
 */
@Component
public class JwtUtil {

    private static final String SECRET = "perfulandia-clave-secreta-jwt-2024-segura";

    private SecretKey obtenerClave() {
        return Keys.hmacShaKeyFor(SECRET.getBytes());
    }

    public boolean validarToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(obtenerClave())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String obtenerEmailDesdeToken(String token) {
        return Jwts.parser()
                .verifyWith(obtenerClave())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }
}
