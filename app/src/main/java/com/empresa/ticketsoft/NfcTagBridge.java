package com.empresa.ticketsoft;

import android.nfc.Tag;
import android.util.Log;
import android.webkit.WebView;

/**
 * Lee un tag NFC (MIFARE Classic 4 bytes, NTAG 7 bytes) con el lector
 * nativo de la tablet y empuja el UID crudo al WebView — nunca decide
 * nada de negocio, igual que el chip iButton del lado del Arduino/Pi.
 * El JS (contexts/nfc-tag en ts-dispenser-gui) es quien valida/resuelve.
 */
public class NfcTagBridge {
    private static final long DEDUPE_WINDOW_MS = 2500;
    private String ultimoUid = null;
    private long ultimaLecturaMs = 0;
    private final WebView webView;

    public NfcTagBridge(WebView webView) {
        this.webView = webView;
    }

    public void handleTagDiscovered(Tag tag) {
        byte[] id = tag.getId();
        // Solo UID de 4 bytes (MIFARE Classic, confirmado con hardware real)
        // o 7 bytes (NTAG) — el endpoint que resuelve esto en el servidor
        // solo acepta esos dos largos. NFC-F/NFC-V dan 8 bytes, un UID
        // triple-size de NFC-A da 10, y algunos lectores devuelven un
        // arreglo vacío: cualquiera de esos se guardaría en Serial 1/2 al
        // crear un vehículo pero nunca podría resolverse al vender. Mejor
        // descartarlo acá, con log, que dejarlo pasar como un tag "válido"
        // que jamás funciona.
        if (id == null || (id.length != 4 && id.length != 7)) {
            Log.w("NfcTagBridge", "tag descartado, largo de UID no soportado: "
                + (id == null ? "null" : id.length + " bytes"));
            return;
        }
        String uid = "NFC" + bytesToHex(id);
        long ahora = System.currentTimeMillis();
        if (uid.equals(ultimoUid) && (ahora - ultimaLecturaMs) < DEDUPE_WINDOW_MS) {
            return;
        }
        ultimoUid = uid;
        ultimaLecturaMs = ahora;
        Log.i("NfcTagBridge", "tag leido uid=" + uid);
        webView.post(() -> webView.evaluateJavascript(
            "window.onNfcTag && window.onNfcTag('" + uid + "')", null));
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }
}
