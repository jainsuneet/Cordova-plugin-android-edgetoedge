package com.example;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;

import android.os.Build;
import android.view.View;
import android.view.Window;

import java.lang.reflect.Method;

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
                Window window = cordova.getActivity().getWindow();

                if (Build.VERSION.SDK_INT >= 30) {
                    try {
                        Method m = window.getClass().getMethod("setDecorFitsSystemWindows", boolean.class);
                        m.invoke(window, false);
                    } catch (Exception e) {
                        // fallback if method not found or invocation fails
                        setLegacyFlags(window);
                    }
                } else {
                    setLegacyFlags(window);
                }
            }

            private void setLegacyFlags(Window window) {
                View decorView = window.getDecorView();
                decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                );
            }
        });
    }
}