package controlador;

import conexion.Conexion;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@WebServlet("/RegistroServlet")
public class RegistroServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        // Recibir datos del formulario
        String nombre = request.getParameter("nombre");
        String apellido = request.getParameter("apellido");
        String telefono = request.getParameter("telefono");
        String correo = request.getParameter("correo");
        String contrasena = request.getParameter("contrasena");

        String sql = """
                INSERT INTO clientes
                (nombre, apellido, telefono, correo, contrasena)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nombre);
            ps.setString(2, apellido);
            ps.setString(3, telefono);
            ps.setString(4, correo);
            ps.setString(5, contrasena);

            ps.executeUpdate();

            response.sendRedirect("index.html");

        } catch (SQLException e) {
            throw new ServletException("Error al registrar cliente", e);
        }
    }
}