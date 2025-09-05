package com.example;

import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

public class AndroidEdgeToEdge extends CordovaPlugin {

    @Override
    public boolean execute(String action, org.json.JSONArray args, CallbackContext callbackContext) {
        if ("enable".equals(action)) {
            cordova.getActivity().runOnUiThread(this::enableEdgeToEdge);
            callbackContext.success();
            return true;
        }
        return false;
    }

    private void enableEdgeToEdge() {
        Window window = cordova.getActivity().getWindow();
        View decorView = window.getDecorView();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // Standard edge-to-edge flags
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            );
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

            // Wrap WebView in FrameLayout for Android 15
            View webView = ((ViewGroup) decorView).getChildAt(0);
            if (webView != null && Build.VERSION.SDK_INT == 33) { // Android 15 preview SDK = 33
                FrameLayout wrapper = new FrameLayout(cordova.getActivity());
                ((ViewGroup) decorView).removeView(webView);
                wrapper.addView(webView);
                ((ViewGroup) decorView).addView(wrapper);

                // Manually add top and bottom padding for status/navigation bar
                int topInset = getStatusBarHeight();
                int bottomInset = getNavigationBarHeight();
                wrapper.setPadding(0, topInset, 0, bottomInset);
            } else if (webView != null) {
                // Fallback for other versions
                decorView.setOnApplyWindowInsetsListener((v, insets) -> {
                    int topInset = insets.getSystemWindowInsetTop();
                    int bottomInset = insets.getSystemWindowInsetBottom();
                    webView.setPadding(0, topInset, 0, bottomInset);
                    return insets.consumeSystemWindowInsets();
                });
            }
        }
    }

    private int getStatusBarHeight() {
        int result = 0;
        int resourceId = cordova.getActivity().getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = cordova.getActivity().getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    private int getNavigationBarHeight() {
        int result = 0;
        int resourceId = cordova.getActivity().getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = cordova.getActivity().getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
}