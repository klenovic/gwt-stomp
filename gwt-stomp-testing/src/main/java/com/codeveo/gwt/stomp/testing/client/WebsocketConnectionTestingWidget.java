package com.codeveo.gwt.stomp.testing.client;

import com.codeveo.gwt.stomp.client.StompClient;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.*;

import java.util.HashMap;
import java.util.Map;


public class WebSocketConnectionTestingWidget implements TestingWidget {

    interface WebSocketConnectionTestingWidgetUiBinder extends UiBinder<FlowPanel, WebSocketConnectionTestingWidget> {
    }

    private static WebSocketConnectionTestingWidgetUiBinder uiBinder = GWT.create(WebSocketConnectionTestingWidgetUiBinder.class);

    private final static boolean STOMP_DEBUG = true;

    private final FlowPanel rootWidget;

    private StompClient stompClient;

    private TestCallback testCallback;

    @UiField
    protected TextBox wsUrlInput;

    @UiField
    protected Label statusLabel;

    @UiField
    protected Label errorLabel;

    @UiField
    protected Label endpointUrl;

    @UiField
    protected Button connectButton;

    @UiField
    protected Button disconnectButton;

    @UiField
    protected SendPanel sendPanel;

    public WebSocketConnectionTestingWidget() {
        rootWidget = uiBinder.createAndBindUi(this);
        setHtmlIdForSelenium();
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

        sendPanel.setSendCommand(new Command() {
            @Override
            public void execute() {
                sendPanel.setCanSend(false);
                stompClient.send(sendPanel.getDestination(), "");
                sendPanel.setCanSend(true);
            }
        });
    }

    @Override
    public String getTestId() {
        return "websocketConnectionTest";
    }

    @Override
    public void reset() {
        if(testCallback != null) {
            testCallback.enabled = false;
        }

        if(stompClient != null) {
            stompClient.disconnect();
        }

        wsUrlInput.setValue("");
        statusLabel.setText("INIT");
        endpointUrl.setText("");
        errorLabel.setText("");
        sendPanel.reset();
        connectButton.setEnabled(true);
        disconnectButton.setEnabled(false);
        sendPanel.setCanSend(false);

        testCallback = new TestCallback();
        stompClient = new StompClient(null, testCallback, false, STOMP_DEBUG);
    }

    private void setHtmlIdForSelenium() {
        wsUrlInput.getElement().setId("websocket-url-input");
        statusLabel.getElement().setId("connection-status-label");
        errorLabel.getElement().setId("error-cause");
        endpointUrl.getElement().setId("websocket-url");
        connectButton.getElement().setId("connect-button");
        disconnectButton.getElement().setId("disconnect-button");
    }

    @Override
    public Widget asWidget() {
        return rootWidget;
    }

    public class TestCallback implements StompClient.Callback {

        private boolean enabled = true;

        @Override
        public void onConnect() {
            if(!enabled) {
                return;
            }
            statusLabel.setText("CONNECTED");
            connectButton.setEnabled(false);
            disconnectButton.setEnabled(true);
            sendPanel.setCanSend(true);
        }

        @Override
        public void onError(String cause) {
            if(!enabled) {
                return;
            }
            statusLabel.setText("FAILED");
            connectButton.setEnabled(true);
            disconnectButton.setEnabled(false);
            errorLabel.setText(cause);
            sendPanel.setCanSend(false);
        }

        @Override
        public void onDisconnect() {
            if(!enabled) {
                return;
            }
            statusLabel.setText("DISCONNECTED");
            connectButton.setEnabled(true);
            disconnectButton.setEnabled(false);
            sendPanel.setCanSend(false);
        }
    }
}