package com.enlasate.backend.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GenerarHash {

    public static void main(String[] args) {

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        String password = "Enlasate2026!";

        String hash = encoder.encode(password);

        System.out.println("Contraseña: " + password);
        System.out.println("Hash BCrypt: " + hash);

        System.out.println(
            "Verificación: " + encoder.matches(password, hash)
        );
    }
}