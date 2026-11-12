package controlador;

import conexion.Conexion;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import seguridad.CsrfToken;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@WebServlet("/AdminServiciosServlet")
public class AdminServiciosServlet extends HttpServlet {

    private boolean esAdmin(HttpServletRequest request) {
        HttpSession sesion = request.getSession(false);
        return sesion != null
                && sesion.getAttribute("id_cliente") != null
                && "ADMIN".equals(sesion.getAttribute("rol"));
    }

    private String escapar(String valor) {
        if (valor == null) return "";
        return valor.replace("&", "&amp;")
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
            response.sendError(403, "Acceso solo para administradores");
            return;
        }

        response.setContentType("text/html;charset=UTF-8");

        String sql = """
                SELECT id_servicio, nombre, descripcion,
                       precio, duracion_minutos, activo
                FROM public.servicios
                ORDER BY id_servicio
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
                <title>Gestión de Servicios</title>
                <style>
                    body {
                        font-family: Arial, sans-serif;
                        background: #fff1f6;
                        padding: 30px;
                    }
                    .contenedor {
                        max-width: 1050px;
                        margin: auto;
                        background: white;
                        padding: 30px;
                        border-radius: 16px;
                    }
                    h1, h2 { color: #b83272; }
                    input, textarea {
                        padding: 9px;
                        margin: 5px;
                        border: 1px solid #ddd;
                        border-radius: 7px;
                    }
                    button, .volver {
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
                        margin-top: 20px;
                    }
                    th, td {
                        padding: 12px;
                        border-bottom: 1px solid #eee;
                        text-align: left;
                    }
                    th { background: #fce4ef; }
                    .tabla { overflow-x: auto; }
                    .fila-formulario {
                        display: flex;
                        flex-wrap: wrap;
                        align-items: center;
                    }
                    .acciones form { display: inline; }
                </style>
                </head>
                <body>
                <div class="contenedor">
                    <a class="volver" href="AdminServlet">
                        Volver al panel
                    </a>
                    <h1>Gestión de Servicios</h1>
                    <h2>Agregar nuevo servicio</h2>

                    <form method="post"
                        action="AdminServiciosServlet">
                    """);
                               
                    out.println(
                         "<input type='hidden' name='csrf_token' value='"
                           + CsrfToken.obtener(request.getSession())
                           + "'>"
                         );
                               
                               out.println("""
                                   <input type="hidden"
                                          name="accion" value="agregar">
                        <input name="nombre"
                               placeholder="Nombre del servicio"
                               maxlength="100" required>
                        <input name="descripcion"
                               placeholder="Descripción">
                        <input name="precio" type="number"
                               step="0.01" min="0"
                               placeholder="Precio" required>
                        <input name="duracion" type="number"
                               min="1"
                               placeholder="Duración en minutos"
                               required>
                        <button type="submit">Agregar</button>
                    </form>

                    <h2>Servicios registrados</h2>
                    <div class="tabla">
                    <table>
                    <thead>
                    <tr>
                        <th>ID</th>
                        <th>Nombre y descripción</th>
                        <th>Precio</th>
                        <th>Duración</th>
                        <th>Estado</th>
                        <th>Acciones</th>
                    </tr>
                    </thead>
                    <tbody>
                    """);

            boolean hayServicios = false;

            while (rs.next()) {
                hayServicios = true;

                int id = rs.getInt("id_servicio");
                String nombre = escapar(rs.getString("nombre"));
                String descripcion =
                        escapar(rs.getString("descripcion"));
                String precio = rs.getBigDecimal("precio")
                        .toPlainString();
                int duracion = rs.getInt("duracion_minutos");
                boolean activo = rs.getBoolean("activo");

                out.println("<tr>");
                out.println("<td>" + id + "</td>");

                out.println("<td><form method='post' "
                        + "action='AdminServiciosServlet'>"
                        + "<input type='hidden' name='csrf_token' value='"
                        + CsrfToken.obtener(request.getSession())
                        + "'>"
                        + "<input type='hidden' name='accion' "
                        + "value='editar'>"
                        + "<input type='hidden' name='id' "
                        + "value='" + id + "'>"
                        + "<input name='nombre' maxlength='100' "
                        + "value='" + nombre + "' required>"
                        + "<input name='descripcion' "
                        + "value='" + descripcion + "'>");

                out.println("</td><td>"
                        + "<input name='precio' type='number' "
                        + "step='0.01' min='0' value='"
                        + precio + "' required>");

                out.println("</td><td>"
                        + "<input name='duracion' type='number' "
                        + "min='1' value='" + duracion
                        + "' required>");

                out.println("</td><td>"
                        + (activo ? "Activo" : "Inactivo"));

                out.println("</td><td class='acciones'>"
                        + "<button type='submit'>Guardar</button>"
                        + "</form>");

                out.println("<form method='post' "
                        + "action='AdminServiciosServlet'>"
                        + "<input type='hidden' name='csrf_token' value='"
                        + CsrfToken.obtener(request.getSession())
                        + "'>"
                        + "<input type='hidden' name='accion' "
                        + "value='cambiarEstado'>"
                        + "<input type='hidden' name='id' "
                        + "value='" + id + "'>"
                        + "<button type='submit'>"
                        + (activo ? "Desactivar" : "Activar")
                        + "</button></form></td>");

                out.println("</tr>");
            }

            if (!hayServicios) {
                out.println("""
                    <tr><td colspan="6">
                    Todavía no hay servicios registrados.
                    </td></tr>
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
                    "Error al consultar servicios", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        if (!esAdmin(request)) {
            response.sendError(403, "Acceso solo para administradores");
            return;
        }
        if (!CsrfToken.validar(request)) {
            response.sendError(
             HttpServletResponse.SC_FORBIDDEN,
             "Solicitud no autorizada: token CSRF inválido."
        );
        return;
        }

        request.setCharacterEncoding("UTF-8");

        String accion = request.getParameter("accion");

        if (accion == null) {
            response.sendError(400, "Acción no especificada");
            return;
        }

        try (Connection con = Conexion.getConexion()) {

            if ("agregar".equals(accion)
                    || "editar".equals(accion)) {

                String nombre = request.getParameter("nombre");
                String descripcion =
                        request.getParameter("descripcion");

                if (nombre == null || nombre.isBlank()
                        || nombre.length() > 100) {
                    response.sendError(400, "Nombre inválido");
                    return;
                }

                BigDecimal precio = new BigDecimal(
                        request.getParameter("precio"));
                int duracion = Integer.parseInt(
                        request.getParameter("duracion"));

                if (precio.signum() < 0 || duracion <= 0) {
                    response.sendError(400, "Valores inválidos");
                    return;
                }

                if ("agregar".equals(accion)) {
                    String sql = """
                            INSERT INTO public.servicios
                            (nombre, descripcion, precio,
                             duracion_minutos, activo)
                            VALUES (?, ?, ?, ?, true)
                            """;

                    try (PreparedStatement ps =
                            con.prepareStatement(sql)) {
                        ps.setString(1, nombre.trim());
                        ps.setString(2, descripcion);
                        ps.setBigDecimal(3, precio);
                        ps.setInt(4, duracion);
                        ps.executeUpdate();
                    }

                } else {
                    int id = Integer.parseInt(
                            request.getParameter("id"));

                    String sql = """
                            UPDATE public.servicios
                            SET nombre = ?, descripcion = ?,
                                precio = ?, duracion_minutos = ?
                            WHERE id_servicio = ?
                            """;

                    try (PreparedStatement ps =
                            con.prepareStatement(sql)) {
                        ps.setString(1, nombre.trim());
                        ps.setString(2, descripcion);
                        ps.setBigDecimal(3, precio);
                        ps.setInt(4, duracion);
                        ps.setInt(5, id);
                        ps.executeUpdate();
                    }
                }

            } else if ("cambiarEstado".equals(accion)) {

                int id = Integer.parseInt(
                        request.getParameter("id"));

                String sql = """
                        UPDATE public.servicios
                        SET activo = NOT activo
                        WHERE id_servicio = ?
                        """;

                try (PreparedStatement ps =
                        con.prepareStatement(sql)) {
                    ps.setInt(1, id);
                    ps.executeUpdate();
                }

            } else {
                response.sendError(400, "Acción desconocida");
                return;
            }

            response.sendRedirect(
                    request.getContextPath()
                    + "/AdminServiciosServlet");

        } catch (NumberFormatException
                 | NullPointerException e) {
            response.sendError(400, "Datos numéricos inválidos");

        } catch (SQLException e) {
            throw new ServletException(
                    "Error al guardar servicio", e);
        }
    }
}