package com.example.ui.components.filemanager

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.native.CppEngine
import com.example.native.RustHttpServer
import com.example.ui.theme.LineNumberColor
import com.example.ui.theme.SoftGreen

@Composable
fun FileDrawerFooter(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val cppStatus = remember { CppEngine.getEngineStatus() }
        val isRustRunning = RustHttpServer.isServerRunning()

        Text(
            text = "⚡ $cppStatus",
            color = LineNumberColor,
            fontSize = 10.sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "🦀 Rust Server: ${if (isRustRunning) "http://127.0.0.1:8080" else "En Espera"}",
            color = if (isRustRunning) SoftGreen else LineNumberColor,
            fontSize = 10.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "DevStudio v1.0 • Entorno Polyglot Kotlin/C++/Rust",
            color = LineNumberColor,
            fontSize = 10.sp
        )
    }
}
