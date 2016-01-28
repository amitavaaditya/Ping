package com.techno.ping;


import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;


/**
 * A simple {@link Fragment} subclass.
 */
public class LoginFragment extends Fragment {
    //Debugging
    private static final String TAG = "LoginFragment";
    private static final boolean D = true;

    //Login details
    private EditText username;
    private EditText password;
    private CheckBox rememberMe;

    //username
    String uname;
    String pass;

    //WebSocketClient instance
    private WebSocketClient webSocketClient;

    public void setWebSocketClient(WebSocketClient webSocketClient) {
        this.webSocketClient = webSocketClient;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (D) Log.e(TAG, "onCreateView()");
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        if (D) Log.e(TAG, "onViewCreated()");

        //initialise UI elements
        username = (EditText)view.findViewById(R.id.username);
        password = (EditText)view.findViewById(R.id.password);
        rememberMe = (CheckBox)view.findViewById(R.id.rememberMe);

        //connect handler to WebSocketClient
        webSocketClient.setHandler(handler);

        //Load settings
        final SharedPreferences sharedPreferences = getActivity().getPreferences(Context.MODE_PRIVATE);
        uname = sharedPreferences.getString("username", "");
        pass = sharedPreferences.getString("password", "");
        boolean remember = sharedPreferences.getBoolean("rememberMe", false);

        username.setText(uname);
        password.setText(pass);
        rememberMe.setChecked(remember);

        //Attempt to auto-login if saved previously
        if(remember) {
            webSocketClient.sendMessage("connect," + uname + "," + "" + ',' + pass);

        }

        //initialise Button and listener
        Button loginButton = (Button) view.findViewById(R.id.connect_button);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uname = username.getText().toString();
                pass = password.getText().toString();
                Boolean remember = rememberMe.isChecked();

                //if rememberMe is checked then save settings
                if(remember){
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("username",uname);
                    editor.putString("password", pass);
                    editor.putBoolean("rememberMe",true);
                    editor.apply();
                }

                //Attempt to login
                webSocketClient.sendMessage("connect," + uname + "," + "" + ',' + pass);
            }
        });
    }

    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Activity activity = getActivity();
            switch (msg.what) {

                case Constants.MESSAGE_RECEIVE:
                    String receivedMessage = msg.getData().getString(Constants.MESSAGE);
                    if (D) Log.e(TAG, "Message Received: " + receivedMessage);
                    if(receivedMessage.equals("C")) {
                        Log.e(TAG, "Logged in successfully");
                        if (null != activity) {
                            Toast.makeText(activity, "Successfully logged in",
                                    Toast.LENGTH_SHORT).show();
                        }
                        //Initialise MessageFragment
                        MessageFragment messageFragment = new MessageFragment();
                        messageFragment.setWebSocketClient(webSocketClient);
                        messageFragment.setUsername(uname);
                        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                        fragmentTransaction.replace(R.id.frame, messageFragment);
                        fragmentTransaction.commit();
                    } else {
                        Log.e(TAG, "Authentication Failure");
                        if (null != activity) {
                            Toast.makeText(activity, "Authentication Failure",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                    break;

                case Constants.MESSAGE_CONNECTION:
                    if (D) Log.e(TAG, "WebSocket Connection Lost");
                    if (null != activity) {
                        Toast.makeText(activity, "WebSocket Connection Lost. Please Reconnect..",
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
