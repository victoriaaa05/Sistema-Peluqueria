package controlador;

import conexion.Conexion;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@WebServlet("/ServiciosDisponiblesServlet")
public class ServiciosDisponiblesServlet extends HttpServlet {

    private String escaparJson(String texto) {
        if (texto == null) return "";

        StringBuilder resultado = new StringBuilder();

        for (char c : texto.toCharArray()) {
            switch (c) {
                case '"' -> resultado.append("\\\"");
                case '\\' -> resultado.append("\\\\");
                case '\n' -> resultado.append("\\n");
                case '\r' -> resultado.append("\\r");
                case '\t' -> resultado.append("\\t");
                default -> {
                    if (c < 32) {
                        resultado.append(
                            String.format("\\u%04x", (int) c));
                    } else {
                        resultado.append(c);
                    }
                }
            }
        }

        return resultado.toString();
    }

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession sesion = request.getSession(false);

        if (sesion == null
                || !(sesion.getAttribute("id_cliente")
                     instanceof Integer)) {

            response.sendError(
                HttpServletResponse.SC_UNAUTHORIZED,
                "Debes iniciar sesión");
            return;
        }

        response.setContentType(
                "application/json;charset=UTF-8");
        response.setHeader("Cache-Control", "no-store");

        String sql = """
                SELECT nombre, precio, duracion_minutos
                FROM public.servicios
                WHERE activo = true
                ORDER BY nombre
                """;

        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            StringBuilder json = new StringBuilder("[");
            boolean primero = true;

            while (rs.next()) {
                if (!primero) json.append(",");

                primero = false;

                json.append("{")
                    .append("\"nombre\":\"")
                    .append(escaparJson(rs.getString("nombre")))
                    .append("\",")
                    .append("\"precio\":")
                    .append(rs.getBigDecimal("precio"))
                    .append(",")
                    .append("\"duracion\":")
                    .append(rs.getInt("duracion_minutos"))
                    .append("}");
            }

            json.append("]");

            try (PrintWriter out = response.getWriter()) {
                out.print(json);
            }

        } catch (SQLException e) {
            getServletContext().log(
                    "Error al consultar servicios", e);

            response.sendError(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "No se pudieron cargar los servicios");
        }
    }
}