package controlador;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/AdminServlet")
public class AdminServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null
                || session.getAttribute("id_cliente") == null) {

            response.sendRedirect(
                    request.getContextPath() + "/login.html");
            return;
        }

        if (!"ADMIN".equals(session.getAttribute("rol"))) {
            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "No tienes permiso para acceder");
            return;
        }

        response.setContentType("text/html;charset=UTF-8");

        String nombre = String.valueOf(
                session.getAttribute("nombre"));

        try (PrintWriter out = response.getWriter()) {
            out.println("""
                <!DOCTYPE html>
                <html lang="es">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport"
                          content="width=device-width, initial-scale=1">
                    <title>Administración - Peluquería Elegance</title>
                    <style>
                        body {
                            font-family: Arial, sans-serif;
                            background: #fff1f6;
                            text-align: center;
                            padding: 50px 20px;
                        }
                        .panel {
                            max-width: 650px;
                            margin: auto;
                            background: white;
                            padding: 35px;
                            border-radius: 16px;
                            box-shadow: 0 4px 18px #0001;
                        }
                        h1 { color: #b83272; }
                        .opciones {
                            display: grid;
                            gap: 12px;
                            margin-top: 25px;
                        }
                        .opcion {
                            padding: 15px;
                            background: #fce4ef;
                            border-radius: 10px;
                            color: #7a244d;
                        }
                    </style>
                </head>
                <body>
                    <div class="panel">
                        <h1>Peluquería Elegance</h1>
                        <h2>Panel de administración</h2>
                        <p>Acceso de administrador autorizado.</p>
                        <div class="opciones">
                            <a class="opcion" href="AdminUsuariosServlet"
                               style="display:block; text-decoration:none;">
                                👥 Gestión de usuarios
                            </a>
                            <a class="opcion" href="AdminServiciosServlet"
                               style="display:block; text-decoration:none;">
                                ✂️ Gestión de servicios
                            </a>
                            <a class="opcion" href="AdminCitasServlet"
                               style="display:block; text-decoration:none;">
                                📅 Gestión de todas las citas
                            </a>
                        </div>
                    </div>
                </body>
                </html>
                """);
        }
    }
}