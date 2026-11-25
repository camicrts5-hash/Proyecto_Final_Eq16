package com.enlasate.backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${app.mail.enabled:false}")
    private boolean habilitado;

    @Value("${app.mail.from:no-responder@enlasate.online}")
    private String remitente;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    // -----------------------------------------------------------------
    // Envía un correo HTML. Si el envío está deshabilitado (desarrollo),
    // solo escribe el contenido en la consola del servidor.
    // -----------------------------------------------------------------
    public boolean enviar(String destino, String asunto, String html) {

        if (!habilitado) {
            log.warn("=====================================================");
            log.warn("CORREO NO ENVIADO (app.mail.enabled=false)");
            log.warn("Para     : {}", destino);
            log.warn("Asunto   : {}", asunto);
            log.warn("Contenido: {}", html);
            log.warn("=====================================================");
            return false;
        }

        try {
            MimeMessage mensaje = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, true, "UTF-8");

            helper.setFrom(remitente);
            helper.setTo(destino);
            helper.setSubject(asunto);
            helper.setText(html, true);

            mailSender.send(mensaje);

            log.info("Correo enviado a {}", destino);
            return true;

        } catch (Exception e) {
            log.error("No se pudo enviar el correo a {}: {}", destino, e.getMessage());
            return false;
        }
    }
}
