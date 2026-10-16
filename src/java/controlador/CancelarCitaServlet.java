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

@WebServlet(
    name = "CancelarCitaServlet",
    urlPatterns = {"/CancelarCitaServlet"}
)
public class CancelarCitaServlet extends HttpServlet {

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        // Verificar que el cliente inició sesión
        HttpSession sesion = request.getSession(false);

        if (sesion == null
                || !(sesion.getAttribute("id_cliente") instanceof Integer)) {

            response.sendRedirect(
                request.getContextPath() + "/login.html"
            );
            return;
        }

        int idCliente = (Integer) sesion.getAttribute("id_cliente");

        // Obtener el número de la cita
        int idCita;

        try {
            idCita = Integer.parseInt(
                request.getParameter("id_cita")
            );

            if (idCita <= 0) {
                throw new NumberFormatException();
            }

        } catch (NumberFormatException e) {
            response.sendError(
                HttpServletResponse.SC_BAD_REQUEST,
                "Número de cita inválido."
            );
            return;
        }

        // Cancelar solamente citas propias, pendientes y futuras
        String actualizar =
            "UPDATE citas "
            + "SET estado = 'Cancelada' "
            + "WHERE id_cita = ? "
            + "AND id_cliente = ? "
            + "AND estado = 'Pendiente' "
            + "AND (fecha + hora) > CURRENT_TIMESTAMP";

        try (Connection con = Conexion.getConexion();
             PreparedStatement ps =
                     con.prepareStatement(actualizar)) {

            ps.setInt(1, idCita);
            ps.setInt(2, idCliente);

            int filas = ps.executeUpdate();

            if (filas == 1) {
                response.sendRedirect(
                    request.getContextPath()
                    + "/MisCitasServlet?cancelada=1"
                );
            } else {
                response.sendRedirect(
                    request.getContextPath()
                    + "/MisCitasServlet?cancelada=0"
                );
            }

        } catch (Exception e) {

            getServletContext().log(
                "Error al cancelar cita", e
            );

            response.sendError(
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "No se pudo cancelar la cita."
            );
        }
    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.sendRedirect(
            request.getContextPath() + "/MisCitasServlet"
        );
    }
}