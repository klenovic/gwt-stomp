package com.codeveo.gwt.stomp.testing.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.*;


public class SendPanel implements IsWidget {
    interface SendPanelUiBinder extends UiBinder<FlowPanel, SendPanel> {
    }

    private static SendPanelUiBinder uiBinder = GWT.create(SendPanelUiBinder.class);

    @UiField
    protected Button sendButton;

    @UiField
    protected Button resetButton;

    @UiField
    protected TextBox destination;

    private FlowPanel rootPanel;

    private Command sendCommand;

    public SendPanel() {
        rootPanel = uiBinder.createAndBindUi(this);

        destination.getElement().setId("destination-input");
        sendButton.getElement().setId("send-button");
        resetButton.getElement().setId("reset-send-button");

        sendButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (sendCommand != null) {
                    sendCommand.execute();
                }
            }
        });

        resetButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                reset();
            }
        });
    }

    public void reset() {
        destination.setValue("");
    }

    public void setCanSend(boolean canSend) {
        destination.setEnabled(canSend);
        sendButton.setEnabled(canSend);
    }

    public void setSendCommand(Command command) {
        sendCommand = command;
    }

    public String getDestination() {
        return destination.getValue();
    }

    @Override
    public Widget asWidget() {
        return rootPanel;
    }
}