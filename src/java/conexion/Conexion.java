package conexion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexion {

    private static final String URL =
       "jdbc:postgresql://ep-dawn-rice-b4vg56kk-pooler.c-6.us-east-2.aws.neon.tech/neondb?sslmode=require&channelBinding=require";

    private static final String USUARIO = "neondb_owner";

    private static final String CONTRASENA =
        System.getenv("PELUQUERIA_DB_PASSWORD");

    public static Connection getConexion() {

        Connection conexion = null;

        try {

            Class.forName("org.postgresql.Driver");

            conexion = DriverManager.getConnection(
                    URL,
                    USUARIO,
                    CONTRASENA
            );

            System.out.println("Conexión exitosa con PostgreSQL");

        } catch (ClassNotFoundException | SQLException e) {

            System.out.println("Error de conexión: " + e.getMessage());

        }

        return conexion;
    }
}