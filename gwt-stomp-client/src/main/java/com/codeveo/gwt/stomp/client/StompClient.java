package com.codeveo.gwt.stomp.client;

import com.google.gwt.core.client.JavaScriptObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StompClient {
    private static final Logger LOGGER = Logger.getLogger(StompClient.class.getName());
    private final boolean useSockJs;
    private String wsURL;
    private Callback callback;
    private JavaScriptObject jsoStompClient;
    private boolean isConnected = false;
    private Map<String, Subscription> subscriptions;
    private boolean enableDebug;

    public interface Callback {
        /**
         * Called when connection is opened and ready to send, subscribe and receive messages.<br/>
         * Only called right after connect() is.<br/>
         * Then, isConnected() will return true.
         * @see StompClient#connect()
         */
        void onConnect();

        /**
         * Called when established connection is lost (due to server side or client side) or cannot be established<br/>
         * Reasons could be : <br/>
         * - stomp.js or sock.js is not loaded. It's developer responsability to load it.<br/>
         * - wrong url sheme. If you are only using websocket and you don't provide url like ws:// or wss://<br/>
         * - connection lost on client side for any reason<br/>
         * - connection closed by server<br/>
         * Only called right after connect() or some times after onConnect().<br/>
         * Then, isConnected() will return false.
         * @see StompClient#connect()
         * @param cause Error cause.
         */
        void onError(String cause);

        /**
         * Called when clean disconnection is done.<br/>
         * Only called after disconnect().<br/>
         * Then, isConnected() will return false
         * @see StompClient#disconnect()
         */
        void onDisconnect();
    }

    public StompClient(String wsURL, Callback callback, boolean useSockJs, boolean enableDebug) {
        this.useSockJs = useSockJs;
        this.wsURL = wsURL;
        this.callback = callback;
        this.subscriptions = new HashMap<>();
        this.enableDebug = enableDebug;
    }

    public void setWsURL(String wsURL) {
        this.wsURL = wsURL;
    }

    public final void connect() {
        connect(new HashMap<String, String>());
    }

    public final void connect(Map<String, String> headers) {
        if (isConnected) {
            LOGGER.log(Level.FINE, "Already connected");
            return;
        }

        JavaScriptObject jsHeaders = JavaScriptObject.createObject();
        if(headers != null) {
            for (Entry<String, String> header : headers.entrySet()) {
                putString(jsHeaders, header.getKey(), header.getValue());
            }
        }

        LOGGER.log(Level.FINE, "Connecting to '" + wsURL + "' ...");
        __connect(wsURL, useSockJs, enableDebug, jsHeaders);
    }
    private native JavaScriptObject putString(JavaScriptObject jso, String key, String value)/*-{
        jso[key] = value;
        return jso;
    }-*/;

    public final void disconnect() {
        if(!isConnected) {
            LOGGER.log(Level.FINE, "Not connected");
            return;
        }

        for (Entry<String, Subscription> id : subscriptions.entrySet()) {
            unsubscribe(id.getKey());
        }

        LOGGER.log(Level.FINE, "Disconecting from '" + wsURL + "' ...");
        __disconnect();
    }

    public final Subscription subscribe(String destination, MessageListener listener) {
        LOGGER.log(Level.FINE, "Subscribing to destination '" + destination + "' ...");
        Subscription subscription = __subscribe(destination, listener);

        LOGGER.log(Level.FINE, "Subscribed to destination '" + destination + "' with ID '" + subscription.getId() + "'");
        subscriptions.put(destination, subscription);

        return subscription;
    }

    public final void unsubscribe(String destination) {
        Subscription subscription = subscriptions.get(destination);

        if (subscription != null) {
            LOGGER.log(Level.FINE, "Unsubscribing from destination '" + destination + "' ...");
            __unsubscribe(subscription);

            LOGGER.log(Level.FINE, "Unsubscribed from destination '" + destination + "'");
            subscriptions.remove(destination);
        }
    }

    public native final void send(String destination, String jsonString)
    /*-{
        var self = this;
        self.@com.codeveo.gwt.stomp.client.StompClient::jsoStompClient.send(destination, {}, jsonString);
    }-*/;

    private native void __connect(String wsURL, boolean overSockJs, boolean enableDebug, JavaScriptObject headers)
    /*-{
        var self = this;

    	var onConnected = function () {
            self.@com.codeveo.gwt.stomp.client.StompClient::onConnected()();
        };

    	var onError = function (cause) {
    		self.@com.codeveo.gwt.stomp.client.StompClient::onError(Ljava/lang/String;)(cause);
    	};

        var getErrorMsg = function (err, defaultMessage) {
            var msg = err.message;
            if(typeof(msg) === "string") {
                return msg;
            } else {
                return defaultMessage;
            }
        };

        if($wnd.Stomp === undefined || $wnd.Stomp === null) {
            onError("It seems that you did not included stomp.js.");
            return;
        }

        // TODO check sockjs import.

        if (overSockJs === true) {
            try {
                var socket = new $wnd.SockJS(wsURL);
                self.@com.codeveo.gwt.stomp.client.StompClient::jsoStompClient = $wnd.Stomp.over(socket);
            } catch (err) {
                onError(getErrorMsg(err, "Could not instanciate stomp client. (check if url is correct)"));
                return;
            }
        } else {
            try {
                self.@com.codeveo.gwt.stomp.client.StompClient::jsoStompClient = $wnd.Stomp.client(wsURL);
            } catch (err) {
                onError(getErrorMsg(err, "Could not instanciate stomp client. (check if url is correct)"));
                return;
            }
        }

        if (self.@com.codeveo.gwt.stomp.client.StompClient::jsoStompClient != null && !enableDebug) {
            self.@com.codeveo.gwt.stomp.client.StompClient::jsoStompClient.debug = null;
        }

        self.@com.codeveo.gwt.stomp.client.StompClient::jsoStompClient.connect(headers, onConnected, onError);
    }-*/;

    private native void __disconnect()
    /*-{
    	var self = this;

    	var ondisconnect = function(){
    		self.@com.codeveo.gwt.stomp.client.StompClient::onDisconnect()();
    	};

    	self.@com.codeveo.gwt.stomp.client.StompClient::jsoStompClient.disconnect(ondisconnect);
    }-*/;

    private native Subscription __subscribe(String destination, MessageListener listener)
    /*-{
        var self = this;

    	var onMessage = function (message) {
    		listener.@com.codeveo.gwt.stomp.client.MessageListener::onMessage(Lcom/codeveo/gwt/stomp/client/Message;)(message);
    	};

    	var subscription = self.@com.codeveo.gwt.stomp.client.StompClient::jsoStompClient.subscribe(destination, onMessage);

     	return subscription;
    }-*/;

    private native void __unsubscribe(Subscription subscription)
    /*-{
        subscription.unsubscribe();
    }-*/;

    /* Need to wrap the callbacks */
    private void onConnected() {
        isConnected = true;
        if (callback != null) {
            callback.onConnect();
        }
    }

    private void onDisconnect() {
        isConnected = false;
        if (callback != null) {
            callback.onDisconnect();
        }
    }

    private void onError(String cause) {
        isConnected = false;
        if (callback != null) {
            callback.onError(cause);
        }
    }

    public boolean isConnected() {
        return isConnected;
    }
}
