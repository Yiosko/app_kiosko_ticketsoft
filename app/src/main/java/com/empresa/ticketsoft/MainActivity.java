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
            @Override
            public void onReceivedError(
                    WebView view,
                    int errorCode,
                    String description,
                    String failing
            ){
                mostrarErrorPerzonalizado(view);
            }

            public void onReceivedHttpError(
                    WebView view,
                    android.webkit.WebResourceRequest request,
                    android.webkit.WebResourceResponse errorResponse
            ){
                mostrarErrorPerzonalizado(view);
            }
        });

        webView.loadUrl("http://192.168.1.131:8000/");

        // Runnable para bajar brillo Uuuuhh
        dimScreenRunnable = () -> setScreenBrightness(0.2f);

        resetInactivityTimer();
    }

    private void mostrarErrorPerzonalizado(WebView webView){
        String html =
                "<html>"+
            "<head>" +
            "<meta name='viewport' content='width=device-width, initial-scale=1'>"+
            "<style>"+
                "body {"+
                    "background: radial-gradient(circle at top, #3fa3ff, #0a7bfb, #2c5364);"+
                    "font-family: 'Segoe UI', Arial, sans-serif;"+
                    "display: flex;"+
                    "justify-content: center;"+
                    "align-items: center;"+
                    "height: 100vh;"+
                    "margin: 0; }"+

                ".window {"+
                    "width: 380px;"+
                    "background: #f2f2f2;"+
                    "border-radius: 8px;"+
                    "box-shadow: 0 20px 50px rgba(0,0,0,0.6);"+
                    "overflow: hidden;}"+

                ".title-bar {"+
                    "background: linear-gradient(to right, #0050ef, #0078d7);"+
                    "color: white;"+
                    "padding: 10px;"+
                    "display: flex;"+
                    "align-items: center;"+
                    "justify-content: space-between;"+
                    "font-weight: bold;}"+

                ".title-bar span {"+
                    "font-size: 14px;"+
                "}"+

                ".window-buttons {"+
                    "display: flex;"+
                    "gap: 6px;}"+

                ".btn {"+
                    "width: 14px;"+
                    "height: 14px;"+
                    "background: #cfcfcf;"+
                    "border: 1px solid #888;}"+

                ".content {"+
                    "padding: 25px;"+
                    "text-align: center;"+
                    "color: #222;}"+

                ".content img {"+
                    "width: 160px;"+
                    "margin-bottom: 15px;}"+

                ".content h1 {"+
                    "font-size: 18px;"+
                    "margin-bottom: 10px;}"+

                ".content p {"+
                    "font-size: 14px;"+
                    "color: #555;"+
                "}"+

                "button {"+
                    "margin-top: 20px;"+
                    "width: 100%;"+
                    "padding: 12px;"+
                    "font-size: 15px;"+
                    "border: none;"+
                    "border-radius: 6px;"+
                    "background: #0078d7;"+
                    "color: white;"+
                    "cursor: pointer;}"+

                "button:active {"+
                    "background: #0050ef;"+
                "}"+
            "</style>"+
        "</head>"+
        "<body>"+

            "<div class='window'>"+
                "<div class='title-bar'>"+
                    "<span>Error de conexión</span>"+
                "</div>"+

                "<div class='content'>"+
                    "<img src='file:///android_res/drawable/logo_ticketsoft.webp'/>"+
                    "<h1>No se puede acceder a TicketSoft</h1>"+
                    "<p>"+
                        "El sistema no logró comunicarse con el servidor.<br>"+
                        "Verifique la conexión de red."+
                    "</p>"+
                    "<button onclick='location.reload()'>Reintentar</button>"+
                "</div>"+
            "</div>"+

        "</body>"+
        "</html>";

        webView.loadDataWithBaseURL(
                "file:///android_res/",
                html,
                "text/html",
                "UTF-8",
                null
        );
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