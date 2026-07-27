package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EditorBorder
import com.example.ui.theme.EditorPanelHeader

@Composable
fun QuickSymbolBar(
    onSymbolClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val symbols = listOf(
        "<", ">", "/", "=", "\"", "'", "{", "}", "(", ")",
        "[", "]", ";", ":", "!", "+", "-", "$", "TAB"
    )

    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .background(EditorPanelHeader)
            .padding(vertical = 6.dp),
        contentPadding = PaddingValues(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(symbols) { symbol ->
            val symbolToInsert = if (symbol == "TAB") "  " else symbol
            Box(
                modifier = Modifier
                    .defaultMinSize(minWidth = 38.dp, minHeight = 36.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(EditorBorder)
                    .clickable { onSymbolClick(symbolToInsert) }
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = symbol,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}
