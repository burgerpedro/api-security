package com.ada.escalacaotech.apigateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey key =
            Keys.hmacShaKeyFor(
                    "uma-chave-super-secreta-com-pelo-menos-32-bytes".getBytes());

    public Claims validate(String token){

        return Jwts.parser()
                .verifyWith(key)
                .requireIssuer("api-gateway")
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String generateToken(String username, String role) {
        return Jwts.builder()
                .subject(username)
                .issuer("api-gateway")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600000))
                .claim("roles", role)
                .signWith(key)
                .compact();
    }

}
