package com.example.ui.components

import android.webkit.WebView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.data.db.ProjectFileEntity
import com.example.native.RustHttpServer
import com.example.ui.components.preview.PreviewPageSelector
import com.example.ui.components.preview.PreviewToolbar
import com.example.ui.components.preview.PreviewWebViewContainer
import com.example.ui.theme.EditorBackground

@Composable
fun LivePreviewView(
    files: List<ProjectFileEntity>,
    modifier: Modifier = Modifier
) {
    var keyToRefresh by remember { mutableStateOf(0) }
    var isTabletMode by remember { mutableStateOf(false) }
    var serverUrl by remember { mutableStateOf("http://127.0.0.1:8080") }
    var currentPath by remember { mutableStateOf("/index.html") }
    var canGoBack by remember { mutableStateOf(false) }
    var canGoForward by remember { mutableStateOf(false) }
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }

    // Extract all HTML files available in the project for quick switching
    val htmlFiles = remember(files) {
        files.filter { !it.isDirectory && it.extension.lowercase() in listOf("html", "htm") }
            .sortedBy { if (it.name.equals("index.html", ignoreCase = true)) 0 else 1 }
    }

    // Start Rust Localhost HTTP server serving project files
    LaunchedEffect(files) {
        serverUrl = RustHttpServer.startServer(8080) { files }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(EditorBackground)
    ) {
        // Top Toolbar: Navigation & Device Format
        PreviewToolbar(
            canGoBack = canGoBack,
            canGoForward = canGoForward,
            serverUrl = serverUrl,
            currentPath = currentPath,
            isTabletMode = isTabletMode,
            onBackClick = { webViewInstance?.goBack() },
            onForwardClick = { webViewInstance?.goForward() },
            onRefreshClick = {
                webViewInstance?.reload()
                keyToRefresh++
            },
            onToggleTabletMode = { isTabletMode = !isTabletMode }
        )

        // Multi-page selector chips row if project contains HTML files
        PreviewPageSelector(
            htmlFiles = htmlFiles,
            currentPath = currentPath,
            onPageSelect = { file ->
                val targetPath = if (file.path.startsWith("/")) file.path else "/${file.path}"
                currentPath = targetPath
                val targetUrl = "$serverUrl$targetPath"
                webViewInstance?.loadUrl(targetUrl)
            }
        )

        // Embedded WebView container
        PreviewWebViewContainer(
            initialUrl = "$serverUrl$currentPath?v=$keyToRefresh",
            serverUrl = serverUrl,
            isTabletMode = isTabletMode,
            onWebViewCreated = { webViewInstance = it },
            onNavigationStateChanged = { back, forward ->
                canGoBack = back
                canGoForward = forward
            },
            onUrlChanged = { newPath ->
                currentPath = newPath
            }
        )
    }
}
