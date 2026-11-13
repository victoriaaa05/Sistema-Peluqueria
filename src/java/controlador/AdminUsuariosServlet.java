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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@WebServlet("/AdminUsuariosServlet")
public class AdminUsuariosServlet extends HttpServlet {

    private boolean esAdmin(HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        return session != null
                && session.getAttribute("id_cliente") != null
                && "ADMIN".equals(session.getAttribute("rol"));
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
            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "Acceso solo para administradores");
            return;
        }

        response.setContentType("text/html;charset=UTF-8");

        String sql = """
                SELECT id_cliente, nombre, apellido,
                       correo, telefono, rol
                FROM public.clientes
                ORDER BY id_cliente
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
                    <title>Gestión de Usuarios</title>
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
                            border-radius: 6px;
                        }
                        .tabla { overflow-x: auto; }
                        .nota { color: #666; }
                    </style>
                </head>
                <body>
                <div class="contenedor">
                    <a class="volver" href="AdminServlet">
                        Volver al panel
                    </a>
                    <h1>Gestión de Usuarios y Roles</h1>
                    <p class="nota">
                        Consultá los usuarios registrados y
                        administrá sus permisos.
                    </p>
                    <div class="tabla">
                    <table>
                    <thead>
                    <tr>
                        <th>ID</th>
                        <th>Nombre</th>
                        <th>Correo</th>
                        <th>Teléfono</th>
                        <th>Rol</th>
                        <th>Acción</th>
                    </tr>
                    </thead>
                    <tbody>
                    """);

            while (rs.next()) {
                int id = rs.getInt("id_cliente");

                String nombre = escapar(
                        rs.getString("nombre") + " "
                        + rs.getString("apellido"));

                String correo = escapar(
                        rs.getString("correo"));

                String telefono = escapar(
                        rs.getString("telefono"));

                String rol = rs.getString("rol");

                out.println("<tr>");
                out.println("<td>" + id + "</td>");
                out.println("<td>" + nombre + "</td>");
                out.println("<td>" + correo + "</td>");
                out.println("<td>" + telefono + "</td>");

                out.println("<td>" + escapar(rol) + "</td>");

                out.println("<td>");

                if (id == 1) {
                    out.println("Administradora principal");
                } else {
                    
                out.println("""
                    <form method="post"
                        action="AdminUsuariosServlet">
                """);
                
                out.println(
                    "<input type='hidden' name='csrf_token' value='"
                    + CsrfToken.obtener(request.getSession())
                    + "'>"
                );

                    out.println(
                        "<input type='hidden' name='id' "
                        + "value='" + id + "'>");

                    out.println("""
                        <select name="rol">
                            <option value="CLIENTE"
                        """);

                    if ("CLIENTE".equals(rol)) {
                        out.println("selected");
                    }

                    out.println("""
                            >CLIENTE</option>
                            <option value="ADMIN"
                        """);

                    if ("ADMIN".equals(rol)) {
                        out.println("selected");
                    }

                    out.println("""
                            >ADMIN</option>
                        </select>
                        <button type="submit">
                            Guardar rol
                        </button>
                        </form>
                        """);
                }

                out.println("</td></tr>");
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
                    "Error al consultar usuarios", e);
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
        if (!CsrfToken.validar(request)) {
            response.sendError(
                HttpServletResponse.SC_FORBIDDEN,
                "Solicitud no autorizada: token CSRF inválido."
            );
            return;
        }

        String idTexto = request.getParameter("id");
        String nuevoRol = request.getParameter("rol");

        if (!"CLIENTE".equals(nuevoRol)
                && !"ADMIN".equals(nuevoRol)) {
            response.sendError(400, "Rol inválido");
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

        // Proteger a la administradora principal
        if (id == 1) {
            response.sendError(403,
                    "No se puede modificar esta cuenta");
            return;
        }

        String sql = """
                UPDATE public.clientes
                SET rol = ?
                WHERE id_cliente = ?
                """;

        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nuevoRol);
            ps.setInt(2, id);
            ps.executeUpdate();

            response.sendRedirect(
                    request.getContextPath()
                    + "/AdminUsuariosServlet");

        } catch (SQLException e) {
            throw new ServletException(
                    "Error al actualizar rol", e);
        }
    }
}