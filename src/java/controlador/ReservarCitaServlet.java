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
import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

@WebServlet(
    name = "ReservarCitaServlet",
    urlPatterns = {"/ReservarCitaServlet"}
)
public class ReservarCitaServlet extends HttpServlet {

    private static final Set<String> SERVICIOS = Set.of(
        "Corte de cabello",
        "Peinado",
        "Coloración",
        "Tratamiento capilar",
        "Manicura"
    );

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        // Comprobar que el cliente inició sesión
        HttpSession sesion = request.getSession(false);

        if (sesion == null
                || !(sesion.getAttribute("id_cliente") instanceof Integer)) {

            response.sendRedirect(
                request.getContextPath() + "/login.html"
            );
            return;
        }

        int idCliente = (Integer) sesion.getAttribute("id_cliente");

        String servicio = request.getParameter("servicio");
        String fechaTexto = request.getParameter("fecha");
        String horaTexto = request.getParameter("hora");

        LocalDate fecha;
        LocalTime hora;

        // Validar los datos recibidos
        try {
            if (servicio == null || !SERVICIOS.contains(servicio)) {
                throw new IllegalArgumentException("Servicio inválido.");
            }

            fecha = LocalDate.parse(fechaTexto);
            hora = LocalTime.parse(horaTexto);

            if (fecha.isBefore(LocalDate.now())
                    || (fecha.equals(LocalDate.now())
                    && !hora.isAfter(LocalTime.now()))) {

                mostrarMensaje(response,
                    "Selecciona una fecha y hora futuras.");
                return;
            }

        } catch (Exception e) {
            mostrarMensaje(response,
                "Los datos de la cita no son válidos.");
            return;
        }

        try (Connection con = Conexion.getConexion()) {

            // Comprobar si el horario está ocupado
            String verificar =
                "SELECT id_cita FROM citas "
                + "WHERE fecha = ? AND hora = ? "
                + "AND estado <> 'Cancelada'";

            try (PreparedStatement ps =
                    con.prepareStatement(verificar)) {

                ps.setDate(1, Date.valueOf(fecha));
                ps.setTime(2, Time.valueOf(hora));

                try (ResultSet rs = ps.executeQuery()) {

                    if (rs.next()) {
                        mostrarMensaje(response,
                            "Ese horario ya está reservado. "
                            + "Elige otra hora.");
                        return;
                    }
                }
            }

            // Guardar la reserva
            String insertar =
                "INSERT INTO citas "
                + "(id_cliente, servicio, fecha, hora) "
                + "VALUES (?, ?, ?, ?)";

            try (PreparedStatement ps =
                    con.prepareStatement(insertar)) {

                ps.setInt(1, idCliente);
                ps.setString(2, servicio);
                ps.setDate(3, Date.valueOf(fecha));
                ps.setTime(4, Time.valueOf(hora));

                ps.executeUpdate();
            }

            mostrarMensaje(response,
                "¡Tu cita fue registrada correctamente! "
                + "Estado: Pendiente.");

        } catch (java.sql.SQLException e) {

    boolean horarioDuplicado = false;

    if ("23505".equals(e.getSQLState())
            && e instanceof org.postgresql.util.PSQLException) {

        org.postgresql.util.PSQLException errorPg =
                (org.postgresql.util.PSQLException) e;

        org.postgresql.util.ServerErrorMessage detalle =
                errorPg.getServerErrorMessage();

        horarioDuplicado = detalle != null
                && "idx_citas_horario_ocupado".equals(
                        detalle.getConstraint());
    }

    if (horarioDuplicado) {

        mostrarMensaje(response,
            "Ese horario acaba de ser reservado "
            + "por otro cliente. Elige otra hora.");

    } else {

        getServletContext().log(
            "Error de base de datos al reservar cita", e
        );

        mostrarMensaje(response,
            "No se pudo registrar la cita. "
            + "Inténtalo nuevamente.");
    }

} catch (Exception e) {

    getServletContext().log(
        "Error al reservar cita", e
    );

    mostrarMensaje(response,
        "No se pudo registrar la cita. "
        + "Inténtalo nuevamente.");
}
    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.sendRedirect(
            request.getContextPath() + "/reservar.html"
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
            out.println("</header>");

            out.println("<section class='pagina-mensaje'>");
            out.println("<div class='tarjeta-mensaje'>");

            out.println("<h1>Reserva de cita</h1>");
            out.println("<p>" + mensaje + "</p>");

            out.println("<a class='boton-mensaje' "
                    + "href='cliente.html'>");
            out.println("Volver a mi cuenta");
            out.println("</a>");

            out.println("</div>");
            out.println("</section>");

            out.println("</body>");
            out.println("</html>");
        }
    }
}