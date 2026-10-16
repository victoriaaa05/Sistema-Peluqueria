package controlador;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebFilter(
    filterName = "FiltroSesion",
    urlPatterns = {"/cliente.html", "/reservar.html"}
)
public class FiltroSesion implements Filter {

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest peticion =
                (HttpServletRequest) request;

        HttpServletResponse respuesta =
                (HttpServletResponse) response;

        // Evitar guardar páginas privadas en caché
        respuesta.setHeader(
                "Cache-Control",
                "no-cache, no-store, must-revalidate"
        );
        respuesta.setHeader("Pragma", "no-cache");
        respuesta.setDateHeader("Expires", 0);

        // Comprobar si existe una sesión
        HttpSession sesion = peticion.getSession(false);

        if (sesion != null
                && sesion.getAttribute("id_cliente") != null) {

            // El cliente inició sesión: permitir el acceso
            chain.doFilter(request, response);

        } else {

            // No inició sesión: enviarlo al login
            respuesta.sendRedirect(
                    peticion.getContextPath() + "/login.html"
            );
        }
    }
}