package com.example;

import org.apache.cordova.CordovaPlugin;
import android.view.View;
import android.os.Build;

public class AndroidEdgeToEdge extends CordovaPlugin {

    @Override
    protected void pluginInitialize() {
        enableEdgeToEdge();
    }

    private void enableEdgeToEdge() {
        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    cordova.getActivity().getWindow().setDecorFitsSystemWindows(false);
                } else {
                    int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
                    cordova.getActivity().getWindow().getDecorView().setSystemUiVisibility(flags);
                }
            }
        });
    }
}
