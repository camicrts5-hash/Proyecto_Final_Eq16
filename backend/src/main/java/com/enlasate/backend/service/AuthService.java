package com.enlasate.backend.service;

import java.time.LocalDate;

import com.enlasate.backend.dto.AuthResponse;
import com.enlasate.backend.dto.LoginRequest;
import com.enlasate.backend.dto.RegistroRequest;
import com.enlasate.backend.entity.Usuario;
import com.enlasate.backend.entity.Cliente;
import com.enlasate.backend.entity.Rol;
import com.enlasate.backend.repository.UsuarioRepository;
import com.enlasate.backend.repository.ClienteRepository;
import com.enlasate.backend.repository.RolRepository;
import com.enlasate.backend.security.JwtService;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;
    private final RolRepository rolRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
            AuthenticationManager authenticationManager,
            UsuarioRepository usuarioRepository,
            ClienteRepository clienteRepository,
            RolRepository rolRepository,
            JwtService jwtService,
            PasswordEncoder passwordEncoder) {

        this.authenticationManager = authenticationManager;
        this.usuarioRepository = usuarioRepository;
        this.clienteRepository = clienteRepository;
        this.rolRepository = rolRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {

        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.email(),
                request.password()
            )
        );

        Usuario usuario = usuarioRepository.findByEmail(request.email())
            .orElseThrow(() ->
                new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Credenciales incorrectas"
                )
            );

        return generarRespuesta(usuario);
    }

    @Transactional
    public AuthResponse registro(RegistroRequest request) {

        if (usuarioRepository.existsByEmail(request.email())) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "El correo ya está registrado"
            );
        }

        if (usuarioRepository.existsByUsuario(request.usuario())) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "El nombre de usuario ya está registrado"
            );
        }

        Rol rol = rolRepository.findByNombreRol("Cliente")
            .orElseThrow(() ->
                new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "No se encontró el rol Cliente"
                )
            );

        Usuario usuario = new Usuario();

        usuario.setUsuario(request.usuario());
        usuario.setPassword(passwordEncoder.encode(request.password()));
        usuario.setEmail(request.email());
        usuario.setNombre(request.nombre());
        usuario.setApellido(request.apellido());
        usuario.setTelefono(request.telefono());
        usuario.setEstado("Activo");
        usuario.setRol(rol);

        usuario = usuarioRepository.save(usuario);

        Cliente cliente = new Cliente();

        cliente.setUsuario(usuario);
        cliente.setPlan(null);
        cliente.setFechaAlta(LocalDate.now());
        cliente.setEstado("Activo");

        clienteRepository.save(cliente);

        return generarRespuesta(usuario);
    }

    private AuthResponse generarRespuesta(Usuario usuario) {

        String token = jwtService.generateToken(usuario.getEmail());

        return new AuthResponse(
            token,
            "Bearer",
            usuario.getIdUsuario(),
            usuario.getNombre(),
            usuario.getEmail(),
            usuario.getRol().getNombreRol()
        );
    }
}