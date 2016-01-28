package com.techno.ping;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;


public class MainActivity extends Activity {
    //Debugging
    private static final String TAG = "MainActivity";
    private static final boolean D = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (D) Log.e(TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Instantiate WebSocketFragment
        if (savedInstanceState == null) {
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            WebSocketFragment fragment = new WebSocketFragment();
            transaction.replace(R.id.frame, fragment);
            transaction.commit();
        }
    }
}
