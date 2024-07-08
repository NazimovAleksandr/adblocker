# adblocker
Аn example of using an ad blocker

```kotlin
// getting the RustAdBlocker object
val rustAdBlocker: RustAdBlocker = RustAdBlocker.instance()

// setting the rules
rustAdBlocker.setRules(
    listOf(
        "https://easylist.to/easylist/easylist.txt",
    )
)

// using RustAdBlocker
webView.webViewClient = object : WebViewClient() {
    override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
        val url = request?.url.toString()

        val isAdUrl: Boolean = rustAdBlocker.check(url)

        if (isAdUrl) return null

        return super.shouldInterceptRequest(view, request)
    }
}
```