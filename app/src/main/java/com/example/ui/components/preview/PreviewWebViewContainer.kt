package com.example.ui.components.preview

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun PreviewWebViewContainer(
    initialUrl: String,
    serverUrl: String,
    isTabletMode: Boolean,
    onWebViewCreated: (WebView) -> Unit,
    onNavigationStateChanged: (canGoBack: Boolean, canGoForward: Boolean) -> Unit,
    onUrlChanged: (newPath: String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(if (isTabletMode) 12.dp else 0.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(if (isTabletMode) RoundedCornerShape(12.dp) else RoundedCornerShape(0.dp))
                .background(Color.White)
        ) {
            AndroidView(
                factory = { ctx ->
                    WebView(ctx).apply {
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.allowContentAccess = true
                        settings.allowFileAccess = true
                        webViewClient = object : WebViewClient() {
                            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                super.onPageStarted(view, url, favicon)
                                view?.let {
                                    onNavigationStateChanged(it.canGoBack(), it.canGoForward())
                                }
                                if (url != null && url.startsWith(serverUrl)) {
                                    val path = url.removePrefix(serverUrl).substringBefore("?")
                                    val cleanPath = if (path.isBlank() || path == "/") "/index.html" else path
                                    onUrlChanged(cleanPath)
                                }
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                view?.let {
                                    onNavigationStateChanged(it.canGoBack(), it.canGoForward())
                                }
                            }
                        }
                        onWebViewCreated(this)
                        loadUrl(initialUrl)
                    }
                },
                update = { webView ->
                    onWebViewCreated(webView)
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
