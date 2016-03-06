package com.codeveo.gwt.stomp.testing.client;

import com.codeveo.gwt.stomp.client.StompClient;
import com.codeveo.gwt.stomp.client.StompJS;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GwtStompTestingEntryPoint implements EntryPoint {

    private final Map<String, TestingWidget> mapping = new HashMap<>();

    private final TestContainer testContainer = new TestContainer();

    @Override
    public void onModuleLoad() {
        StompJS.install();
        RootPanel.get().add(testContainer);
        initMapping();
        History.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                changeTestingWidget(event.getValue());
            }
        });
        addReadyLabel();
//        quickTest();
    }

    private void initMapping() {
        final List<TestingWidget> testingWidgets = new ArrayList<>();
        testingWidgets.add(new WebSocketConnectionTestingWidget());

        for(TestingWidget testingWidget : testingWidgets) {
            TestingWidget previous = mapping.put(testingWidget.getTestId(), testingWidget);
            if(previous != null) {
                throw new IllegalStateException("Two testing widget cannot have same ID");
            }
        }
    }

    private void addReadyLabel() {
        final Label label = new Label();
        label.setVisible(false);
        label.getElement().setId("gwt-loading-status");
        RootPanel.get().add(label);
    }

    private void changeTestingWidget(String testId) {
        final TestingWidget testingWidget = mapping.get(testId);
        if(testingWidget != null) {
            testContainer.setTestingWidget(testingWidget);
        }
    }

    StompClient stompClient;

    private void quickTest() {
        stompClient = new StompClient("ws://localhost:8080/stomp/", new StompClient.Callback() {
            @Override
            public void onConnect() {
                sub();
            }

            @Override
            public void onError(String cause) {
                RootPanel.get().add(new HTML("onError:" + cause));
            }

            @Override
            public void onDisconnect() {
                RootPanel.get().add(new HTML("onDisconnect"));
            }
        }, false, true);

        final Map<String, String> headers = new HashMap<>();
        headers.put("echoHeaders", "echo echo echo");
        stompClient.connect(headers);

    }

    private void sub() {
//        stompClient.subscribe("/topic/get-connect-headers", new MessageListener() {
//            @Override
//            public void onMessage(Message message) {
//                RootPanel.get().add(new HTML(message.getBody()));
//            }
//        });
//
//        stompClient.send("/app/get-connect-headers", "");

        stompClient.send("/app/close-connection", "");
    }
}
