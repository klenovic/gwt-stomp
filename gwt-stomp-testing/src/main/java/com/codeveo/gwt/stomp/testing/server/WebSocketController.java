package com.codeveo.gwt.stomp.testing.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {

    private static final Logger LOG = LoggerFactory.getLogger(WebSocketController.class);

    @Autowired
    private WebSocketSessionHolder socketSessionHolder;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @MessageMapping("/get-connect-headers")
    public void getSubDir(MessageHeaderAccessor messageHeaderAccessor) throws Exception {
        LOG.info("Inscription réalisée.");
        simpMessagingTemplate.convertAndSend("/topic/get-connect-headers", "hello world !");
    }


    /**
     * This request tell the server to close websocket.
     * @param message
     * @throws Exception
     */
    @MessageMapping("/close-connection")
    public void closeConnection(Message<?> message) {
        final String sessionId = WebSocketHeadersUtils.getHttpSessionId(message);
        LOG.info("Client with sessionId {} asked for server-side websocket closing.", sessionId);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2500);
                } catch (InterruptedException e) {
                    // EMPTY
                }
                socketSessionHolder.closeWebSocketSession(sessionId);
            }
        }).start();
    }
}
