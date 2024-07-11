# adblocker
Аn example of using an ad blocker

путь к файлам so:
```text
adblock/
└── src/
    └── main/
        └── jniLibs/
            ├── armeabi-v7a/
            │   └── libadblock_coffee.so
            ├── arm64-v8a/
            │   └── libadblock_coffee.so
            ├── x86/
            │   └── libadblock_coffee.so
            └── x86_64/
                └── libadblock_coffee.so
```

```kotlin
override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
    val url = request?.url.toString()

    // Переключение между реальными данными и фейковыми (для AdvtBlocker.checkUrls)
    val fake = false

    val isAdUrl: Boolean = if (fake) { ... } else { ... }

    Log.d("TAG_advtBlocker", "shouldInterceptRequest: isAdUrl = $isAdUrl")

    return super.shouldInterceptRequest(view, request)
}
```

```kotlin
private fun initNativeLib() {
    try {
        // Переключение между реальными правилами для AdvtBlocker и фейковыми
        val fake = false

        val rules: List<String> = if (fake) { ... } else { ... }

        advtBlocker = AdvtBlocker.createInstance(rules)
    } catch (e: Exception) {
        Log.e("TAG_advtBlocker", "initNativeLib: ${e.message}")
    }
}
```