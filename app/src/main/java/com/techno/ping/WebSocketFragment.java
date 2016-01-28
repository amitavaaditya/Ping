package com.techno.ping;


import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


/**
 * A simple {@link Fragment} subclass.
 */
public class WebSocketFragment extends Fragment {
    //Debugging
    private static final String TAG = "WebSocketFragment";
    private static final boolean D = true;

    //URL details
    private EditText ipAddressEdit;
    private EditText portEdit;

    //WebSocketClient instance
    private WebSocketClient webSocketClient;

    //Connection status
    boolean working = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "onCreate()");
        if(savedInstanceState == null) {
            if (D) Log.e(TAG, "onCreate()");
            //Enable wifi if disabled
            WifiManager wifiManager = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
            if(!wifiManager.isWifiEnabled()) {
                wifiManager.setWifiEnabled(true);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (D) Log.e(TAG, "onCreateView()");
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_web_socket, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        if (D) Log.e(TAG, "onViewCreated()");

        //initialise UI elements
        ipAddressEdit = (EditText)view.findViewById(R.id.ip_address);
        portEdit = (EditText)view.findViewById(R.id.port);

        //Load settings
        final SharedPreferences sharedPreferences = getActivity().getPreferences(Context.MODE_PRIVATE);
        ipAddressEdit.setText(sharedPreferences.getString(getResources().getString(R.string.ip_address), ""));
        portEdit.setText(sharedPreferences.getString(getResources().getString(R.string.port), ""));

        //initialise Button and listener
        Button connectButton = (Button)view.findViewById(R.id.connect_button);
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ipAddress = ipAddressEdit.getText().toString();
                String port = portEdit.getText().toString();

                //Save settings
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(getResources().getString(R.string.ip_address),ipAddress);
                editor.putString(getResources().getString(R.string.port), port);
                editor.apply();

                //Create WebSocketClient instance
                webSocketClient = new WebSocketClient(ipAddress, port);

                //connect handler to WebSocketClient
                webSocketClient.setHandler(handler);

                //Connect
                webSocketClient.connectWebSocket();
                if (D) Log.e(TAG, "Connecting to " + ipAddress + ":" + port);
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (D) Log.e(TAG, "onDestroy()");
        if(!working) {
            Log.e(TAG, "Disconnecting WebSocket");
            if(webSocketClient != null)
                webSocketClient.disconnectWebSocket();
        }
    }

    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Activity activity = getActivity();
            switch (msg.what) {

                case Constants.MESSAGE_CONNECTION:
                    boolean connection = msg.getData().getBoolean(Constants.CONNECTION);
                    if (D) Log.e(TAG, "Connection Status: " + connection);
                    if(connection) {
                        Log.e(TAG, "Connected");
                        if (null != activity) {
                            Toast.makeText(activity, "Connected",
                                    Toast.LENGTH_SHORT).show();
                        }
                        working = true;
                        //Initialise LoginFragment
                        LoginFragment loginFragment = new LoginFragment();
                        loginFragment.setWebSocketClient(webSocketClient);
                        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                        fragmentTransaction.replace(R.id.frame, loginFragment);
                        fragmentTransaction.commit();

                    } else {
                        Log.e(TAG, "Failed To Connect");
                        if (null != activity) {
                            Toast.makeText(activity, "Failed To Connect",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                    break;
            }
        }
    };
}
