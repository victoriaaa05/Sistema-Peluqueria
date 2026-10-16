
package conexion;

import org.mindrot.jbcrypt.BCrypt;
import java.io.Console;

public class GenerarHash {

    public static void main(String[] args) {

        Console consola = System.console();

        if (consola == null) {
            System.out.println(
                "Ejecuta esta clase desde una terminal."
            );
            return;
        }

        char[] clave = consola.readPassword(
                "Escribe tu nueva contraseña: "
        );

        String hash = BCrypt.hashpw(
                new String(clave),
                BCrypt.gensalt(12)
        );

        java.util.Arrays.fill(clave, '\0');

        System.out.println("HASH GENERADO:");
        System.out.println(hash);
    }
}
