package com.test.adblocker

import android.os.Bundle
import android.webkit.URLUtil
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
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

        val rustAdBlocker: RustAdBlocker = RustAdBlocker.get()

        rustAdBlocker.setRules(
            listOf(
                "https://easylist.to/easylist/easylist.txt",
            )
        )

        webView.webViewClient = object : WebViewClient() {
            override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
                val url = request?.url.toString()

                val isAdUrl: Boolean = rustAdBlocker.check(url)

                if (isAdUrl) return null

                return super.shouldInterceptRequest(view, request)
            }
        }
    }
}