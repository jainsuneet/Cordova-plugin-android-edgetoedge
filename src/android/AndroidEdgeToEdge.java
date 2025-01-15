package com.example;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.json.JSONArray;
import org.json.JSONException;

import android.os.Bundle;
import androidx.core.view.WindowCompat;
import androidx.activity.EdgeToEdge;

import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;

public class AndroidEdgeToEdge extends CordovaPlugin {

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        enableEdgeToEdge();
    }

    private void enableEdgeToEdge() {
        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                EdgeToEdge.enable(cordova.getActivity());
            }
        });
    }
}
