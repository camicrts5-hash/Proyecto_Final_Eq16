package com.enlasate.backend.security;

import com.enlasate.backend.entity.Rol;
import com.enlasate.backend.entity.Usuario;
import com.enlasate.backend.repository.ClienteRepository;
import com.enlasate.backend.repository.RolRepository;
import com.enlasate.backend.repository.UsuarioRepository;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Cuando Google confirma la identidad, aquí se busca (o crea) el usuario
 * y se devuelve al frontend con nuestro propio JWT.
 */
@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;
    private final RolRepository rolRepository;
    private final JwtService jwtService;
    private final String frontendUrl;

    public OAuth2SuccessHandler(
            UsuarioRepository usuarioRepository,
            ClienteRepository clienteRepository,
            RolRepository rolRepository,
            JwtService jwtService,
            @Value("${app.frontend.url:http://localhost:5173}") String frontendUrl) {

        this.usuarioRepository = usuarioRepository;
        this.clienteRepository = clienteRepository;
        this.rolRepository = rolRepository;
        this.jwtService = jwtService;
        this.frontendUrl = frontendUrl;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();

        // Ojo: getAttribute es genérico; hay que pasar por Object para que
        // Java no elija String.valueOf(char[]) y reviente en tiempo de ejecución
        Object atributoEmail = oauthUser.getAttribute("email");
        Object atributoNombre = oauthUser.getAttribute("given_name");
        Object atributoApellido = oauthUser.getAttribute("family_name");

        String email = atributoEmail != null ? atributoEmail.toString().toLowerCase() : "";
        String nombre = atributoNombre != null ? atributoNombre.toString() : "";
        String apellido = atributoApellido != null ? atributoApellido.toString() : "";

        Usuario usuario = usuarioRepository.findByEmail(email).orElseGet(() -> {

            Rol rol = rolRepository.findByNombreRol("Cliente")
                    .orElseThrow(() -> new IllegalStateException("No existe el rol Cliente"));

            Usuario nuevo = new Usuario();
            nuevo.setEmail(email);
            nuevo.setNombre(!nombre.isBlank() ? nombre : email);
            nuevo.setApellido(apellido);

            // Nombre de usuario único (puede chocar con cuentas eliminadas)
            String base = email.substring(0, Math.min(30, email.length()));
            String nombreUsuario = base;
            int intento = 1;
            while (usuarioRepository.existsByUsuario(nombreUsuario)) {
                nombreUsuario = base + intento++;
            }

            nuevo.setUsuario(nombreUsuario);
            nuevo.setTelefono("");
            nuevo.setEstado("Activo");
            nuevo.setRol(rol);
            // Contraseña aleatoria: este usuario entra con Google
            nuevo.setPassword("$2a$10$GOOGLE" + java.util.UUID.randomUUID());

            return usuarioRepository.save(nuevo);
        });

        // Si es cliente y todavía no tiene ficha, se le crea (sin plan, pendiente)
        if ("Cliente".equalsIgnoreCase(usuario.getRol().getNombreRol())) {
            boolean tieneFicha = clienteRepository
                    .findByUsuario_IdUsuario(usuario.getIdUsuario())
                    .isPresent();

            if (!tieneFicha) {
                com.enlasate.backend.entity.Cliente cliente = new com.enlasate.backend.entity.Cliente();
                cliente.setUsuario(usuario);
                cliente.setEstado("Pendiente");
                cliente.setFechaAlta(java.time.LocalDate.now());
                cliente.setNombreContacto(
                        (usuario.getNombre() + " " + (usuario.getApellido() != null ? usuario.getApellido() : "")).trim());
                cliente.setTelefonoContacto("");
                cliente.setEmailContacto(email);

                // Código de activación, igual que en el alta manual
                cliente.setCodigoActivacion(
                        "ENL-" + String.format("%04d", (int) (Math.random() * 9999)));
                cliente.setCodigoExpira(java.time.LocalDateTime.now().plusHours(24));

                clienteRepository.save(cliente);
            }
        }

        String token = jwtService.generateToken(usuario.getEmail());

        String destino = frontendUrl + "/oauth2/redirect"
                + "?token=" + URLEncoder.encode(token, StandardCharsets.UTF_8)
                + "&id=" + usuario.getIdUsuario()
                + "&nombre=" + URLEncoder.encode(usuario.getNombre(), StandardCharsets.UTF_8)
                + "&email=" + URLEncoder.encode(usuario.getEmail(), StandardCharsets.UTF_8)
                + "&rol=" + URLEncoder.encode(usuario.getRol().getNombreRol(), StandardCharsets.UTF_8);

        getRedirectStrategy().sendRedirect(request, response, destino);
    }
}
