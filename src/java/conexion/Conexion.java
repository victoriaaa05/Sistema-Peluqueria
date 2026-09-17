package conexion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexion {

    private static final String URL =
            "jdbc:postgresql://localhost:5432/peluqueria_db";

    private static final String USUARIO = "postgres";

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