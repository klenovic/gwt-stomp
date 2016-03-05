package com.codeveo.gwt.stomp.testing.client;

import com.google.gwt.user.client.ui.IsWidget;

public interface TestingWidget extends IsWidget {
    String getTestId();

    void reset();
}
