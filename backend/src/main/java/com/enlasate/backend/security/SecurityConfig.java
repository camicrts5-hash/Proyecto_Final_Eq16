package com.enlasate.backend.security;

import java.time.LocalDateTime;
import java.util.List;

import com.enlasate.backend.exception.ApiError;
import tools.jackson.databind.ObjectMapper;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            AuthenticationEntryPoint authenticationEntryPoint,
            AccessDeniedHandler accessDeniedHandler,
            OAuth2SuccessHandler oAuth2SuccessHandler,
            org.springframework.security.oauth2.client.registration.ClientRegistrationRepository clientRegistrationRepository) throws Exception {

        // Fuerza a Google a mostrar el selector de cuentas en cada intento
        org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver resolver =
                new org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver(
                        clientRegistrationRepository, "/oauth2/authorization");

        resolver.setAuthorizationRequestCustomizer(builder ->
                builder.additionalParameters(params -> params.put("prompt", "select_account")));

        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            .csrf(csrf -> csrf.disable())

            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            )

            .oauth2Login(oauth -> oauth
                .authorizationEndpoint(auth -> auth.authorizationRequestResolver(resolver))
                .successHandler(oAuth2SuccessHandler)
                .failureUrl("/login?error=google")
            )

            .exceptionHandling(exception -> exception
                .authenticationEntryPoint(authenticationEntryPoint)
                .accessDeniedHandler(accessDeniedHandler)
            )

          .authorizeHttpRequests(auth -> auth
    .requestMatchers("/api/auth/login", "/api/auth/registro").permitAll()
    .requestMatchers("/api/auth/recuperar", "/api/auth/restablecer").permitAll()
    .requestMatchers("/api/auth/logout").authenticated()
    .requestMatchers("/oauth2/**", "/login/oauth2/**", "/login").permitAll()
    .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/planes", "/api/planes/**").permitAll()
    .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/estados", "/api/estados/**").permitAll()
    .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/municipios", "/api/municipios/**").permitAll()
    .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/colonias", "/api/colonias/**").permitAll()
    .anyRequest().authenticated()
)

            .addFilterBefore(
                jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration) throws Exception {

        return configuration.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(List.of(
            "http://localhost:5173",
            "http://127.0.0.1:5173",
            "https://enlasate.online",
            "https://www.enlasate.online",
            "http://enlasate.online",
            "http://www.enlasate.online"
        ));

        configuration.setAllowedMethods(List.of(
            "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
        ));

        configuration.setAllowedHeaders(List.of(
            "Authorization", "Content-Type"
        ));

        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
            new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    // 401 - No autenticado
    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint(
            ObjectMapper objectMapper) {

        return (request, response, exception) -> {

            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");

            ApiError error = new ApiError(
                LocalDateTime.now(),
                401,
                "Unauthorized",
                "Debes iniciar sesión para acceder a este recurso",
                request.getRequestURI()
            );

            objectMapper.writeValue(response.getOutputStream(), error);
        };
    }

    // 403 - Sin permisos
    @Bean
    public AccessDeniedHandler accessDeniedHandler(
            ObjectMapper objectMapper) {

        return (request, response, exception) -> {

            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");

            ApiError error = new ApiError(
                LocalDateTime.now(),
                403,
                "Forbidden",
                "No tienes permisos para acceder a este recurso",
                request.getRequestURI()
            );

            objectMapper.writeValue(response.getOutputStream(), error);
        };
    }
}