package com.example;

import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import java.lang.reflect.Method;
import java.lang.reflect.Field;

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
        final Window window = cordova.getActivity().getWindow();
        final View decorView = window.getDecorView();

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) return;

        // Basic flags for edge-to-edge layout
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        );
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        final View contentRoot = decorView.findViewById(android.R.id.content);
        if (contentRoot == null) return;

        final View target = (contentRoot instanceof ViewGroup && ((ViewGroup) contentRoot).getChildCount() > 0)
                ? ((ViewGroup) contentRoot).getChildAt(0)
                : contentRoot;

        target.setFitsSystemWindows(false);

        contentRoot.setOnApplyWindowInsetsListener((v, insets) -> {
            try {
                if (Build.VERSION.SDK_INT >= 30) {
                    // Runtime reflection for Android 30+ / Android 15
                    try {
                        Method decorMethod = Window.class.getMethod("setDecorFitsSystemWindows", boolean.class);
                        decorMethod.invoke(window, false);
                    } catch (Exception ignored) {}

                    Class<?> typeClass = Class.forName("android.view.WindowInsets$Type");
                    Method systemBarsMethod = typeClass.getMethod("systemBars");
                    int mask = (int) systemBarsMethod.invoke(null);

                    Method getInsetsMethod = WindowInsets.class.getMethod("getInsets", int.class);
                    Object insetsObj = getInsetsMethod.invoke(insets, mask);

                    Field topF = insetsObj.getClass().getField("top");
                    Field bottomF = insetsObj.getClass().getField("bottom");

                    int top = (int) topF.get(insetsObj);
                    int bottom = (int) bottomF.get(insetsObj);

                    target.setPadding(0, top, 0, bottom);
                    return insets; // do not consume
                }
            } catch (Throwable ignored) {}

            // Legacy fallback for API < 30
            target.setPadding(0, insets.getSystemWindowInsetTop(), 0, insets.getSystemWindowInsetBottom());
            return insets.consumeSystemWindowInsets();
        });

        contentRoot.requestApplyInsets();
    }
}