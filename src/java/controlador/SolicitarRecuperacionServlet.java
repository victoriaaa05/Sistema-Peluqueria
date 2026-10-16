package controlador;

import conexion.Conexion;
import conexion.EnviarCorreo;

import jakarta.mail.MessagingException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;

@WebServlet(
    name = "SolicitarRecuperacionServlet",
    urlPatterns = {"/SolicitarRecuperacionServlet"}
)
public class SolicitarRecuperacionServlet extends HttpServlet {

    private static final SecureRandom RANDOM = new SecureRandom();

    private static final String MENSAJE =
        "Si el correo está registrado, recibirás un enlace "
        + "para recuperar tu contraseña.";

    private String generarToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

    private String calcularHash(String token) throws Exception {
        MessageDigest digest =
                MessageDigest.getInstance("SHA-256");

        byte[] hash = digest.digest(
                token.getBytes(StandardCharsets.UTF_8)
        );

        return HexFormat.of().formatHex(hash);
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String correo = request.getParameter("correo");

        if (correo == null || correo.isBlank()) {
            response.sendError(
                HttpServletResponse.SC_BAD_REQUEST,
                "Debes ingresar un correo."
            );
            return;
        }

        correo = correo.trim();

        try (Connection con = Conexion.getConexion()) {

            int idCliente = -1;

            String consulta =
                "SELECT id_cliente FROM clientes WHERE correo = ?";

            try (PreparedStatement ps =
                    con.prepareStatement(consulta)) {

                ps.setString(1, correo);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        idCliente = rs.getInt("id_cliente");
                    }
                }
            }

            if (idCliente != -1) {

                String token = generarToken();
                String tokenHash = calcularHash(token);

                String insertar =
                    "INSERT INTO recuperacion_contrasenas "
                    + "(id_cliente, token_hash, fecha_expiracion) "
                    + "VALUES (?, ?, ?)";

                try (PreparedStatement ps =
                        con.prepareStatement(insertar)) {

                    ps.setInt(1, idCliente);
                    ps.setString(2, tokenHash);
                    ps.setTimestamp(
                        3,
                        Timestamp.valueOf(
                            LocalDateTime.now().plusMinutes(30)
                        )
                    );

                    ps.executeUpdate();
                }

                String enlace =
                    request.getScheme() + "://"
                    + request.getServerName() + ":"
                    + request.getServerPort()
                    + request.getContextPath()
                    + "/restablecer.html?token=" + token;

                String contenido =
                    "Hola,\n\n"
                    + "Recibimos una solicitud para recuperar "
                    + "tu contraseña de Peluquería Elegance.\n\n"
                    + "Abre este enlace:\n"
                    + enlace
                    + "\n\nEl enlace vence en 30 minutos.\n"
                    + "Si no solicitaste este cambio, "
                    + "puedes ignorar este mensaje.";

                EnviarCorreo.enviar(
                    correo,
                    "Recuperar contraseña - Peluquería Elegance",
                    contenido
                );
            }

        } catch (Exception e) {
            getServletContext().log(
                "Error al solicitar recuperación", e
            );

            // No revelamos al usuario si el correo existe.
        }

        response.setContentType("text/html;charset=UTF-8");

try (PrintWriter out = response.getWriter()) {

    out.println("<!DOCTYPE html>");
    out.println("<html lang='es'>");

    out.println("<head>");
    out.println("<meta charset='UTF-8'>");
    out.println("<meta name='viewport' "
            + "content='width=device-width, initial-scale=1.0'>");
    out.println("<title>Recuperación - Peluquería Elegance</title>");
    out.println("<link rel='stylesheet' href='css/estilos.css'>");
    out.println("</head>");

    out.println("<body>");

    out.println("<header>");
    out.println("<div class='logo'>Peluquería Elegance</div>");
    out.println("<nav>");
    out.println("<a href='index.html'>Inicio</a>");
    out.println("<a href='login.html'>Iniciar sesión</a>");
    out.println("</nav>");
    out.println("</header>");

    out.println("<section class='pagina-mensaje'>");
    out.println("<div class='tarjeta-mensaje'>");

    out.println("<h1>Revisa tu correo</h1>");
    out.println("<p>" + MENSAJE + "</p>");

    out.println("<a class='boton-mensaje' href='login.html'>");
    out.println("Volver a iniciar sesión");
    out.println("</a>");

    out.println("</div>");
    out.println("</section>");

    out.println("</body>");
    out.println("</html>");
}
    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.sendRedirect(
            request.getContextPath() + "/recuperar.html"
        );
    }
}