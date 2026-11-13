package controlador;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet(
    name = "CerrarSesionServlet",
    urlPatterns = {"/CerrarSesionServlet"}
)
public class CerrarSesionServlet extends HttpServlet {

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        // Obtener la sesión actual sin crear una nueva
        HttpSession sesion = request.getSession(false);

        // Cerrar la sesión si existe
        if (sesion != null) {
            sesion.invalidate();
        }

        // Evitar que se guarde esta respuesta en caché
        response.setHeader("Cache-Control", "no-store");

        // Regresar a la página de inicio de sesión
        response.sendRedirect(
            request.getContextPath() + "/login.html"
        );
    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.sendRedirect(
            request.getContextPath() + "/login.html"
        );
    }
}