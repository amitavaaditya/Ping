package com.techno.ping;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketException;
import de.tavendo.autobahn.WebSocketHandler;

/**
 * Created by techno on 1/8/15.
 * WebSocket Client file for connection with the server.
 */
public class WebSocketClient {
    //Debugging
    private static final String TAG = "WebSocketClient";
    private static final boolean D = true;

    //URL details
    private final String ipAddress;
    private final String port;

    //Handler to transmit messages with UI
    private Handler handler;

    //WebSocket Connection
    private final WebSocketConnection connection = new WebSocketConnection();

    //Constructor
    public WebSocketClient(String ipAddress, String port) {
        this.ipAddress = ipAddress;
        this.port = port;
    }

    //Connect WebSocket
    public void connectWebSocket() {
        if (D) Log.e(TAG, "connectWebSocket()");
        start();
    }

    public boolean isConnected() {
        if (D) Log.e(TAG, "connectionStatus()");
        return connection.isConnected();
    }

    //Set Handler to interact with UI
    public void setHandler(Handler handler) { this.handler = handler; }

    //Send Message
    public void sendMessage(String data) {
        if (D) Log.e(TAG, "sendMessage():" + data);
        connection.sendTextMessage(data);
    }

    //Disconnect Message
    public void disconnectWebSocket() {
        if (D) Log.e(TAG, "disconnectWebSocket()");
        connection.disconnect();
    }

    private void start() {
        if (D) Log.e(TAG, "start()");

        final String uri = "ws://" + ipAddress + ":" + port;

        try {
            connection.connect(uri, new WebSocketHandler() {

                @Override
                public void onOpen() {
                    if (D) Log.e(TAG, "Status: Connected to " + uri);
                    Message msg = handler.obtainMessage(Constants.MESSAGE_CONNECTION);
                    Bundle bundle = new Bundle();
                    bundle.putBoolean(Constants.CONNECTION, true);
                    msg.setData(bundle);
                    handler.sendMessage(msg);
                }

                @Override
                public void onTextMessage(String payload) {
                    Log.e(TAG, "onTextMessage():" + payload);
                    Bundle bundle = new Bundle();
                    bundle.putString(Constants.MESSAGE, payload);
                    Message message = handler.obtainMessage(Constants.MESSAGE_RECEIVE);
                    message.setData(bundle);
                    handler.sendMessage(message);
                }

                @Override
                public void onClose(int code, String reason) {
                    Log.e(TAG, "Connection lost." + reason);
                    // Send a failure message back to the Activity
                    Message msg = handler.obtainMessage(Constants.MESSAGE_CONNECTION);
                    Bundle bundle = new Bundle();
                    bundle.putBoolean(Constants.CONNECTION, false);
                    msg.setData(bundle);
                    handler.sendMessage(msg);
                }
            });
        } catch (WebSocketException e) {
            Log.e(TAG, e.toString());
        }
    }
}
