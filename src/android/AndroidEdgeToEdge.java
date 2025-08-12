package com.example;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;

import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;

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

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    try {
                        // Attempt to use setDecorFitsSystemWindows() for Android 11+
                        Method setDecorFitsMethod = window.getClass()
                                .getMethod("setDecorFitsSystemWindows", boolean.class);
                        setDecorFitsMethod.invoke(window, false);

                        // Attempt to use getInsetsController() for Android 11+
                        Method getInsetsControllerMethod = window.getClass()
                                .getMethod("getInsetsController");
                        Object insetsController = getInsetsControllerMethod.invoke(window);

                        if (insetsController != null) {
                            // Attempt to use setSystemBarsAppearance() for Android 11+
                            Class<?> insetsControllerClass = insetsController.getClass();
                            int appearanceLightStatusBars = 0x1; // Value of APPEARANCE_LIGHT_STATUS_BARS
                            Method setSystemBarsAppearanceMethod = insetsControllerClass
                                    .getMethod("setSystemBarsAppearance", int.class, int.class);
                            setSystemBarsAppearanceMethod.invoke(
                                    insetsController, appearanceLightStatusBars, appearanceLightStatusBars);
                        }
                    } catch (Exception e) {
                        // Reflection failed, fallback to legacy flags
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