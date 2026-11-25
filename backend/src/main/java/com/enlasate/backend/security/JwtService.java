package com.enlasate.backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateToken(String email) {
        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSigningKey())
                .compact();
    }

    public boolean isTokenValid(String token, String email) {
        final String extractedEmail = extractEmail(token);
        return extractedEmail.equals(email) && !isTokenExpired(token);
    }

    // -----------------------------------------------------------------
    // Token de activación: firmado, con vigencia de 24 horas
    // -----------------------------------------------------------------
    public String generarTokenActivacion(String email) {

        return Jwts.builder()
                .subject(email)
                .claim("tipo", "activacion")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 24L * 60 * 60 * 1000))
                .signWith(getSigningKey())
                .compact();
    }

    // -----------------------------------------------------------------
    // Token de recuperación de contraseña (30 minutos)
    // -----------------------------------------------------------------
    public String generarTokenRecuperacion(String email) {

        return Jwts.builder()
                .subject(email)
                .claim("tipo", "recuperacion")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 30L * 60 * 1000))
                .signWith(getSigningKey())
                .compact();
    }

    public String emailDeTokenRecuperacion(String token) {

        try {
            Claims claims = extractAllClaims(token);

            if (!"recuperacion".equals(claims.get("tipo"))) {
                return null;
            }

            if (claims.getExpiration().before(new Date())) {
                return null;
            }

            return claims.getSubject();

        } catch (Exception e) {
            return null;
        }
    }

    public boolean esTokenActivacionValido(String token, String email) {

        try {
            Claims claims = extractAllClaims(token);

            return email.equalsIgnoreCase(claims.getSubject())
                    && "activacion".equals(claims.get("tipo"))
                    && claims.getExpiration().after(new Date());

        } catch (Exception e) {
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}