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

@WebServlet("/AdminCitasServlet")
public class AdminCitasServlet extends HttpServlet {

    private boolean esAdmin(HttpServletRequest request) {
        HttpSession sesion = request.getSession(false);

        return sesion != null
                && sesion.getAttribute("id_cliente") != null
                && "ADMIN".equals(sesion.getAttribute("rol"));
    }

    private String escapar(String texto) {
        if (texto == null) return "";

        return texto.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        if (!esAdmin(request)) {
            response.sendError(403,
                    "Acceso solo para administradores");
            return;
        }

        response.setContentType("text/html;charset=UTF-8");

        String sql = """
                SELECT c.id_cita,
                       cl.nombre,
                       cl.apellido,
                       c.servicio,
                       c.fecha,
                       c.hora,
                       c.estado
                FROM public.citas c
                INNER JOIN public.clientes cl
                    ON c.id_cliente = cl.id_cliente
                ORDER BY c.fecha DESC,
                         c.hora DESC,
                         c.id_cita DESC
                """;

        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery();
             PrintWriter out = response.getWriter()) {

            out.println("""
                <!DOCTYPE html>
                <html lang="es">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport"
                          content="width=device-width, initial-scale=1">
                    <title>Gestión de Citas</title>
                    <style>
                        body {
                            font-family: Arial, sans-serif;
                            background: #fff1f6;
                            padding: 30px;
                        }
                        .contenedor {
                            max-width: 1100px;
                            margin: auto;
                            background: white;
                            padding: 30px;
                            border-radius: 16px;
                        }
                        h1 { color: #b83272; }
                        .volver, button {
                            background: #b83272;
                            color: white;
                            border: none;
                            padding: 10px 15px;
                            border-radius: 7px;
                            cursor: pointer;
                            text-decoration: none;
                        }
                        table {
                            width: 100%;
                            border-collapse: collapse;
                            margin-top: 25px;
                        }
                        th, td {
                            padding: 12px;
                            border-bottom: 1px solid #eee;
                            text-align: left;
                        }
                        th { background: #fce4ef; }
                        select {
                            padding: 8px;
                            border: 1px solid #ddd;
                            border-radius: 6px;
                        }
                        .tabla { overflow-x: auto; }
                        .nota { color: #666; }
                        .estado {
                            font-weight: bold;
                            color: #8a2856;
                        }
                    </style>
                </head>
                </head>
                <body>
                <div class="contenedor">
                    <a class="volver" href="AdminServlet">
                        Volver al panel
                    </a>
                    <h1>Gestión de todas las citas</h1>
                    <p class="nota">
                        Consultá las reservas de los clientes
                        y actualizá su estado.
                    </p>

                    <div class="tabla">
                    <table>
                    <thead>
                    <tr>
                        <th>ID</th>
                        <th>Cliente</th>
                        <th>Servicio</th>
                        <th>Fecha</th>
                        <th>Hora</th>
                        <th>Estado actual</th>
                        <th>Acción</th>
                    </tr>
                    </thead>
                    <tbody>
                    """);

            boolean hayCitas = false;

            while (rs.next()) {
                hayCitas = true;

                int id = rs.getInt("id_cita");

                String cliente = escapar(
                        rs.getString("nombre") + " "
                        + rs.getString("apellido"));

                String servicio = escapar(
                        rs.getString("servicio"));

                String fecha = rs.getDate("fecha").toString();
                String hora = rs.getTime("hora").toString();
                String estado = rs.getString("estado");

                out.println("<tr>");
                out.println("<td>" + id + "</td>");
                out.println("<td>" + cliente + "</td>");
                out.println("<td>" + servicio + "</td>");
                out.println("<td>" + fecha + "</td>");
                out.println("<td>" + hora + "</td>");
                out.println("<td class='estado'>"
                        + escapar(estado) + "</td>");

                out.println("<td>");

                out.println(
                    "<form method='post' "
                    + "action='AdminCitasServlet'>"
                    + "<input type='hidden' name='id' "
                    + "value='" + id + "'>"
                    + "<select name='estado'>"
                );

                String[] estados = {
                    "Pendiente",
                    "Confirmada",
                    "Completada",
                    "Cancelada"
                };

                for (String opcion : estados) {
                    out.println("<option value='"
                            + opcion + "' "
                            + (opcion.equals(estado)
                               ? "selected" : "")
                            + ">"
                            + opcion + "</option>");
                }

                out.println("""
                    </select>
                    <button type="submit">
                        Guardar
                    </button>
                    </form>
                    """);

                out.println("</td></tr>");
            }

            if (!hayCitas) {
                out.println("""
                    <tr>
                        <td colspan="7">
                            Todavía no hay citas registradas.
                        </td>
                    </tr>
                    """);
            }

            out.println("""
                    </tbody>
                    </table>
                    </div>
                    </div>
                    </body>
                    </html>
                    """);

        } catch (SQLException e) {
            throw new ServletException(
                    "Error al consultar citas", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        if (!esAdmin(request)) {
            response.sendError(403,
                    "Acceso solo para administradores");
            return;
        }

        String idTexto = request.getParameter("id");
        String nuevoEstado = request.getParameter("estado");

        if (!"Pendiente".equals(nuevoEstado)
                && !"Confirmada".equals(nuevoEstado)
                && !"Completada".equals(nuevoEstado)
                && !"Cancelada".equals(nuevoEstado)) {

            response.sendError(400, "Estado inválido");
            return;
        }

        int id;

        try {
            id = Integer.parseInt(idTexto);
        } catch (NumberFormatException
                 | NullPointerException e) {

            response.sendError(400, "ID inválido");
            return;
        }

        String sql = """
                UPDATE public.citas
                SET estado = ?
                WHERE id_cita = ?
                """;

        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nuevoEstado);
            ps.setInt(2, id);

            int filas = ps.executeUpdate();

            if (filas == 0) {
                response.sendError(404, "Cita no encontrada");
                return;
            }

            response.sendRedirect(
                    request.getContextPath()
                    + "/AdminCitasServlet");

        } catch (SQLException e) {
            throw new ServletException(
                    "Error al actualizar la cita", e);
        }
    }
}