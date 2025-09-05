import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.graphics.Insets;

public class AndroidEdgeToEdge extends org.apache.cordova.CordovaPlugin {
    @Override
    public boolean execute(String action, org.json.JSONArray args,
                           org.apache.cordova.CallbackContext callbackContext) {
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
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            );
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

            if (Build.VERSION.SDK_INT >= 30) {
                // Android 11+ → Android 15
                window.setDecorFitsSystemWindows(false);

                View contentRoot = decorView.findViewById(android.R.id.content);
                if (contentRoot != null) {
                    View target = (contentRoot instanceof ViewGroup && ((ViewGroup) contentRoot).getChildCount() > 0)
                            ? ((ViewGroup) contentRoot).getChildAt(0)
                            : contentRoot;

                    if (target != null) {
                        target.setFitsSystemWindows(false);

                        contentRoot.setOnApplyWindowInsetsListener((v, insets) -> {
                            Insets systemBars = insets.getInsets(WindowInsets.Type.systemBars());
                            target.setPadding(0, systemBars.top, 0, systemBars.bottom);
                            return insets;
                        });

                        contentRoot.requestApplyInsets();
                    }
                }
            } else {
                // Android 4.4 – 10
                decorView.setOnApplyWindowInsetsListener((v, insets) -> {
                    ViewGroup vg = (v instanceof ViewGroup) ? (ViewGroup) v : null;
                    View webView = (vg != null && vg.getChildCount() > 0) ? vg.getChildAt(0) : null;

                    if (webView != null) {
                        int topInset = insets.getSystemWindowInsetTop();
                        int bottomInset = insets.getSystemWindowInsetBottom();
                        webView.setPadding(0, topInset, 0, bottomInset);
                    }
                    return insets.consumeSystemWindowInsets();
                });
            }
        }
    }
}