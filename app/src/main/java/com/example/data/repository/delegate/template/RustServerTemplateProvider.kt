package com.example.data.repository.delegate.template

import com.example.data.db.ProjectFileEntity

object RustServerTemplateProvider {
    fun getFiles(projectId: Long): List<ProjectFileEntity> {
        return listOf(
            ProjectFileEntity(
                projectId = projectId,
                name = "main.rs",
                path = "/src/main.rs",
                extension = "rs",
                parentPath = "/src",
                content = """
                    use std::io::Write;
                    use std::net::TcpListener;

                    fn main() {
                        let listener = TcpListener::bind("127.0.0.1:8080").unwrap();
                        println!("Servidor Rust escuchando en http://127.0.0.1:8080");

                        for stream in listener.incoming() {
                            let mut stream = stream.unwrap();
                            let response = "HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\n\r\n¡Servidor Rust DevStudio Activo!";
                            stream.write_all(response.as_bytes()).unwrap();
                        }
                    }
                """.trimIndent()
            ),
            ProjectFileEntity(
                projectId = projectId,
                name = "Cargo.toml",
                path = "/Cargo.toml",
                extension = "toml",
                content = """
                    [package]
                    name = "rust_devstudio_server"
                    version = "0.1.0"
                    edition = "2021"

                    [dependencies]
                """.trimIndent()
            )
        )
    }
}
