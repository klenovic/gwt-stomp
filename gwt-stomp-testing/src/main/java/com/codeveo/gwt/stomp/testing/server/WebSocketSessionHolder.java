package com.codeveo.gwt.stomp.testing.server;

import com.google.gwt.thirdparty.guava.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class WebSocketSessionHolder {

    private final Map<String, WebSocketSession> socketSessions = new HashMap<>();

    private final static Logger LOG = LoggerFactory.getLogger(WebSocketSessionHolder.class);

    public void saveSession(WebSocketSession socketSession) {

        LOG.trace("Incoming WebSocketSession");
        String sessionId = (String)socketSession.getAttributes().get(HttpSessionHandshakeInterceptor.HTTP_SESSION_ID_ATTR_NAME);
        if(Strings.isNullOrEmpty(sessionId)) {
            try {
                LOG.warn("Closing websocket without session ID");
                socketSession.close(CloseStatus.POLICY_VIOLATION);
            } catch (IOException e) {
                LOG.warn("Impossible de fermer la websocket", e);
            }
            return;
        }

        LOG.info("Saving WebSocketSession reference for sessionId {}", sessionId);
        WebSocketSession previousSession = socketSessions.put(sessionId, socketSession);
        if(previousSession != null && previousSession.isOpen()) {
            try {
                previousSession.close(CloseStatus.POLICY_VIOLATION);
            } catch (IOException e) {
                LOG.warn("Impossible de fermer la websocket", e);
            }
        }
    }

    public void closeWebSocketSession(String sessionId) {
        closeWebSocketSession(sessionId, CloseStatus.NORMAL);
    }

    public void closeWebSocketSession(String sessionId, CloseStatus closeStatus) {
        if(sessionId == null) {
            return;
        }

        WebSocketSession socketSession = socketSessions.remove(sessionId);
        if(socketSession == null || !socketSession.isOpen()) {
            return;
        }

        try {
            socketSession.close(closeStatus);
        } catch (IOException e) {
            LOG.warn("Impossible de fermer la websocket", e);
        }
    }
}
