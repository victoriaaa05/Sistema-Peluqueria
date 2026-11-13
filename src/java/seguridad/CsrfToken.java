package seguridad;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.security.SecureRandom;
import java.util.Base64;

public final class CsrfToken {

    private static final SecureRandom RANDOM =
            new SecureRandom();

    private static final String CLAVE_SESION =
            "csrf_token";

    private CsrfToken() {
    }

    public static String obtener(HttpSession sesion) {
        synchronized (sesion) {
            String token = (String)
                    sesion.getAttribute(CLAVE_SESION);

            if (token == null) {
                byte[] bytes = new byte[32];
                RANDOM.nextBytes(bytes);

                token = Base64.getUrlEncoder()
                        .withoutPadding()
                        .encodeToString(bytes);

                sesion.setAttribute(CLAVE_SESION, token);
            }

            return token;
        }
    }

    public static boolean validar(
            HttpServletRequest request) {

        HttpSession sesion =
                request.getSession(false);

        if (sesion == null) {
            return false;
        }

        String esperado = (String)
                sesion.getAttribute(CLAVE_SESION);

        String recibido =
                request.getParameter("csrf_token");

        if (esperado == null || recibido == null) {
            return false;
        }

        return java.security.MessageDigest.isEqual(
                esperado.getBytes(
                    java.nio.charset.StandardCharsets.UTF_8),
                recibido.getBytes(
                    java.nio.charset.StandardCharsets.UTF_8)
        );
    }
}