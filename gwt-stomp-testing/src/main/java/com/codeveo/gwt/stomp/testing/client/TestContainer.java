package com.codeveo.gwt.stomp.testing.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;

public class TestContainer extends Composite {

    private final Label testIdLabel = new Label();

    private TestingWidget testingWidget;

    private final FlowPanel testContainerPanel = new FlowPanel();

    public TestContainer() {
        FlowPanel rootWidget = new FlowPanel();
        initWidget(rootWidget);

        testIdLabel.getElement().setId("test-id");

        Button resetButton = new Button("Reset Test");
        resetButton.getElement().setId("test-reset-button");
        rootWidget.add(testIdLabel);
        rootWidget.add(resetButton);
        rootWidget.add(testContainerPanel);

        resetButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                onResetTest();
            }
        });
    }

    private void onResetTest() {
        if(testingWidget == null) {
            return;
        }

        testingWidget.reset();
        testContainerPanel.clear();
        testContainerPanel.add(testingWidget);
    }

    public void setTestingWidget(TestingWidget testingWidget) {
        this.testingWidget = testingWidget;
        testingWidget.reset();
        testIdLabel.setText(testingWidget.getTestId());
    }
}
