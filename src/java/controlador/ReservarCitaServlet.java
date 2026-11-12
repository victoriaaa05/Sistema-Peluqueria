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
import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;

@WebServlet("/ReservarCitaServlet")
public class ReservarCitaServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        HttpSession sesion = request.getSession(false);

        if (sesion == null
                || !(sesion.getAttribute("id_cliente")
                     instanceof Integer)) {

            response.sendRedirect(
                    request.getContextPath() + "/login.html");
            return;
        }

        if (!"CLIENTE".equals(
                sesion.getAttribute("rol"))) {
            response.sendError(403,
                    "Solo los clientes pueden reservar citas.");
            return;
        }

        int idCliente =
                (Integer) sesion.getAttribute("id_cliente");

        String servicio = request.getParameter("servicio");
        String fechaTexto = request.getParameter("fecha");
        String horaTexto = request.getParameter("hora");

        LocalDate fecha;
        LocalTime hora;

        try {
            if (servicio == null || servicio.isBlank()) {
                throw new IllegalArgumentException();
            }

            fecha = LocalDate.parse(fechaTexto);
            hora = LocalTime.parse(horaTexto);

            if (fecha.isBefore(LocalDate.now())
                    || (fecha.equals(LocalDate.now())
                    && !hora.isAfter(LocalTime.now()))) {

                mostrarMensaje(response,
                        "Seleccioná una fecha y hora futuras.");
                return;
            }

        } catch (Exception e) {
            mostrarMensaje(response,
                    "Los datos de la cita no son válidos.");
            return;
        }

        try (Connection con = Conexion.getConexion()) {

            // Comprobar que el servicio exista y esté activo.
            String consultarServicio = """
                    SELECT nombre
                    FROM public.servicios
                    WHERE nombre = ?
                      AND activo = true
                    """;

            try (PreparedStatement ps =
                    con.prepareStatement(consultarServicio)) {

                ps.setString(1, servicio);

                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        mostrarMensaje(response,
                                "El servicio seleccionado no está "
                                + "disponible. Elegí otro.");
                        return;
                    }
                }
            }

            // Verificar disponibilidad del horario.
            String verificar = """
                    SELECT id_cita
                    FROM public.citas
                    WHERE fecha = ?
                      AND hora = ?
                      AND estado <> 'Cancelada'
                    """;

            try (PreparedStatement ps =
                    con.prepareStatement(verificar)) {

                ps.setDate(1, Date.valueOf(fecha));
                ps.setTime(2, Time.valueOf(hora));

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        mostrarMensaje(response,
                                "Ese horario ya está reservado. "
                                + "Elegí otra hora.");
                        return;
                    }
                }
            }

            String insertar = """
                    INSERT INTO public.citas
                        (id_cliente, servicio, fecha, hora)
                    VALUES (?, ?, ?, ?)
                    """;

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

        } catch (SQLException e) {

            if ("23505".equals(e.getSQLState())) {
                mostrarMensaje(response,
                        "Ese horario acaba de ser reservado. "
                        + "Elegí otra hora.");
            } else {
                getServletContext().log(
                        "Error al reservar cita", e);

                mostrarMensaje(response,
                        "No se pudo registrar la cita. "
                        + "Intentá nuevamente.");
            }
        }
    }

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        response.sendRedirect(
                request.getContextPath() + "/reservar.html");
    }

    private void mostrarMensaje(HttpServletResponse response,
                                String mensaje)
            throws IOException {

        response.setContentType("text/html;charset=UTF-8");

        try (PrintWriter out = response.getWriter()) {

            out.println("""
                    <!DOCTYPE html>
                    <html lang="es">
                    <head>
                        <meta charset="UTF-8">
                        <meta name="viewport"
                              content="width=device-width,
                              initial-scale=1.0">
                        <title>Peluquería Elegance</title>
                        <link rel="stylesheet"
                              href="css/estilos.css">
                    </head>
                    <body>
                        <header>
                            <div class="logo">
                                Peluquería Elegance
                            </div>
                        </header>
                        <section class="pagina-mensaje">
                            <div class="tarjeta-mensaje">
                                <h1>Reserva de cita</h1>
                    """);

            out.println("<p>" + mensaje + "</p>");

            out.println("""
                                <a class="boton-mensaje"
                                   href="cliente.html">
                                    Volver a mi cuenta
                                </a>
                            </div>
                        </section>
                    </body>
                    </html>
                    """);
        }
    }
}