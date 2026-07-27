package com.example.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import com.example.ui.theme.SyntaxAttr
import com.example.ui.theme.SyntaxComment
import com.example.ui.theme.SyntaxKeyword
import com.example.ui.theme.SyntaxString
import com.example.ui.theme.SyntaxTag

object SyntaxHighlighter {

    fun highlightCode(code: String, extension: String): AnnotatedString {
        return buildAnnotatedString {
            append(code)

            when (extension.lowercase()) {
                "html", "htm" -> highlightHtml(code)
                "js", "ts" -> highlightJs(code)
                "css" -> highlightCss(code)
                else -> { /* plain text fallback */ }
            }
        }
    }

    private fun AnnotatedString.Builder.highlightHtml(code: String) {
        // Tag regex: <(/?[a-zA-Z0-9]+)([^>]*)>
        val tagRegex = Regex("""<(/?[a-zA-Z0-9\-]+)""")
        tagRegex.findAll(code).forEach { match ->
            val range = match.groups[1]?.range
            if (range != null) {
                addStyle(
                    SpanStyle(color = SyntaxTag, fontWeight = FontWeight.Bold),
                    range.first,
                    range.last + 1
                )
            }
        }

        // Attribute regex: ([a-zA-Z\-]+)=
        val attrRegex = Regex("""\b([a-zA-Z\-]+)\s*=""")
        attrRegex.findAll(code).forEach { match ->
            val range = match.groups[1]?.range
            if (range != null) {
                addStyle(
                    SpanStyle(color = SyntaxAttr),
                    range.first,
                    range.last + 1
                )
            }
        }

        // String regex: "([^"]*)"
        val stringRegex = Regex(""""([^"]*)"""")
        stringRegex.findAll(code).forEach { match ->
            addStyle(
                SpanStyle(color = SyntaxString),
                match.range.first,
                match.range.last + 1
            )
        }
    }

    private fun AnnotatedString.Builder.highlightJs(code: String) {
        val keywords = listOf(
            "const", "let", "var", "function", "return", "if", "else",
            "for", "while", "class", "export", "import", "async", "await", "try", "catch"
        )
        keywords.forEach { kw ->
            val regex = Regex("""\b$kw\b""")
            regex.findAll(code).forEach { match ->
                addStyle(
                    SpanStyle(color = SyntaxKeyword, fontWeight = FontWeight.Bold),
                    match.range.first,
                    match.range.last + 1
                )
            }
        }

        // Strings
        val stringRegex = Regex(""""([^"]*)"|'([^']*)'|`([^`]*)`""")
        stringRegex.findAll(code).forEach { match ->
            addStyle(
                SpanStyle(color = SyntaxString),
                match.range.first,
                match.range.last + 1
            )
        }

        // Single line comments
        val commentRegex = Regex("""//.*""")
        commentRegex.findAll(code).forEach { match ->
            addStyle(
                SpanStyle(color = SyntaxComment),
                match.range.first,
                match.range.last + 1
            )
        }
    }

    private fun AnnotatedString.Builder.highlightCss(code: String) {
        // Selectors
        val selectorRegex = Regex("""([.#]?[a-zA-Z0-9\-_]+)\s*\{""")
        selectorRegex.findAll(code).forEach { match ->
            val range = match.groups[1]?.range
            if (range != null) {
                addStyle(
                    SpanStyle(color = SyntaxTag, fontWeight = FontWeight.Bold),
                    range.first,
                    range.last + 1
                )
            }
        }

        // Comments
        val commentRegex = Regex("""/\*[\s\S]*?\*/""")
        commentRegex.findAll(code).forEach { match ->
            addStyle(
                SpanStyle(color = SyntaxComment),
                match.range.first,
                match.range.last + 1
            )
        }
    }
}
