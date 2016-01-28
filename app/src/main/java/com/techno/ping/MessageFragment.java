package com.techno.ping;


import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


/**
 * A simple {@link Fragment} subclass.
 */
public class MessageFragment extends Fragment {
    //Debugging
    private static final String TAG = "MessageFragment";
    private static boolean D = true;

    // Layout Views
    private ListView conversationView;
    private Button sendButton;

    //WebSocket Connection
    private WebSocketClient webSocketClient;

    //Username
    private String username;

    //Active user in conversation
    private String recipient;

    //Array adapter for the conversation thread
    private ArrayAdapter<String> conversationArrayAdapter;

    public void setUsername(String username) {
        this.username = username;
    }

    public void setUsers(String username, String recipient) {
        this.username = username;
        this.recipient = recipient;
    }

    public void setWebSocketClient(WebSocketClient webSocketClient) {
        this.webSocketClient = webSocketClient;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (D) Log.e(TAG, "onCreate()");
        webSocketClient.setHandler(handler);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (D) Log.e(TAG, "onCreateView()");
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_message, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        if (D) Log.e(TAG, "onViewCreated()");
        conversationView = (ListView) view.findViewById(R.id.in);
        sendButton = (Button) view.findViewById(R.id.button_send);
        setupChat();
    }

    private void setupChat() {
        Log.d(TAG, "setupChat()");

        // Initialize the array adapter for the conversation thread
        conversationArrayAdapter = new ArrayAdapter<>(getActivity(), R.layout.message);

        conversationView.setAdapter(conversationArrayAdapter);

        // Initialize the send button with a listener that for click events
        sendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
                View view = getView();
                if (null != view) {
                    TextView textView = (TextView) view.findViewById(R.id.edit_text_out);
                    String message = textView.getText().toString();
                    sendMessage(message);
                }
            }
        });
    }

    private void sendMessage(String message) {
        Log.d(TAG, "sendMessage():" + message);
        if(message.length() > 0) {
            if (webSocketClient.isConnected()) {
                webSocketClient.sendMessage("message" + ',' + username + ',' + recipient + ',' + message);
                conversationArrayAdapter.add("Me:" + message);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (D) Log.e(TAG, "onDestroy()");
        if(webSocketClient.isConnected())
            webSocketClient.disconnectWebSocket();
        webSocketClient = null;
    }

    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Activity activity = getActivity();
            switch (msg.what) {
                case Constants.MESSAGE_RECEIVE:
                    String receivedMessage = msg.getData().getString(Constants.MESSAGE);
                    if (D) Log.e(TAG, "Message Received: " + receivedMessage);
                    conversationArrayAdapter.add(receivedMessage);

                    break;
                case Constants.MESSAGE_CONNECTION:
                    if (D) Log.e(TAG, "WebSocket Connection Lost");
                    if (null != activity) {
                        Toast.makeText(activity,"WebSocket Connection Lost. Please Reconnect..",
                                Toast.LENGTH_SHORT).show();

                        //Re-Initialise WebSocketFragment
                        WebSocketFragment webSocketFragment = new WebSocketFragment();
                        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                        fragmentTransaction.replace(R.id.frame, webSocketFragment);
                        fragmentTransaction.commit();
                    }
                    break;
            }
        }
    };
}
