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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;


/**
 * A simple {@link Fragment} subclass.
 */
public class UsersFragment extends Fragment {
    //Debugging
    private static final String TAG = "UsersFragment";
    private static boolean D = true;

    // Layout Views
    private ListView usersView;

    //Username
    private String username;

    //Array adapter for the conversation thread
    private ArrayAdapter<String> usersArrayAdapter;

    //WebSocket Connection
    private WebSocketClient webSocketClient;

    public void setWebSocketClient(WebSocketClient webSocketClient) {
        this.webSocketClient = webSocketClient;
    }

    public void setUsername(String username) {
        this.username = username;
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
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_users, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        if (D) Log.e(TAG, "onViewCreated()");
        usersView = (ListView) view.findViewById(R.id.online_users);
        // Initialize the array adapter for the conversation thread
        usersArrayAdapter = new ArrayAdapter<>(getActivity(), R.layout.message);
        usersView.setAdapter(usersArrayAdapter);

        updateView();

        usersView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String recipient = usersView.getItemAtPosition(position).toString();
                Log.e(TAG,"onItemClick():" + recipient);

                //Initialise MessageFragment
                MessageFragment messageFragment = new MessageFragment();
                messageFragment.setWebSocketClient(webSocketClient);
                messageFragment.setUsers(username,recipient);
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.frame, messageFragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        });
    }

    private void updateView() {
        if (D) Log.e(TAG, "updateView()");
        webSocketClient.sendMessage("request" + ',' + username + ",,");
    }

    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Activity activity = getActivity();
            switch (msg.what) {
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
                case Constants.MESSAGE_RECEIVE:
                    String receivedMessage = msg.getData().getString(Constants.MESSAGE);
                    if (D) Log.e(TAG, "User: " + receivedMessage);
                    usersArrayAdapter.add(receivedMessage);
                    break;
            }
        }
    };
}
