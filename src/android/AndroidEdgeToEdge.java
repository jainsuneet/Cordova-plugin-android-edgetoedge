package com.example;

import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // Keep your existing flags (unchanged for ≤ Android 14)
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            );
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

            if (Build.VERSION.SDK_INT >= 30) {
                // --- Android 11+ path (works on Android 15 while compiling with SDK 29) ---
                // 1) Tell the system we will handle insets (reflection to avoid new API at compile time)
                try {
                    Method setDecorFits = Window.class.getMethod("setDecorFitsSystemWindows", boolean.class);
                    setDecorFits.invoke(window, false);
                } catch (Throwable ignored) { }

                // 2) Work on the content root (FrameLayout that holds Cordova WebView)
                final View contentRoot = decorView.findViewById(android.R.id.content);
                final ViewGroup contentGroup = (contentRoot instanceof ViewGroup) ? (ViewGroup) contentRoot : null;

                // The view we actually pad: prefer the first child (Cordova WebView container), else the content root.
                final View target = (contentGroup != null && contentGroup.getChildCount() > 0)
                        ? contentGroup.getChildAt(0)
                        : contentRoot;

                if (contentRoot != null && target != null) {
                    // Ensure layout can extend under bars; we add padding back via insets.
                    target.setFitsSystemWindows(false);

                    contentRoot.setOnApplyWindowInsetsListener((v, insets) -> {
                        try {
                            // Reflect: int mask = WindowInsets.Type.systemBars();
                            final Class<?> typeClass = Class.forName("android.view.WindowInsets$Type");
                            final Method systemBarsMethod = typeClass.getMethod("systemBars");
                            final int systemBarsMask = (int) systemBarsMethod.invoke(null);

                            // Reflect: Insets sb = insets.getInsets(mask);
                            final Method getInsetsMethod = WindowInsets.class.getMethod("getInsets", int.class);
                            final Object insetsObj = getInsetsMethod.invoke(insets, systemBarsMask);

                            // Read fields from android.graphics.Insets without referencing the class at compile time
                            final Field leftF = insetsObj.getClass().getField("left");
                            final Field topF = insetsObj.getClass().getField("top");
                            final Field rightF = insetsObj.getClass().getField("right");
                            final Field bottomF = insetsObj.getClass().getField("bottom");

                            final int left = (int) leftF.get(insetsObj);
                            final int top = (int) topF.get(insetsObj);
                            final int right = (int) rightF.get(insetsObj);
                            final int bottom = (int) bottomF.get(insetsObj);

                            // Apply padding so nothing overlaps system bars
                            target.setPadding(left, top, right, bottom);

                            // IMPORTANT for API 30+: do not consume
                            return insets;
                        } catch (Throwable t) {
                            // If reflection fails for any reason, fall back to returning as-is
                            return insets;
                        }
                    });

                    // Ensure initial dispatch
                    contentRoot.requestApplyInsets();
                }
            } else {
                // --- Legacy path (API 19–29): your original behavior, unchanged ---
                decorView.setOnApplyWindowInsetsListener((v, insets) -> {
                    final ViewGroup vg = (v instanceof ViewGroup) ? (ViewGroup) v : null;
                    final View webView = (vg != null && vg.getChildCount() > 0) ? vg.getChildAt(0) : null;

                    if (webView != null) {
                        int topInset = insets.getSystemWindowInsetTop();
                        int bottomInset = insets.getSystemWindowInsetBottom();
                        // Left/right are typically 0 on phones in portrait for nav/status; keep your original top/bottom only.
                        webView.setPadding(0, topInset, 0, bottomInset);
                    }
                    // Legacy API expects consumption to prevent double-padding from parent views.
                    return insets.consumeSystemWindowInsets();
                });
            }
        }
    }
}