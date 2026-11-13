package conexion;

import java.sql.Connection;
import java.sql.SQLException;

public class PruebaConexion {

    public static void main(String[] args) {

        String password = System.getenv("PELUQUERIA_DB_PASSWORD");

        System.out.println(
            "¿Contraseña configurada?: " + (password != null)
        );

        try (Connection conexion = Conexion.getConexion()) {

            if (conexion != null) {
                System.out.println(
                    "¡La conexión funciona correctamente!"
                );
            } else {
                System.out.println(
                    "No se pudo conectar a PostgreSQL."
                );
            }

        } catch (SQLException e) {
            System.out.println(
                "Error al cerrar o utilizar la conexión."
            );
        }
    }
}