package com.test.adblocker

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.webkit.URLUtil
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.adblock.AdvtBlocker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.URL
import java.net.URLConnection

class MainActivity : AppCompatActivity() {

    private val urlRules: MutableList<String> = mutableListOf()

    private var advtBlocker: AdvtBlocker? = null

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val webView: WebView = findViewById(R.id.web_view)
        val editText: EditText = findViewById(R.id.edit_text)
        val button: Button = findViewById(R.id.button)

        button.setOnClickListener {
            var url = editText.text.toString()

            if (!URLUtil.isValidUrl(url)) {
                url = "https://www.google.com/search?q=$url"
            }

            webView.loadUrl(url)
        }

        lifecycleScope.launch(Dispatchers.IO) {
            loadRulesFromUrl("https://easylist.to/easylist/easylist.txt")
            initNativeLib()
        }

        var baseUrl = ""
        var sourceUrl = ""

        webView.settings.javaScriptEnabled = true

        webView.webViewClient = object : WebViewClient() {

            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                baseUrl = request?.url.toString()
                return super.shouldOverrideUrlLoading(view, request)
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                sourceUrl = url.toString()
            }

            override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
                val url = request?.url.toString()

                val fake = false

                val isAdUrl: Boolean = if (fake) {
                    advtBlocker?.checkUrls(
                        /* url = */ "http://example.com/-advertisement-icon.",
                        /* sourceUrl = */ "http://example.com/helloworld",
                        /* requestType = */ "image"
                    ) ?: false
                } else {
                    advtBlocker?.checkUrls(
                        /* url = */ url,
                        /* sourceUrl = */ sourceUrl,
                        /* requestType = */ request?.getRequestType(baseUrl) ?: "_",
                    ) ?: false
                }

                Log.d("TAG_advtBlocker", "shouldInterceptRequest: isAdUrl = $isAdUrl")

                return super.shouldInterceptRequest(view, request)
            }
        }
    }

    private fun initNativeLib() {
        try {
            Log.d("TAG_advtBlocker", "initNativeLib: ${Build.SUPPORTED_ABIS[0]}")

            val fake = false

            val rules: List<String> = if (fake) {
                ArrayList(
                    listOf(
                        "-advertisement-icon.",
                        "-advertisement-management/",
                        "-advertisement.",
                        "-advertisement/script."
                    )
                )
            } else {
                urlRules
            }

            advtBlocker = AdvtBlocker.createInstance(rules)

            Log.d("TAG_advtBlocker", "initNativeLib: end")
        } catch (e: Exception) {
            Log.e("TAG_advtBlocker", "initNativeLib: ${e.message}")
        }
    }

    private fun WebResourceRequest.getRequestType(baseUrl: String): String {
        val url = url.toString()
        val method = method
        val headers = requestHeaders

        return when {
            method.equals(
                "POST",
                ignoreCase = true
            ) && headers["Content-Type"]?.contains("application/json") == true && headers.containsKey("Origin") -> "beacon"

            method.equals(
                "POST",
                ignoreCase = true
            ) && headers["Content-Type"]?.contains("application/csp-report") == true -> "csp_report"

            url.equals(baseUrl, ignoreCase = true) -> "document"
            url.contains(".woff") || url.contains(".ttf") || url.contains(".otf") -> "font"
            url.contains(".jpg") || url.contains(".jpeg") || url.contains(".png") || url.contains(".gif") || url.contains(
                ".bmp"
            ) || url.contains(".webp") -> "image"

            url.contains(".mp3") || url.contains(".mp4") || url.contains(".avi") || url.contains(".mkv") || url.contains(
                ".webm"
            ) -> "media"

            url.contains(".swf") || url.contains(".flv") -> "object"
            url.contains("ping") -> "ping"
            url.contains(".js") -> "script"
            url.contains(".css") -> "stylesheet"
            url.contains("frame") -> "sub_frame"
            headers["Upgrade"]?.contains("websocket") == true -> "websocket"
            method.equals(
                "POST",
                ignoreCase = true
            ) && headers["Content-Type"]?.contains("application/json") == true -> "xhr"

            url.contains(".manifest") -> "web_manifest"
            url.contains(".xbl") -> "xbl"
            url.contains(".dtd") -> "xml_dtd"
            url.contains(".xslt") -> "xslt"
            url.contains("speculative") -> "speculative"
            else -> "_"
        }
    }

    @Suppress("SameParameterValue")
    private fun loadRulesFromUrl(url: String) {
        try {
            val urlConnection: URLConnection? = URL(url).openConnection()
            val inputStream: InputStream? = urlConnection?.getInputStream()
            val reader = BufferedReader(InputStreamReader(inputStream))

            var line = ""

            while (reader.readLine()?.also { line = it } != null) {
                addRule(line)
            }

            reader.close()
            inputStream?.close()

            Log.d("TAG_advtBlocker", "update: urlRules.size = ${urlRules.size}")
        } catch (ignore: Exception) {
        }
    }

    private fun addRule(line: String) {
        when {
            line.isEmpty() -> return
            line.startsWith("!") -> return
            line.startsWith("[") -> return

            else -> urlRules.add(
                line.replace("||", "//")
            )
        }
    }
}