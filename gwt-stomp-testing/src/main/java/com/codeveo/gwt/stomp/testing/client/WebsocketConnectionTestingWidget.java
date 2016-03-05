package com.codeveo.gwt.stomp.testing.client;

import com.codeveo.gwt.stomp.client.StompClient;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.*;

import java.util.HashMap;
import java.util.Map;

public class WebsocketConnectionTestingWidget implements TestingWidget {

    private final static boolean STOMP_DEBUG = true;

    private final FlowPanel rootWidget;

    private StompClient stompClient;

    public WebsocketConnectionTestingWidget() {
        rootWidget = new FlowPanel();
    }

    @Override
    public String getTestId() {
        return "websocketConnectionTest";
    }

    @Override
    public void reset() {
        rootWidget.clear();
        if(stompClient != null) {
            stompClient.disconnect();
        }

        final TextBox wsUrlInput = new TextBox();
        wsUrlInput.getElement().setId("websocket-url-input");

        final Label statusLabel = new Label("INIT");
        statusLabel.getElement().setId("connection-status-label");

        final Label errorLabel = new Label();
        errorLabel.getElement().setId("error-cause");

        final Label endpointUrl = new Label();
        endpointUrl.getElement().setId("websocket-url");

        final Button connectButton = new Button("CONNECT");
        connectButton.getElement().setId("connect-button");

        final Button disconnectButton = new Button("DISCONNECT");
        disconnectButton.getElement().setId("disconnect-button");
        disconnectButton.setEnabled(false);

        stompClient = new StompClient(null, new StompClient.Callback() {
            @Override
            public void onConnect() {
                statusLabel.setText("CONNECTED");
                connectButton.setEnabled(false);
                disconnectButton.setEnabled(true);
            }

            @Override
            public void onError(String cause) {
                statusLabel.setText("FAILED");
                connectButton.setEnabled(true);
                disconnectButton.setEnabled(false);
                errorLabel.setText(cause);
            }

            @Override
            public void onDisconnect() {
                statusLabel.setText("DISCONNECTED");
                connectButton.setEnabled(true);
                disconnectButton.setEnabled(false);
            }
        }, false, STOMP_DEBUG);

        connectButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                stompClient.setWsURL(wsUrlInput.getValue());
                statusLabel.setText("CONNECTING");
                errorLabel.setText("");
                endpointUrl.setText(wsUrlInput.getValue());

                final Map<String, String> headers = new HashMap<>();
                headers.put("test", "toto");
                stompClient.connect(headers);
            }
        });

        disconnectButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                statusLabel.setText("DISCONNECTING");
                stompClient.disconnect();
            }
        });

        rootWidget.add(wsUrlInput);
        rootWidget.add(endpointUrl);
        rootWidget.add(connectButton);
        rootWidget.add(disconnectButton);
        rootWidget.add(statusLabel);
        rootWidget.add(errorLabel);
    }

    @Override
    public Widget asWidget() {
        return rootWidget;
    }
}
