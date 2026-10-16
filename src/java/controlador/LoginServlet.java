
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

        String sql = """
                SELECT id_cliente, nombre, apellido, contrasena
                FROM clientes
                WHERE correo = ?
                """;

        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, correo);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {

                    int idCliente = rs.getInt("id_cliente");
                    String contrasenaGuardada =
                            rs.getString("contrasena");

                    boolean accesoPermitido = false;

                    // Verificar contraseña protegida con BCrypt
                    if (contrasenaGuardada != null
                            && contrasenaGuardada.matches(
                                    "^\\$2[aby]\\$\\d{2}\\$.*")) {

                        accesoPermitido = BCrypt.checkpw(
                                contrasena, contrasenaGuardada
                        );

                    } else if (contrasenaGuardada != null
                            && contrasenaGuardada.equals(contrasena)) {

                        // Compatibilidad temporal con clientes antiguos
                        accesoPermitido = true;

                        // Convertir contraseña antigua a BCrypt
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

                        HttpSession session = request.getSession();
                        session.setAttribute("id_cliente", idCliente);
                        session.setAttribute(
                                "nombre", rs.getString("nombre"));
                        session.setAttribute(
                                "apellido", rs.getString("apellido"));

                        response.sendRedirect("cliente.html");
                        return;
                    }
                }

                response.sendRedirect("login.html?error=1");
            }

        } catch (SQLException e) {
            throw new ServletException(
                    "Error al iniciar sesión", e
            );
        }
    }
}
