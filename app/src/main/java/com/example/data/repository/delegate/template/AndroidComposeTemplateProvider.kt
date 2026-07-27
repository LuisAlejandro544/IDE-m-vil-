package com.example.data.repository.delegate.template

import com.example.data.db.ProjectFileEntity

object AndroidComposeTemplateProvider {
    fun getFiles(projectId: Long): List<ProjectFileEntity> {
        return listOf(
            ProjectFileEntity(
                projectId = projectId,
                name = "MainActivity.kt",
                path = "/src/main/java/MainActivity.kt",
                extension = "kt",
                parentPath = "/src/main/java",
                content = """
                    package com.example.app

                    import androidx.compose.foundation.layout.*
                    import androidx.compose.material3.*
                    import androidx.compose.runtime.*
                    import androidx.compose.ui.Modifier
                    import androidx.compose.ui.unit.dp

                    @Composable
                    fun MainScreen() {
                        var count by remember { mutableStateOf(0) }

                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.background
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "Jetpack Compose App",
                                    style = MaterialTheme.typography.headlineMedium
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Contador: ${'$'}count",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(onClick = { count++ }) {
                                    Text("Incrementar")
                                }
                            }
                        }
                    }
                """.trimIndent()
            ),
            ProjectFileEntity(
                projectId = projectId,
                name = "Theme.kt",
                path = "/src/main/java/Theme.kt",
                extension = "kt",
                parentPath = "/src/main/java",
                content = """
                    package com.example.app.ui.theme

                    import androidx.compose.material3.MaterialTheme
                    import androidx.compose.material3.darkColorScheme
                    import androidx.compose.runtime.Composable
                    import androidx.compose.ui.graphics.Color

                    private val DarkColorScheme = darkColorScheme(
                        primary = Color(0xFF4F83F6),
                        background = Color(0xFF121318),
                        surface = Color(0xFF1A1C23)
                    )

                    @Composable
                    fun AppTheme(content: @Composable () -> Unit) {
                        MaterialTheme(
                            colorScheme = DarkColorScheme,
                            content = content
                        )
                    }
                """.trimIndent()
            )
        )
    }
}
