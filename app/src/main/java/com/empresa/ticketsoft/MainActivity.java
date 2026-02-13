package com.empresa.ticketsoft;

import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.net.Uri;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public class MainActivity extends AppCompatActivity {

    // private static final String DOMINIO_PERMITIDO = "192.168.1.131:8000";

    private static final long INACTIVITY_TIME = 30 * 1000;
    private Handler handler = new Handler();
    private Runnable dimScreenRunnable;
    private WebView webView;
    private static final String PREFS = "TicketSoftPrefs";
    private static final String KEY_URL = "server_url";

    private String getServerUrl() {
        return getSharedPreferences(PREFS, MODE_PRIVATE)
                .getString(KEY_URL, "http://192.168.1.131:8000/");
    }

    private void saveServerUrl(String url) {
        getSharedPreferences(PREFS, MODE_PRIVATE)
                .edit()
                .putString(KEY_URL, url)
                .apply();
    }

    private class WebBridge {

        @android.webkit.JavascriptInterface
        public String getUrl() {
            return getServerUrl();
        }

        @android.webkit.JavascriptInterface
        public void saveUrl(String url) {
            saveServerUrl(url);
        }

        @android.webkit.JavascriptInterface
        public void reloadApp() {
            runOnUiThread(() -> webView.loadUrl(getServerUrl()));
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            activarPantallaCompleta();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_YES
        );

        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        webView = new WebView(this);
        setContentView(webView);


        WebSettings settings = webView.getSettings();

        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url == null) return true;

                Uri uri = Uri.parse(url);
                Uri base = Uri.parse(getServerUrl());

                return !uri.getHost().equals(base.getHost());
            }

            @Override
            public void onReceivedError(
                    WebView view,
                    android.webkit.WebResourceRequest request,
                    android.webkit.WebResourceError error
            ) {
                if (request.isForMainFrame()) {
                    mostrarErrorPerzonalizado(view);
                }
            }

            @Override
            public void onReceivedHttpError(
                    WebView view,
                    android.webkit.WebResourceRequest request,
                    android.webkit.WebResourceResponse errorResponse
            ) {
                if (request.isForMainFrame()) {
                    mostrarErrorPerzonalizado(view);
                }
            }
        });


        webView.addJavascriptInterface(new WebBridge(), "Android");
        webView.loadUrl(getServerUrl());

        // Runnable para bajar brillo Uuuuhh
        dimScreenRunnable = () -> setScreenBrightness(0.2f);

        resetInactivityTimer();
    }

    private void mostrarErrorPerzonalizado(WebView webView){
        webView.loadUrl("file:///android_asset/error.html");
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        resetInactivityTimer();
        setScreenBrightness(1.0f); // brillo al 100%
        return super.dispatchTouchEvent(ev);
    }

    private void resetInactivityTimer() {
        handler.removeCallbacks(dimScreenRunnable);
        handler.postDelayed(dimScreenRunnable, INACTIVITY_TIME);
    }

    private void setScreenBrightness(float brightness) {
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.screenBrightness = brightness;
        getWindow().setAttributes(params);
    }

    @Override
    protected void onResume() {
        super.onResume();
        activarPantallaCompleta();
    }

    @Override
    public void onBackPressed() {
        // 🔒 Bloquea botón atrás (modo kiosko)
    }

    private void activarPantallaCompleta() {

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {

            getWindow().setDecorFitsSystemWindows(false);

            getWindow().getInsetsController().hide(
                    android.view.WindowInsets.Type.statusBars()
                            | android.view.WindowInsets.Type.navigationBars()
            );

            getWindow().getInsetsController().setSystemBarsBehavior(
                    android.view.WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            );

        } else {

            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            );
        }
    }

}