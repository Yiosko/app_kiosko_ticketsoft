package com.empresa.ticketsoft;

import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public class MainActivity extends AppCompatActivity {

    private static final String DOMINIO_PERMITIDO = "192.168.1.131:8000";

    private static final long INACTIVITY_TIME = 30 * 1000;

    private Handler handler = new Handler();
    private Runnable dimScreenRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_YES
        );

        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        activarPantallaCompleta();

        WebView webView = new WebView(this);
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

                return !url.contains(DOMINIO_PERMITIDO);
            }
        });

        webView.loadUrl("http://192.168.1.131:8000/");

        // Runnable para bajar brillo Uuuuhh
        dimScreenRunnable = () -> setScreenBrightness(0.2f);

        resetInactivityTimer();
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