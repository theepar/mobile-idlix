package com.example.watchmobile.utils

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import java.io.ByteArrayInputStream

object CloudflareBypasser {
    private const val TAG = "CloudflareBypasser"
    private var isBypassing = false
    private var bypassedCookies: String = ""
    var bypassedUserAgent: String = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36"
        private set

    // Simple adblocker domains list
    private val adDomains = listOf(
        "adsterra", "popads", "doubleclick", "exoclick", "google-analytics",
        "googlesyndication", "adskeeper", "mgid", "propellerads", "onclickads"
    )

    fun getCookies() = bypassedCookies
    
    fun hasValidCookies(): Boolean {
        return bypassedCookies.contains("cf_clearance") || bypassedCookies.isNotEmpty()
    }

    @SuppressLint("SetJavaScriptEnabled")
    suspend fun bypass(context: Context, url: String): Boolean = withContext(Dispatchers.Main) {
        if (isBypassing) {
            while (isBypassing) {
                delay(500)
            }
            return@withContext hasValidCookies()
        }
        
        Log.d(TAG, "Starting Cloudflare bypass for $url")
        isBypassing = true
        
        val result = try {
            suspendCoroutine<Boolean> { continuation ->
                var isResumed = false
                val webView = try {
                    WebView(context).apply {
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.userAgentString = settings.userAgentString.replace("; wv", "")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to create WebView: ${e.message}")
                    if (!isResumed) {
                        isResumed = true
                        continuation.resume(false)
                    }
                    null
                }

                if (webView == null) return@suspendCoroutine

                bypassedUserAgent = webView.settings.userAgentString

            webView.webViewClient = object : WebViewClient() {
                // Intercept request to block ads/popups, reducing noise during Cloudflare challenge
                override fun shouldInterceptRequest(
                    view: WebView?,
                    request: WebResourceRequest?
                ): WebResourceResponse? {
                    val urlStr = request?.url?.toString() ?: return null
                    // If the url contains any known ad domain, block it
                    if (adDomains.any { urlStr.contains(it) }) {
                        Log.d(TAG, "Ad blocked: $urlStr")
                        return WebResourceResponse("text/plain", "UTF-8", ByteArrayInputStream(ByteArray(0)))
                    }
                    return super.shouldInterceptRequest(view, request)
                }

                override fun onPageFinished(view: WebView, url: String) {
                    val cookies = CookieManager.getInstance().getCookie(url)
                    Log.d(TAG, "Page finished. Cookies: $cookies")
                    
                    if (cookies != null && cookies.contains("cf_clearance")) {
                        bypassedCookies = cookies
                        if (!isResumed) {
                            isResumed = true
                            continuation.resume(true)
                            view.destroy()
                        }
                    } else {
                        view.evaluateJavascript("(function() { return document.title; })();") { title ->
                            Log.d(TAG, "Page title: $title")
                            if (title != null && !title.contains("Just a moment", ignoreCase = true) 
                                && !title.contains("Attention Required", ignoreCase = true)) {
                                bypassedCookies = cookies ?: ""
                                if (!isResumed) {
                                    isResumed = true
                                    continuation.resume(true)
                                    view.destroy()
                                }
                            }
                        }
                    }
                }
            }
            
            webView.loadUrl(url)
            
            // Timeout after 15 seconds
            Handler(Looper.getMainLooper()).postDelayed({
                if (!isResumed) {
                    Log.w(TAG, "Bypass timed out!")
                    isResumed = true
                    bypassedCookies = CookieManager.getInstance().getCookie(url) ?: ""
                    continuation.resume(hasValidCookies())
                    webView.destroy()
                }
            }, 15000)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Bypass error: ${e.message}")
            false
        }
        
        isBypassing = false
        Log.d(TAG, "Bypass finished. Success: $result")
        return@withContext result
    }
}
