package com.example.ui.components

import android.annotation.SuppressLint
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.theme.EditorBackground
import com.example.ui.theme.EditorPanelHeader
import com.example.ui.theme.LineNumberColor
import org.json.JSONObject

class CodeEditorInterface(private val onContentChange: (String) -> Unit) {
    @JavascriptInterface
    fun onContentChange(newContent: String) {
        onContentChange(newContent)
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun CodeEditorView(
    filePath: String?,
    content: String,
    extension: String,
    onContentChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (filePath == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(EditorBackground),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Selecciona o crea un archivo en el gestor para comenzar a programar.",
                color = LineNumberColor,
                fontSize = 14.sp
            )
        }
        return
    }

    val lines = content.split("\n")
    val lineCount = maxOf(lines.size, 1)

    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var lastLoadedPath by remember { mutableStateOf<String?>(null) }

    // Synchronize editor content when file changes
    LaunchedEffect(filePath, content) {
        val webView = webViewRef ?: return@LaunchedEffect
        if (lastLoadedPath != filePath) {
            lastLoadedPath = filePath
            val safeContent = JSONObject.quote(content)
            webView.evaluateJavascript("if (window.setCode) { setCode($safeContent); }", null)
        }
    }

    val htmlContent = remember {
        """
        <!DOCTYPE html>
        <html>
        <head>
          <meta charset="utf-8">
          <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
          <style>
            * { box-sizing: border-box; }
            html, body {
              margin: 0;
              padding: 0;
              width: 100%;
              height: 100%;
              background-color: #121318;
              color: #F1F5F9;
              font-family: 'JetBrains Mono', Consolas, 'Courier New', monospace;
              overflow: hidden;
            }
            #editor-wrapper {
              display: flex;
              width: 100%;
              height: 100%;
              position: relative;
              background-color: #121318;
            }
            #line-numbers {
              width: 48px;
              height: 100%;
              background-color: #1A1C23;
              color: #64748B;
              border-right: 1px solid #2D303E;
              font-size: 13px;
              line-height: 1.6;
              padding: 12px 6px 12px 0;
              text-align: right;
              user-select: none;
              overflow: hidden;
              font-family: inherit;
              flex-shrink: 0;
            }
            #code-area {
              flex: 1;
              height: 100%;
              background: #121318;
              color: #F1F5F9;
              border: none;
              outline: none;
              resize: none;
              padding: 12px;
              font-size: 13px;
              line-height: 1.6;
              font-family: inherit;
              white-space: pre;
              overflow: auto;
              tab-size: 2;
              -moz-tab-size: 2;
            }
          </style>
        </head>
        <body>
          <div id="editor-wrapper">
            <div id="line-numbers">1</div>
            <textarea id="code-area" spellcheck="false" wrap="off"></textarea>
          </div>
          <script>
            var tx = document.getElementById('code-area');
            var ln = document.getElementById('line-numbers');
            var isHostUpdating = false;

            function updateLineNumbers() {
              var lines = tx.value.split('\n').length;
              var nums = [];
              for (var i = 1; i <= Math.max(lines, 1); i++) {
                nums.push(i);
              }
              ln.innerHTML = nums.join('<br>');
            }

            tx.addEventListener('scroll', function() {
              ln.scrollTop = tx.scrollTop;
            });

            tx.addEventListener('input', function() {
              updateLineNumbers();
              if (!isHostUpdating && window.AndroidEditor && window.AndroidEditor.onContentChange) {
                window.AndroidEditor.onContentChange(tx.value);
              }
            });

            tx.addEventListener('keydown', function(e) {
              if (e.key === 'Tab') {
                e.preventDefault();
                var start = tx.selectionStart;
                var end = tx.selectionEnd;
                tx.value = tx.value.substring(0, start) + '  ' + tx.value.substring(end);
                tx.selectionStart = tx.selectionEnd = start + 2;
                updateLineNumbers();
                if (window.AndroidEditor && window.AndroidEditor.onContentChange) {
                  window.AndroidEditor.onContentChange(tx.value);
                }
              }
            });

            function setCode(text) {
              isHostUpdating = true;
              tx.value = text;
              updateLineNumbers();
              setTimeout(function() { isHostUpdating = false; }, 30);
            }

            function insertText(text) {
              var start = tx.selectionStart;
              var end = tx.selectionEnd;
              tx.value = tx.value.substring(0, start) + text + tx.value.substring(end);
              tx.selectionStart = tx.selectionEnd = start + text.length;
              tx.focus();
              updateLineNumbers();
              if (window.AndroidEditor && window.AndroidEditor.onContentChange) {
                window.AndroidEditor.onContentChange(tx.value);
              }
            }
          </script>
        </body>
        </html>
        """.trimIndent()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(EditorBackground)
    ) {
        // Code Editor Box
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            AndroidView(
                factory = { ctx ->
                    WebView(ctx).apply {
                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            databaseEnabled = true
                            cacheMode = WebSettings.LOAD_DEFAULT
                            useWideViewPort = true
                            loadWithOverviewMode = true
                        }
                        webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                val safeContent = JSONObject.quote(content)
                                view?.evaluateJavascript("setCode($safeContent);", null)
                            }
                        }
                        addJavascriptInterface(CodeEditorInterface { newText ->
                            onContentChange(newText)
                        }, "AndroidEditor")

                        loadDataWithBaseURL("https://localhost/", htmlContent, "text/html", "UTF-8", null)
                        webViewRef = this
                    }
                },
                update = { webView ->
                    webViewRef = webView
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        // Bottom Editor Info Status Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(EditorPanelHeader)
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "✏️ Editor de Código",
                color = LineNumberColor,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Líneas: $lineCount",
                color = LineNumberColor,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Caracteres: ${content.length}",
                color = LineNumberColor,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = extension.uppercase(),
                color = MaterialTheme.colorScheme.primary,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
