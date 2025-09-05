package com.example;

import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.Window;
import android.view.WindowInsets;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

public class AndroidEdgeToEdge extends CordovaPlugin {

    @Override
    public boolean execute(String action, org.json.JSONArray args, CallbackContext callbackContext) {
        if ("enable".equals(action)) {
            cordova.getActivity().runOnUiThread(() -> enableEdgeToEdge());
            callbackContext.success();
            return true;
        }
        return false;
    }

    private void enableEdgeToEdge() {
        Window window = cordova.getActivity().getWindow();
        View decorView = window.getDecorView();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // Enable layout under system bars
            decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            );
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

            // Apply padding so content isn't hidden
            decorView.setOnApplyWindowInsetsListener((v, insets) -> {
                View webView = ((ViewGroup) v).getChildAt(0);
                if (webView != null) {
                    if (Build.VERSION.SDK_INT >= 35) {
                        // Android 15 and above: use new API
                        android.graphics.Insets systemBars = insets.getInsets(WindowInsets.Type.systemBars());
                        webView.setPadding(0, systemBars.top, 0, systemBars.bottom);
                        return insets; // do not consume
                    } else {
                        // Android 14 and below: legacy API
                        int topInset = insets.getSystemWindowInsetTop();
                        int bottomInset = insets.getSystemWindowInsetBottom();
                        webView.setPadding(0, topInset, 0, bottomInset);
                        return insets.consumeSystemWindowInsets();
                    }
                }
                return insets;
            });
        }
    }
}