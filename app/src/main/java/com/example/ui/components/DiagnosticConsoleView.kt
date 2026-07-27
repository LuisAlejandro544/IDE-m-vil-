package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.DiagnosticLogEntity
import com.example.ui.components.diagnostic.DiagnosticLogCard
import com.example.ui.components.diagnostic.DiagnosticStatusBanner
import com.example.ui.components.diagnostic.DiagnosticToolbar
import com.example.ui.theme.EditorBackground
import com.example.ui.theme.EditorBorder
import com.example.ui.theme.EditorSurface
import com.example.ui.theme.LineNumberColor

@Composable
fun DiagnosticConsoleView(
    logs: List<DiagnosticLogEntity>,
    onRunLinter: () -> Unit,
    onClearLogs: () -> Unit,
    onSendDiagnosticsToAi: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("Todos") }
    val filterOptions = listOf("Todos", "Errores", "Advertencias", "Linter", "Sistema")

    val errorCount = logs.count { it.level == "ERROR" }
    val warningCount = logs.count { it.level == "WARNING" }

    val filteredLogs = logs.filter { log ->
        when (selectedFilter) {
            "Errores" -> log.level == "ERROR"
            "Advertencias" -> log.level == "WARNING"
            "Linter" -> log.source == "Linter"
            "Sistema" -> log.source == "System" || log.source == "Compiler" || log.source == "Rust Server"
            else -> true
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(EditorBackground)
            .padding(12.dp)
    ) {
        // Status Overview Banner
        DiagnosticStatusBanner(
            errorCount = errorCount,
            warningCount = warningCount
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Filter chips and actions
        DiagnosticToolbar(
            filterOptions = filterOptions,
            selectedFilter = selectedFilter,
            onFilterSelected = { selectedFilter = it },
            onRunLinter = onRunLinter,
            onClearLogs = onClearLogs,
            onSendDiagnosticsToAi = onSendDiagnosticsToAi
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Diagnostic Logs List
        if (filteredLogs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(1.dp, EditorBorder, RoundedCornerShape(10.dp))
                    .background(EditorSurface)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.BugReport,
                        contentDescription = "Sin registros",
                        tint = LineNumberColor,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No hay registros en la Consola de Diagnóstico", fontSize = 13.sp, color = LineNumberColor)
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .border(1.dp, EditorBorder, RoundedCornerShape(10.dp))
                    .background(EditorSurface)
                    .padding(8.dp)
            ) {
                items(filteredLogs) { log ->
                    DiagnosticLogCard(log = log)
                }
            }
        }
    }
}
