package com.randaegarcia.security;

import io.quarkus.arc.Arc;
import io.quarkus.vertx.http.runtime.CurrentVertxRequest;
import io.vertx.ext.web.RoutingContext;
import org.hibernate.envers.RevisionListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Base64;

public class CustomRevisionListener implements RevisionListener {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void newRevision(Object revisionEntity) {
        CustomRevisionEntity revision = (CustomRevisionEntity) revisionEntity;

        try {
            CurrentVertxRequest currentRequest = Arc.container()
                    .instance(CurrentVertxRequest.class).get();

            RoutingContext routingContext = currentRequest.getCurrent();

            if (routingContext != null) {
                String authHeader = routingContext.request().getHeader("Authorization");

                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    String username = extractUsernameFromJwt(authHeader.substring(7));
                    revision.setUsername(username);
                } else {
                    // No hay token de autenticación - operación del sistema
                    revision.setUsername("SYSTEM");
                }
            } else {
                // No hay contexto HTTP - operación interna o batch
                revision.setUsername("SYSTEM");
            }

        } catch (Exception e) {
            revision.setUsername("ERROR");
        }
    }

    /**
     * Este metodo extrae únicamente el username del JWT token
     */
    private String extractUsernameFromJwt(String jwt) {
        try {
            String[] jwtParts = jwt.split("\\.");
            if (jwtParts.length != 3) {
                return "INVALID_TOKEN";
            }

            String payload = new String(Base64.getUrlDecoder().decode(jwtParts[1]));
            JsonNode claims = objectMapper.readTree(payload);

            String username = getClaimValue(claims, "preferred_username");

            // Si no tenemos preferred_username, intentamos con otros campos comunes
            if (isEmpty(username)) {
                username = getClaimValue(claims, "username");
            }

            // Si aun no tenemos username, usamos el email como fallback
            if (isEmpty(username)) {
                username = getClaimValue(claims, "email");
            }

            // Si todavia no tenemos nada, usamos el subject
            if (isEmpty(username)) {
                username = getClaimValue(claims, "sub");
            }

            return !isEmpty(username) ? username : "UNKNOWN_USER";

        } catch (Exception e) {
            return "JWT_PARSE_ERROR";
        }
    }

    /**
     * Metodo auxiliar para extraer un claim del JSON de manera segura
     * Evita NullPointerExceptions y maneja casos edge elegantemente
     */
    private String getClaimValue(JsonNode claims, String claimName) {
        JsonNode claimNode = claims.get(claimName);
        return claimNode != null && !claimNode.isNull() ? claimNode.asText() : null;
    }

    /**
     * Metodo auxiliar para verificar si un string está vacío o null
     */
    private boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
}
