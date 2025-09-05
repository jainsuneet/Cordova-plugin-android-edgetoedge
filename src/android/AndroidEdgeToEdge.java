package com.example;

import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.Window;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

public class AndroidEdgeToEdge extends CordovaPlugin {

    private int topInset = 0;
    private int bottomInset = 0;

    @Override
    public boolean execute(String action, org.json.JSONArray args, CallbackContext callbackContext) {
        if ("enable".equals(action)) {
            cordova.getActivity().runOnUiThread(this::enableEdgeToEdge);
            callbackContext.success();
            return true;
        } else if ("getInsets".equals(action)) {
            try {
                org.json.JSONObject result = new org.json.JSONObject();
                result.put("top", topInset);
                result.put("bottom", bottomInset);
                callbackContext.success(result);
            } catch (Exception e) {
                callbackContext.error(e.getMessage());
            }
            return true;
        }
        return false;
    }

    private void enableEdgeToEdge() {
        Window window = cordova.getActivity().getWindow();
        View decorView = window.getDecorView();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            );
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

            decorView.setOnApplyWindowInsetsListener((v, insets) -> {
                topInset = insets.getSystemWindowInsetTop();
                bottomInset = insets.getSystemWindowInsetBottom();

                View webView = ((ViewGroup) v).getChildAt(0);
                if (webView != null) {
                    webView.setPadding(0, topInset, 0, bottomInset);
                }
                return insets.consumeSystemWindowInsets();
            });

            decorView.requestApplyInsets();
        }
    }
}