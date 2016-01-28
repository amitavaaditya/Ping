package com.techno.ping;

/**
 * Created by techno on 1/8/15.
 * List of constants used
 */
public interface Constants {
    //Message Types sent from the MessageService handler
    int MESSAGE_CONNECTION  = 1;
    int MESSAGE_RECEIVE = 2;
    int MESSAGE_TOAST = 3;

    // Key names received from the MessageService Handler
    String CONNECTION = "connection";
    String MESSAGE = "message";
}
