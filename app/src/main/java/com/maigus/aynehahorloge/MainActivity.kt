// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 MAIGUS

package com.maigus.aynehahorloge

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.util.Base64
import android.view.View
import android.view.WindowManager
import android.webkit.GeolocationPermissions
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.webkit.WebViewAssetLoader
import com.maigus.aynehahorloge.widget.WidgetUpdater
import org.json.JSONObject
import java.io.File

/**
 * Affiche l'Horloge & Calendrier AYNEHA (index.html, la même page que sur le
 * site) depuis les fichiers embarqués dans l'APK : 100 % hors connexion.
 * Le pont « AynehaApp » donne à la page ce que le navigateur seul ne peut pas
 * faire : partager une image, garder l'écran allumé, régler la batterie.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var infoButton: View
    private var geoOrigin: String? = null
    private var geoCallback: GeolocationPermissions.Callback? = null

    // Mise à jour depuis ayneha.org (voir MiseAJour.kt).
    private var majInfo: MiseAJour.Info? = null
    @Volatile private var majFichier: File? = null

    /** Envoie l'état de la mise à jour à la page : window.aynehaMaj({etat, ...}). */
    private fun envoyerMaj(etat: String, version: String? = null, pct: Int? = null) {
        val o = JSONObject().put("etat", etat)
        if (version != null) o.put("version", version)
        if (pct != null) o.put("pct", pct)
        runOnUiThread { webView.evaluateJavascript("window.aynehaMaj && window.aynehaMaj($o)", null) }
    }

    companion object { private const val REQ_LOCATION = 41 }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQ_LOCATION) {
            val ok = grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
            geoCallback?.invoke(geoOrigin, ok, false)
            geoCallback = null; geoOrigin = null
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.webview)
        infoButton = findViewById<TextView>(R.id.btn_info)

        val assetLoader = WebViewAssetLoader.Builder()
            .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(this))
            .build()

        webView.webViewClient = object : androidx.webkit.WebViewClientCompat() {
            override fun shouldInterceptRequest(
                view: WebView,
                request: android.webkit.WebResourceRequest
            ): android.webkit.WebResourceResponse? = assetLoader.shouldInterceptRequest(request.url)
        }

        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.allowFileAccess = false
        webView.settings.setGeolocationEnabled(true)
        webView.addJavascriptInterface(Bridge(), "AynehaApp")

        // Bouton « position » de la page : position approximative, demandée
        // seulement quand on touche ce bouton, pour calculer le coucher du soleil.
        webView.webChromeClient = object : WebChromeClient() {
            override fun onGeolocationPermissionsShowPrompt(origin: String, callback: GeolocationPermissions.Callback) {
                if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    callback.invoke(origin, true, false)
                } else {
                    geoOrigin = origin; geoCallback = callback
                    requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), REQ_LOCATION)
                }
            }
        }

        infoButton.setOnClickListener { startActivity(Intent(this, PrivacyActivity::class.java)) }

        webView.loadUrl("https://appassets.androidplatform.net/assets/index.html")
    }

    override fun onResume() {
        super.onResume()
        // Au retour dans l'application (ex. après le réglage batterie) : rafraîchir.
        webView.evaluateJavascript("window.aynehaRefresh && window.aynehaRefresh()", null)
        // Retour du réglage « applications inconnues » : lancer l'installation en attente.
        majFichier?.let { f ->
            if (MiseAJour.peutInstaller(this)) { majFichier = null; envoyerMaj("pret"); MiseAJour.installer(this, f) }
        }
        WidgetUpdater.refresh(this)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (webView.canGoBack()) webView.goBack() else @Suppress("DEPRECATION") super.onBackPressed()
    }

    private fun isBatteryRestricted(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return false
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        return !pm.isIgnoringBatteryOptimizations(packageName)
    }

    private inner class Bridge {

        /** Tirer l'écran vers le bas : chercher une nouvelle version sur ayneha.org. */
        @JavascriptInterface
        fun checkUpdate() {
            envoyerMaj("verif")
            Thread {
                try {
                    val info = MiseAJour.chercher()
                    val actuelle = MiseAJour.versionActuelle(this@MainActivity)
                    if (info == null || MiseAJour.comparer(info.version, actuelle) <= 0) envoyerMaj("ajour", actuelle)
                    else { majInfo = info; envoyerMaj("dispo", info.version) }
                } catch (e: Exception) { envoyerMaj("erreur") }
            }.start()
        }

        /** Télécharger la nouvelle version, vérifier son empreinte, puis proposer l'installation. */
        @JavascriptInterface
        fun downloadUpdate() {
            val info = majInfo ?: return
            Thread {
                try {
                    val f = MiseAJour.dejaTelecharge(this@MainActivity, info)
                        ?: MiseAJour.telecharger(this@MainActivity, info) { p -> envoyerMaj("telech", pct = p) }
                    if (!MiseAJour.peutInstaller(this@MainActivity)) {
                        majFichier = f
                        envoyerMaj("autoriser")
                        runOnUiThread { MiseAJour.ouvrirAutorisation(this@MainActivity) }
                    } else {
                        envoyerMaj("pret")
                        runOnUiThread { MiseAJour.installer(this@MainActivity, f) }
                    }
                } catch (e: SecurityException) { envoyerMaj("refuse")
                } catch (e: Exception) { envoyerMaj("erreur") }
            }.start()
        }

        @JavascriptInterface
        fun getPlace(): String = SonghayCalendar.getPlace(this@MainActivity)

        @JavascriptInterface
        fun setPlace(lat: Double, lon: Double) {
            SonghayCalendar.setPlace(this@MainActivity, lat, lon)
            WidgetUpdater.updateAll(this@MainActivity)
        }

        @JavascriptInterface
        fun getLunarAdjust(): Int = SonghayCalendar.getAdjust(this@MainActivity)

        @JavascriptInterface
        fun setLunarAdjust(value: Int) {
            SonghayCalendar.setAdjust(this@MainActivity, value)
            WidgetUpdater.updateAll(this@MainActivity)
        }

        /** Vrai seulement si un widget est posé ET que la batterie peut le freiner. */
        @JavascriptInterface
        fun isBatteryRestricted(): Boolean =
            WidgetUpdater.widgetCount(this@MainActivity) > 0 && this@MainActivity.isBatteryRestricted()

        @JavascriptInterface
        fun openBatterySettings() {
            runOnUiThread {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return@runOnUiThread
                try {
                    startActivity(Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:$packageName")))
                } catch (e: Exception) {
                    try { startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)) } catch (_: Exception) {}
                }
            }
        }

        /** Mode veille de nuit : écran allumé + plein écran. */
        @JavascriptInterface
        fun setNightMode(on: Boolean) {
            runOnUiThread {
                val ctrl = WindowCompat.getInsetsController(window, window.decorView)
                if (on) {
                    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    ctrl.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                    ctrl.hide(WindowInsetsCompat.Type.systemBars())
                    infoButton.visibility = View.GONE
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    ctrl.show(WindowInsetsCompat.Type.systemBars())
                    infoButton.visibility = View.VISIBLE
                }
            }
        }

        /** Partage l'image PNG (en base64) préparée par la page. */
        @JavascriptInterface
        fun shareImage(base64Png: String) {
            val bytes = Base64.decode(base64Png, Base64.DEFAULT)
            val dir = File(cacheDir, "partage").apply { mkdirs() }
            val file = File(dir, "ayneha-horloge.png")
            file.writeBytes(bytes)
            val uri = FileProvider.getUriForFile(this@MainActivity, "$packageName.fileprovider", file)
            val send = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            runOnUiThread { startActivity(Intent.createChooser(send, null)) }
        }
    }
}
