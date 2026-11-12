package controlador;

import conexion.Conexion;
import org.mindrot.jbcrypt.BCrypt;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@WebServlet("/LoginServlet")
public class LoginServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        String correo = request.getParameter("correo");
        String contrasena = request.getParameter("contrasena");

        if (correo == null || contrasena == null
                || correo.isBlank() || contrasena.isBlank()) {
            response.sendRedirect("login.html?error=1");
            return;
        }

        String sql = """
                SELECT id_cliente, nombre, apellido,
                       contrasena, rol
                FROM clientes
                WHERE correo = ?
                """;

        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, correo.trim());

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {

                    int idCliente = rs.getInt("id_cliente");
                    String contrasenaGuardada =
                            rs.getString("contrasena");
                    String rol = rs.getString("rol");

                    boolean accesoPermitido = false;

                    if (contrasenaGuardada != null
                            && contrasenaGuardada.matches(
                                    "^\\$2[aby]\\$\\d{2}\\$.*")) {

                        try {
                            accesoPermitido = BCrypt.checkpw(
                                    contrasena, contrasenaGuardada
                            );
                        } catch (IllegalArgumentException e) {
                            accesoPermitido = false;
                        }

                    } else if (contrasenaGuardada != null
                            && contrasenaGuardada.equals(contrasena)) {

                        accesoPermitido = true;

                        // Migrar contraseña antigua a BCrypt
                        String hash = BCrypt.hashpw(
                                contrasena, BCrypt.gensalt(12)
                        );

                        String actualizar = """
                                UPDATE clientes
                                SET contrasena = ?
                                WHERE id_cliente = ?
                                """;

                        try (PreparedStatement psActualizar =
                                con.prepareStatement(actualizar)) {

                            psActualizar.setString(1, hash);
                            psActualizar.setInt(2, idCliente);
                            psActualizar.executeUpdate();
                        }
                    }

                    if (accesoPermitido) {

                        // Evitar reutilizar una sesión anterior
                        HttpSession anterior =
                                request.getSession(false);

                        if (anterior != null) {
                            anterior.invalidate();
                        }

                        HttpSession session =
                                request.getSession(true);

                        session.setAttribute(
                                "id_cliente", idCliente);
                        session.setAttribute(
                                "nombre", rs.getString("nombre"));
                        session.setAttribute(
                                "apellido", rs.getString("apellido"));
                        session.setAttribute("rol", rol);

                        if ("ADMIN".equals(rol)) {
                            response.sendRedirect(
                                    request.getContextPath()
                                    + "/AdminServlet");
                        } else {
                            response.sendRedirect(
                                    request.getContextPath()
                                    + "/cliente.html");
                        }

                        return;
                    }
                }

                response.sendRedirect(
                        request.getContextPath()
                        + "/login.html?error=1");
            }

        } catch (SQLException e) {
            throw new ServletException(
                    "Error al iniciar sesión", e
            );
        }
    }
}