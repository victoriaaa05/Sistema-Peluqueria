package controlador;

import conexion.Conexion;

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

        // Recibimos los datos de login.html
        String correo = request.getParameter("correo");
        String contrasena = request.getParameter("contrasena");
        
        System.out.println("ENTRO AL LOGIN");
        System.out.println("Correo recibido: " + correo);

        String sql = """
                SELECT id_cliente, nombre, apellido
                FROM clientes
                WHERE correo = ? AND contrasena = ?
                """;

        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, correo);
            ps.setString(2, contrasena);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    
                     HttpSession session = request.getSession();
                     
                     session.setAttribute("id_cliente", rs.getInt("id_cliente"));
                     session.setAttribute("nombre", rs.getString("nombre"));
                     session.setAttribute("apellido", rs.getString("apellido"));

                    
                    System.out.println(
                            "Cliente conectado: "
                            + rs.getString("nombre")
                            + " "
                            + rs.getString("apellido")
                    );

                    response.sendRedirect("cliente.html");

                } else {

                    // Correo o contraseña incorrectos
                    response.sendRedirect("login.html?error=1");
                }
            }

        } catch (SQLException e) {
            throw new ServletException(
                    "Error al iniciar sesión",
                    e
            );
        }
    }
}