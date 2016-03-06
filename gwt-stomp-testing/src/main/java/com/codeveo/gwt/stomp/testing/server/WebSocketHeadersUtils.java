package com.codeveo.gwt.stomp.testing.server;

import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import java.util.HashMap;
import java.util.Map;

public class WebSocketHeadersUtils {

    /**
     * Retourne les attributs de la sessions simp.<br/>
     * IMPORTANT : Il ne s'agit pas des attributs de la session http.
     * @param message
     * @return
     */
    public static Map<String, Object> getSimpSessionAttributes(Message<?> message) {
        if(message == null || message.getHeaders() == null) {
            return new HashMap<>();
        }

        Map<String, Object> map = SimpMessageHeaderAccessor.getSessionAttributes(message.getHeaders());
        return map == null ? new HashMap<String, Object>() : map;
    }

    /**
     * Retourne l'id de la session http. Ceci ne peut fonctionner que si l'id de la session http a été copié
     * dans les attributs de la session simp par un interceptor.
     * @see HttpSessionHandshakeInterceptor
     * @param message
     * @return
     */
    public static String getHttpSessionId(Message<?> message) {
        final Object httpSessionId = getSimpSessionAttributes(message).get(HttpSessionHandshakeInterceptor.HTTP_SESSION_ID_ATTR_NAME);
        return httpSessionId == null ? null : httpSessionId.toString();
    }
}
