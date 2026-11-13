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
import java.time.ZoneId;


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

            // Zona horaria de Paraguay
ZoneId zonaParaguay = ZoneId.of("America/Asuncion");

LocalDate hoy = LocalDate.now(zonaParaguay);
LocalTime horaActual = LocalTime.now(zonaParaguay);

// Horario de atención
LocalTime apertura = LocalTime.of(9, 0);
LocalTime cierre = LocalTime.of(18, 0);

// Validar que la fecha y hora sean futuras
if (fecha.isBefore(hoy)
        || (fecha.equals(hoy)
        && !hora.isAfter(horaActual))) {

    mostrarMensaje(response,
            "Seleccioná una fecha y hora futuras.");
    return;
}

// Validar el horario de atención
if (hora.isBefore(apertura) || hora.isAfter(cierre)) {

    mostrarMensaje(response,
            "El horario de atención es de 09:00 a 18:00.");
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

            mostrarConfirmacion(response);

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
    
private void mostrarConfirmacion(HttpServletResponse response)
        throws IOException {

    response.setContentType("text/html;charset=UTF-8");

    try (PrintWriter out = response.getWriter()) {

        out.println("""
            <!DOCTYPE html>
            <html lang="es">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport"
                      content="width=device-width, initial-scale=1.0">

                <title>Cita registrada - Peluquería Elegance</title>

                <style>
                    body {
                        margin: 0;
                        min-height: 100vh;
                        font-family: Arial, sans-serif;
                        background: #fff0f7;
                        display: flex;
                        justify-content: center;
                        align-items: center;
                        padding: 25px;
                        box-sizing: border-box;
                    }

                    .confirmacion {
                        background: white;
                        width: 100%;
                        max-width: 650px;
                        padding: 35px 25px;
                        border-radius: 22px;
                        text-align: center;
                        box-shadow: 0 8px 30px #0001;
                        box-sizing: border-box;
                    }

                    .marca {
                        color: #b83272;
                        font-size: 15px;
                        font-weight: bold;
                        letter-spacing: 2px;
                    }

                    h1 {
                        font-family: Georgia, serif;
                        color: #30202b;
                        font-size: 36px;
                        margin: 20px 0 10px;
                    }

                    .subtitulo {
                        color: #80546b;
                        font-size: 18px;
                        line-height: 1.6;
                    }

                    .imagenes-confirmacion {
                        display: flex;
                        justify-content: center;
                        align-items: flex-end;
                        gap: 18px;
                        margin: 30px auto;
                    }

                    .hamster {
                        width: 190px;
                        height: 230px;
                        object-fit: cover;
                        border-radius: 15px;
                    }

                    .monito {
                        width: 120px;
                        height: 140px;
                        object-fit: cover;
                        border-radius: 15px;
                        transform: rotate(7deg);
                    }

                    .estado {
                        display: inline-block;
                        background: #fce4ef;
                        color: #8c315c;
                        padding: 10px 20px;
                        border-radius: 25px;
                        font-size: 14px;
                        margin-bottom: 25px;
                    }

                    .boton {
                        display: inline-block;
                        background: #b83272;
                        color: white;
                        padding: 14px 28px;
                        border-radius: 25px;
                        text-decoration: none;
                        font-weight: bold;
                    }

                    .boton:hover {
                        background: #932557;
                    }

                    @media (max-width: 480px) {
                        h1 {
                            font-size: 28px;
                        }

                        .hamster {
                            width: 150px;
                            height: 190px;
                        }

                        .monito {
                            width: 85px;
                            height: 110px;
                        }
                    }
                </style>
            </head>

            <body>

                <main class="confirmacion">

                    <div class="marca">
                        PELUQUERÍA ELEGANCE
                    </div>

                    <h1>¡Tu cita está agendada! 💗</h1>

                    <p class="subtitulo">
                        Preparate para brillar, ¡te esperamos!
                    </p>

                    <div class="imagenes-confirmacion">

                        <img class="hamster"
                             src="img/gracias.png"
                             alt="Hámster con ruleros">

                        <img class="monito"
                             src="img/cita_agendada.jpg"
                             alt="Monito con corona">

                    </div>

                    <div class="estado">
                        Estado de tu cita: Pendiente
                    </div>

                    <p class="subtitulo">
                        Tu momento de belleza está cada vez más cerca.
                    </p>

                    <a class="boton" href="MisCitasServlet">
                        Ver mis citas
                    </a>

                </main>

            </body>
            </html>
            """);
    }
}

}