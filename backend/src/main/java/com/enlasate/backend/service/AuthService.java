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
    private final EmailService emailService;

    public AuthService(
            AuthenticationManager authenticationManager,
            UsuarioRepository usuarioRepository,
            ClienteRepository clienteRepository,
            RolRepository rolRepository,
            JwtService jwtService,
            PasswordEncoder passwordEncoder,
            EmailService emailService) {

        this.authenticationManager = authenticationManager;
        this.usuarioRepository = usuarioRepository;
        this.clienteRepository = clienteRepository;
        this.rolRepository = rolRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
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
        cliente.setEstado("Pendiente");
        cliente.setCodigoActivacion(
                "ENL-" + String.format("%04d", (int) (Math.random() * 9999))
        );
        cliente.setCodigoExpira(
                java.time.LocalDateTime.now().plusHours(24)
        );

        clienteRepository.save(cliente);

        return generarRespuesta(usuario);
    }

    // -----------------------------------------------------------------
    // Cambio de contraseña del usuario autenticado
    // -----------------------------------------------------------------
    @Transactional
    public void cambiarPassword(
            String email,
            com.enlasate.backend.dto.CambioPasswordRequest request) {

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "No se encontró el usuario"
                        )
                );

        if (!passwordEncoder.matches(request.passwordActual(), usuario.getPassword())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La contraseña actual no es correcta"
            );
        }

        if (passwordEncoder.matches(request.passwordNueva(), usuario.getPassword())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La contraseña nueva debe ser diferente a la actual"
            );
        }

        usuario.setPassword(passwordEncoder.encode(request.passwordNueva()));

        usuarioRepository.save(usuario);
    }

    // -----------------------------------------------------------------
    // Recuperación de contraseña: manda un enlace al correo
    // -----------------------------------------------------------------
    @org.springframework.beans.factory.annotation.Value("${app.frontend.url:http://localhost:5173}")
    private String urlFrontend;

    @Transactional(readOnly = true)
    public void solicitarRecuperacion(String email) {

        usuarioRepository.findByEmail(email.trim().toLowerCase())
                .ifPresent(usuario -> {

                    String token = jwtService.generarTokenRecuperacion(usuario.getEmail());

                    String enlace = urlFrontend + "/restablecer?token=" + token;

                    String html = """
                            <div style="font-family:Arial,sans-serif;max-width:520px;margin:auto">
                              <h2 style="color:#0f2a4a">ENLÁSATE</h2>
                              <p>Hola %s,</p>
                              <p>Recibimos una solicitud para restablecer tu contraseña.
                                 El enlace vence en <b>30 minutos</b>.</p>
                              <p style="margin:26px 0">
                                <a href="%s"
                                   style="background:#0f2a4a;color:#fff;padding:13px 22px;
                                          border-radius:8px;text-decoration:none;font-weight:bold">
                                  Restablecer mi contraseña
                                </a>
                              </p>
                              <p style="color:#6b7688;font-size:13px">
                                Si no fuiste tú, ignora este correo. Tu contraseña no cambiará.
                              </p>
                              <hr style="border:none;border-top:1px solid #e9ecf2">
                              <p style="color:#9aa4b2;font-size:12px">
                                ENLÁSATE · Servicio de internet
                              </p>
                            </div>
                            """.formatted(usuario.getNombre(), enlace);

                    emailService.enviar(
                            usuario.getEmail(),
                            "Restablece tu contraseña de ENLÁSATE",
                            html
                    );
                });

        // Por seguridad no se revela si el correo existe o no
    }

    // -----------------------------------------------------------------
    // Restablecer la contraseña usando el token del correo
    // -----------------------------------------------------------------
    @Transactional
    public void restablecerPassword(String token, String passwordNueva) {

        String email = jwtService.emailDeTokenRecuperacion(token);

        if (email == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El enlace venció o no es válido. Solicita uno nuevo."
            );
        }

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "No se encontró el usuario"
                        )
                );

        usuario.setPassword(passwordEncoder.encode(passwordNueva));

        usuarioRepository.save(usuario);
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