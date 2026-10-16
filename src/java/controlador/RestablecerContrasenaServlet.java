package controlador;

import conexion.Conexion;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HexFormat;

import org.mindrot.jbcrypt.BCrypt;

@WebServlet(
    name = "RestablecerContrasenaServlet",
    urlPatterns = {"/RestablecerContrasenaServlet"}
)
public class RestablecerContrasenaServlet extends HttpServlet {

    private String calcularHash(String token) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");

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

        String token = request.getParameter("token");
        String contrasena = request.getParameter("contrasena");
        String confirmar = request.getParameter("confirmar");

        if (token == null || token.isBlank()
                || contrasena == null
                || confirmar == null
                || contrasena.length() < 5
                || !contrasena.equals(confirmar)) {

            mostrarMensaje(
                response,
                "Las contraseñas no coinciden o son demasiado cortas."
            );
            return;
        }

        try (Connection con = Conexion.getConexion()) {

            con.setAutoCommit(false);

            try {
                String tokenHash = calcularHash(token);

                String consulta =
                    "SELECT id_recuperacion, id_cliente, "
                    + "fecha_expiracion, utilizado "
                    + "FROM recuperacion_contrasenas "
                    + "WHERE token_hash = ? "
                    + "FOR UPDATE";

                int idRecuperacion;
                int idCliente;
                Timestamp expiracion;
                boolean utilizado;

                try (PreparedStatement ps =
                        con.prepareStatement(consulta)) {

                    ps.setString(1, tokenHash);

                    try (ResultSet rs = ps.executeQuery()) {

                        if (!rs.next()) {
                            con.rollback();
                            mostrarMensaje(
                                response,
                                "El enlace no es válido o ya fue utilizado."
                            );
                            return;
                        }

                        idRecuperacion =
                            rs.getInt("id_recuperacion");

                        idCliente = rs.getInt("id_cliente");

                        expiracion =
                            rs.getTimestamp("fecha_expiracion");

                        utilizado = rs.getBoolean("utilizado");
                    }
                }

                if (utilizado
                        || expiracion == null
                        || !expiracion.toLocalDateTime()
                            .isAfter(LocalDateTime.now())) {

                    con.rollback();

                    mostrarMensaje(
                        response,
                        "El enlace venció o ya fue utilizado."
                    );
                    return;
                }

                String nuevaClave = BCrypt.hashpw(
                    contrasena,
                    BCrypt.gensalt(12)
                );

                String actualizarClave =
                    "UPDATE clientes SET contrasena = ? "
                    + "WHERE id_cliente = ?";

                try (PreparedStatement ps =
                        con.prepareStatement(actualizarClave)) {

                    ps.setString(1, nuevaClave);
                    ps.setInt(2, idCliente);

                    if (ps.executeUpdate() != 1) {
                        throw new Exception(
                            "No se pudo actualizar la contraseña."
                        );
                    }
                }

                String marcarUtilizado =
                    "UPDATE recuperacion_contrasenas "
                    + "SET utilizado = TRUE "
                    + "WHERE id_recuperacion = ?";

                try (PreparedStatement ps =
                        con.prepareStatement(marcarUtilizado)) {

                    ps.setInt(1, idRecuperacion);
                    ps.executeUpdate();
                }

                con.commit();

                mostrarMensaje(
                    response,
                    "¡Contraseña actualizada correctamente! "
                    + "Ya puedes iniciar sesión."
                );

            } catch (Exception e) {
                con.rollback();
                throw e;
            }

        } catch (Exception e) {

            getServletContext().log(
                "Error al restablecer contraseña", e
            );

            mostrarMensaje(
                response,
                "No se pudo completar la solicitud. "
                + "Inténtalo nuevamente."
            );
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

    private void mostrarMensaje(
        HttpServletResponse response,
        String mensaje) throws IOException {

    response.setContentType("text/html;charset=UTF-8");

    try (java.io.PrintWriter out = response.getWriter()) {

        out.println("<!DOCTYPE html>");
        out.println("<html lang='es'>");
        out.println("<head>");
        out.println("<meta charset='UTF-8'>");
        out.println("<meta name='viewport' "
                + "content='width=device-width, initial-scale=1.0'>");
        out.println("<title>Peluquería Elegance</title>");
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

        out.println("<h1>Recuperación de contraseña</h1>");
        out.println("<p>" + mensaje + "</p>");

        out.println("<a class='boton-mensaje' href='login.html'>");
        out.println("Ir a iniciar sesión");
        out.println("</a>");

        out.println("</div>");
        out.println("</section>");

        out.println("</body>");
        out.println("</html>");
    }
}
}