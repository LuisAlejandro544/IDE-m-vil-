package com.example.data.repository.delegate.template

import com.example.data.db.ProjectFileEntity

object NodeApiTemplateProvider {
    fun getFiles(projectId: Long): List<ProjectFileEntity> {
        return listOf(
            ProjectFileEntity(
                projectId = projectId,
                name = "server.js",
                path = "/server.js",
                extension = "js",
                content = """
                    const http = require('http');

                    const PORT = 3000;
                    const server = http.createServer((req, res) => {
                        res.setHeader('Content-Type', 'application/json');
                        res.writeHead(200);
                        res.end(JSON.stringify({ status: "OK", message: "Node.js REST API lista en DevStudio" }));
                    });

                    server.listen(PORT, () => {
                        console.log(`Servidor Node.js corriendo en puerto ${'$'}PORT`);
                    });
                """.trimIndent()
            ),
            ProjectFileEntity(
                projectId = projectId,
                name = "package.json",
                path = "/package.json",
                extension = "json",
                content = """
                    {
                      "name": "node-devstudio-api",
                      "version": "1.0.0",
                      "main": "server.js",
                      "scripts": {
                        "start": "node server.js"
                      }
                    }
                """.trimIndent()
            )
        )
    }
}
