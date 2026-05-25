package com.vantryx.api.service;

import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private MimeMessage mimeMessage; // Mockeamos el mensaje de correo

    @InjectMocks
    private EmailService emailService;

    @Test
    @DisplayName("Debería enviar un email con adjunto correctamente")
    void shouldSendEmailWithAttachmentSuccessfully() {
        // 1. Preparar: Configuramos el mailSender para que devuelva nuestro mimeMessage mockeado
        String to = "admin@vantryx.com";
        String subject = "Reporte de Inventario";
        String body = "Hola, adjunto el reporte.";
        byte[] attachment = "contenido dummy".getBytes();
        String fileName = "reporte.xlsx";

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // 2. Ejecutar
        emailService.sendEmailWithAttachment(to, subject, body, attachment, fileName);

        // 3. Verificar: Comprobar que el método send fue invocado
        verify(mailSender, times(1)).send(any(MimeMessage.class));
        verify(mailSender, times(1)).createMimeMessage();
    }

    @Test
    @DisplayName("Debería lanzar RuntimeException si falla el envío")
    void shouldThrowExceptionWhenMailFails() {
        // Simulamos que al intentar crear el mensaje, algo falla (o al enviar)
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // Configuramos el mock para que lance una excepción al ejecutar send
        doThrow(new RuntimeException("Conexión perdida")).when(mailSender).send(any(MimeMessage.class));

        byte[] attachment = "test".getBytes();

        assertThrows(RuntimeException.class, () -> {
            emailService.sendEmailWithAttachment("test@test.com", "Sub", "Body", attachment, "file.txt");
        });
    }
}