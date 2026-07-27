package com.example.ui.components

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Tablet
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.db.ProjectFileEntity
import com.example.native.RustHttpServer
import com.example.ui.theme.EditorBackground
import com.example.ui.theme.EditorBorder
import com.example.ui.theme.EditorPanelHeader
import com.example.ui.theme.LineNumberColor
import com.example.ui.theme.SoftGreen

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun LivePreviewView(
    files: List<ProjectFileEntity>,
    modifier: Modifier = Modifier
) {
    var keyToRefresh by remember { mutableStateOf(0) }
    var isTabletMode by remember { mutableStateOf(false) }
    var serverUrl by remember { mutableStateOf("http://127.0.0.1:8080") }
    val context = LocalContext.current

    // Start Rust Localhost HTTP server serving project files
    LaunchedEffect(files) {
        serverUrl = RustHttpServer.startServer(8080) { files }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(EditorBackground)
    ) {
        // Preview Header Toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(EditorPanelHeader)
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(SoftGreen)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "Vista Previa en Vivo",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 12.sp,
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = "$serverUrl/index.html (Rust Server)",
                    color = SoftGreen,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            IconButton(
                onClick = {
                    try {
                        val fullUrl = "$serverUrl/index.html"
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(fullUrl))
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(context, "No se pudo abrir el navegador externo: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                    contentDescription = "Abrir en navegador externo",
                    tint = SoftGreen,
                    modifier = Modifier.size(18.dp)
                )
            }

            IconButton(
                onClick = { isTabletMode = !isTabletMode },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = if (isTabletMode) Icons.Default.Tablet else Icons.Default.Smartphone,
                    contentDescription = "Cambiar formato de pantalla",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }

            IconButton(
                onClick = { keyToRefresh++ },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Recargar Vista Previa",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Box(
            modifier = Modifier
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
                val currentUrl = "$serverUrl/index.html?v=$keyToRefresh"

                AndroidView(
                    factory = { context ->
                        WebView(context).apply {
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            settings.allowContentAccess = true
                            settings.allowFileAccess = true
                            webViewClient = WebViewClient()
                            loadUrl(currentUrl)
                        }
                    },
                    update = { webView ->
                        webView.loadUrl(currentUrl)
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

