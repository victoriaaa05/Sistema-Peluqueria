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

@WebServlet(
    name = "MisCitasServlet",
    urlPatterns = {"/MisCitasServlet"}
)
public class MisCitasServlet extends HttpServlet {

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        // Verificar que el cliente haya iniciado sesión
        HttpSession sesion = request.getSession(false);

        if (sesion == null
                || !(sesion.getAttribute("id_cliente") instanceof Integer)) {

            response.sendRedirect(
                request.getContextPath() + "/login.html"
            );
            return;
        }

        int idCliente = (Integer) sesion.getAttribute("id_cliente");

        response.setContentType("text/html;charset=UTF-8");

        try (PrintWriter out = response.getWriter()) {

            out.println("<!DOCTYPE html>");
            out.println("<html lang='es'>");

            out.println("<head>");
            out.println("<meta charset='UTF-8'>");
            out.println("<meta name='viewport' "
                    + "content='width=device-width, initial-scale=1.0'>");
            out.println("<title>Mis citas - Peluquería Elegance</title>");
            out.println("<link rel='stylesheet' href='css/estilos.css'>");
            out.println("</head>");

            out.println("<body>");

            out.println("<header>");
            out.println("<div class='logo'>Peluquería Elegance</div>");
            out.println("<nav>");
            out.println("<a href='cliente.html'>Mi cuenta</a>");
            out.println("</nav>");
            out.println("</header>");

            out.println("<section class='panel-cliente'>");
            out.println("<div class='contenido-cliente'>");

            out.println("<h1>Mis citas</h1>");
            out.println("<p>Consulta tus reservas en la peluquería.</p>");

            String consulta =
                "SELECT id_cita, servicio, fecha, hora, estado "
                + "FROM citas "
                + "WHERE id_cliente = ? "
                + "ORDER BY fecha DESC, hora DESC";

            try (Connection con = Conexion.getConexion();
                 PreparedStatement ps =
                         con.prepareStatement(consulta)) {

                ps.setInt(1, idCliente);

                try (ResultSet rs = ps.executeQuery()) {

                    boolean tieneCitas = false;

                    out.println("<div class='lista-citas'>");

                    while (rs.next()) {

                        tieneCitas = true;

                        int idCita = rs.getInt("id_cita");
                        String servicio = escaparHtml(
                                rs.getString("servicio")
                        );
                        String fecha = rs.getDate("fecha").toString();
                        String hora = rs.getTime("hora").toString();
                        String estado = escaparHtml(
                                rs.getString("estado")
                        );

                        out.println("<div class='tarjeta-cita'>");

                        out.println("<h2>" + servicio + "</h2>");
                        out.println("<p><strong>Cita N.º:</strong> "
                                + idCita + "</p>");
                        out.println("<p><strong>Fecha:</strong> "
                                + fecha + "</p>");
                        out.println("<p><strong>Hora:</strong> "
                                + hora + "</p>");
                        out.println("<p><strong>Estado:</strong> "
        + estado + "</p>");

// Mostrar el botón solamente para citas pendientes y futuras
java.time.LocalDate fechaCita =
        rs.getDate("fecha").toLocalDate();

java.time.LocalTime horaCita =
        rs.getTime("hora").toLocalTime();

java.time.LocalDateTime fechaHoraCita =
        java.time.LocalDateTime.of(fechaCita, horaCita);

if ("Pendiente".equals(estado)
        && fechaHoraCita.isAfter(java.time.LocalDateTime.now())) {

    out.println("<form action='CancelarCitaServlet' method='POST'>");

    out.println("<input type='hidden' name='id_cita' value='"
            + idCita + "'>");

    out.println("<button type='submit' class='btn-cancelar' "
            + "onclick=\"return confirm('¿Seguro que deseas cancelar esta cita?');\">");

    out.println("Cancelar cita");
    out.println("</button>");

    out.println("</form>");
}

out.println("</div>");
                    }

                    if (!tieneCitas) {
                        out.println("<p>Todavía no tienes citas registradas.</p>");
                    }

                    out.println("</div>");
                }

            } catch (Exception e) {

                getServletContext().log(
                    "Error al consultar citas", e
                );

                out.println("<p>No se pudieron cargar tus citas.</p>");
            }

            out.println("<p style='margin-top:30px;'>");
            out.println("<a class='boton-mensaje' href='cliente.html'>");
            out.println("Volver a mi cuenta");
            out.println("</a>");
            out.println("</p>");

            out.println("</div>");
            out.println("</section>");

            out.println("</body>");
            out.println("</html>");
        }
    }

    // Evitar que los datos de la base se interpreten como HTML
    private String escaparHtml(String texto) {

        if (texto == null) {
            return "";
        }

        return texto.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&#39;");
    }
}