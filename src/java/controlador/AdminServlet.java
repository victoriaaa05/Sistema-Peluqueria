
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
                            margin: 0;
                        }

                        .panel {
                            max-width: 1050px;
                            width: 90%;
                            margin: auto;
                            background: white;
                            padding: 35px;
                            border-radius: 16px;
                            box-shadow: 0 4px 18px #0001;
                            box-sizing: border-box;
                        }

                        h1 {
                            color: #b83272;
                            font-size: 38px;
                        }

                        /* IMAGEN Y BOTONES LADO A LADO */

                        .admin-diseno {
                            display: grid;
                            grid-template-columns: 220px minmax(0, 1fr);
                            align-items: center;
                            gap: 30px;
                            margin-top: 35px;
                        }

                        /* IMAGEN MONA LISA */

                        .admin-imagen img {
                            display: block;
                            width: 100%;
                            height: 300px;
                            object-fit: cover;
                            border-radius: 14px;
                        }

                        /* OPCIONES DEL ADMINISTRADOR */

                        .admin-opciones {
                            display: flex;
                            flex-direction: column;
                            gap: 18px;
                        }

                        .opcion {
                            display: flex;
                            align-items: center;
                            justify-content: center;
                            min-height: 80px;
                            padding: 18px;
                            box-sizing: border-box;
                            background: #fce4ef;
                            border-radius: 12px;
                            color: #7a244d;
                            font-size: 20px;
                            font-weight: 600;
                            text-decoration: none;
                            text-align: center;
                            transition: 0.3s ease;
                        }

                        .opcion:hover {
                            background: #f4bdd5;
                            transform: translateY(-3px);
                        }

                        /* DISEÑO PARA CELULARES */

                        @media (max-width: 650px) {
                            .panel {
                                width: 95%;
                                padding: 20px;
                            }

                            .admin-diseno {
                                grid-template-columns: 1fr;
                                gap: 25px;
                            }

                            .admin-imagen {
                                width: 200px;
                                margin: auto;
                            }

                            .admin-imagen img {
                                height: 270px;
                            }

                            .opcion {
                                font-size: 17px;
                            }
                        }
                    </style>
                </head>

                <body>

                    <div class="panel">

                        <h1>Peluquería Elegance</h1>

                        <h2>Panel de administración</h2>

                        <p>Acceso de administrador autorizado.</p>

                        <div class="admin-diseno">

                            <!-- MONA LISA A LA IZQUIERDA -->
                            <div class="admin-imagen">
                                <img src="img/monalisa.jpg"
                                     alt="Mona Lisa disfrutando de un día de spa">
                            </div>

                            <!-- BOTONES A LA DERECHA -->
                            <div class="admin-opciones">

                                <a class="opcion"
                                   href="AdminUsuariosServlet">
                                    👥 Gestión de usuarios
                                </a>

                                <a class="opcion"
                                   href="AdminServiciosServlet">
                                    ✂️ Gestión de servicios
                                </a>

                                <a class="opcion"
                                   href="AdminCitasServlet">
                                    📅 Gestión de todas las citas
                                </a>

                            </div>

                        </div>

                    </div>

                </body>
                </html>
                """);
        }
    }
}
