package conexion;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;

public class EnviarCorreo {

    public static void enviar(String destinatario,
                              String asunto,
                              String contenido)
            throws MessagingException {

        String correo = System.getenv("PELUQUERIA_EMAIL");
        String clave = System.getenv("PELUQUERIA_EMAIL_PASSWORD");

        if (correo == null || correo.isBlank()
                || clave == null || clave.isBlank()) {
            throw new IllegalStateException(
                "Faltan las variables de Gmail."
            );
        }

        Properties propiedades = new Properties();

        propiedades.put("mail.smtp.auth", "true");
        propiedades.put("mail.smtp.starttls.enable", "true");
        propiedades.put("mail.smtp.starttls.required", "true");
        propiedades.put("mail.smtp.host", "smtp.gmail.com");
        propiedades.put("mail.smtp.port", "587");

        Session sesion = Session.getInstance(
            propiedades,
            new Authenticator() {
                @Override
                protected PasswordAuthentication
                        getPasswordAuthentication() {

                    return new PasswordAuthentication(correo, clave);
                }
            }
        );

        MimeMessage mensaje = new MimeMessage(sesion);

        mensaje.setFrom(new InternetAddress(correo));
        mensaje.setRecipients(
            Message.RecipientType.TO,
            InternetAddress.parse(destinatario)
        );
        mensaje.setSubject(asunto, "UTF-8");
        mensaje.setText(contenido, "UTF-8");

        Transport.send(mensaje);
    }
}