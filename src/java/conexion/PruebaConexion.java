package conexion;

import java.sql.Connection;

public class PruebaConexion {

    public static void main(String[] args) {

        Connection conexion = Conexion.getConexion();

        if (conexion != null) {
            System.out.println("¡La conexión funciona correctamente!");
        } else {
            System.out.println("No se pudo conectar a PostgreSQL.");
        }
    }
}