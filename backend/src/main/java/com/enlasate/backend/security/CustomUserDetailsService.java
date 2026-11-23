package com.enlasate.backend.security;

import com.enlasate.backend.entity.Usuario;
import com.enlasate.backend.repository.UsuarioRepository;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public CustomUserDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "No se encontró un usuario con ese correo"
                        )
                );

        String rol = usuario.getRol().getNombreRol().toUpperCase();

        return User.builder()
                .username(usuario.getEmail())
                .password(usuario.getPassword())
                .authorities("ROLE_" + rol)
                .disabled(!"Activo".equalsIgnoreCase(usuario.getEstado()))
                .build();
    }
}
