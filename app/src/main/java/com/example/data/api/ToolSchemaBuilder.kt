package com.example.data.api

import org.json.JSONArray
import org.json.JSONObject

object ToolSchemaBuilder {

    fun buildGeminiToolsJson(): JSONArray {
        return JSONArray().apply {
            put(JSONObject().apply {
                put("functionDeclarations", JSONArray().apply {
                    put(JSONObject().apply {
                        put("name", "get_project_structure")
                        put("description", "Obtiene el árbol completo de carpetas y archivos en el proyecto")
                    })
                    put(JSONObject().apply {
                        put("name", "read_file")
                        put("description", "Lee el contenido de un archivo del proyecto")
                        put("parameters", JSONObject().apply {
                            put("type", "OBJECT")
                            put("properties", JSONObject().apply {
                                put("path", JSONObject().put("type", "STRING").put("description", "Ruta del archivo, ej: /index.html"))
                            })
                            put("required", JSONArray().put("path"))
                        })
                    })
                    put(JSONObject().apply {
                        put("name", "edit_file")
                        put("description", "Edita un archivo reemplazando target_content por replacement_content")
                        put("parameters", JSONObject().apply {
                            put("type", "OBJECT")
                            put("properties", JSONObject().apply {
                                put("path", JSONObject().put("type", "STRING").put("description", "Ruta del archivo"))
                                put("target_content", JSONObject().put("type", "STRING").put("description", "Texto exacto a reemplazar"))
                                put("replacement_content", JSONObject().put("type", "STRING").put("description", "Nuevo texto reemplazo"))
                            })
                            put("required", JSONArray().put("path").put("target_content").put("replacement_content"))
                        })
                    })
                    put(JSONObject().apply {
                        put("name", "create_file")
                        put("description", "Crea un nuevo archivo con el contenido dado")
                        put("parameters", JSONObject().apply {
                            put("type", "OBJECT")
                            put("properties", JSONObject().apply {
                                put("path", JSONObject().put("type", "STRING").put("description", "Ruta del nuevo archivo"))
                                put("content", JSONObject().put("type", "STRING").put("description", "Contenido inicial del archivo"))
                            })
                            put("required", JSONArray().put("path").put("content"))
                        })
                    })
                    put(JSONObject().apply {
                        put("name", "delete_file")
                        put("description", "Elimina un archivo o carpeta")
                        put("parameters", JSONObject().apply {
                            put("type", "OBJECT")
                            put("properties", JSONObject().apply {
                                put("path", JSONObject().put("type", "STRING").put("description", "Ruta a eliminar"))
                            })
                            put("required", JSONArray().put("path"))
                        })
                    })
                    put(JSONObject().apply {
                        put("name", "get_diagnostics")
                        put("description", "Obtiene los logs y errores de sintaxis del Linter y Consola de Diagnóstico en Vivo")
                    })
                })
            })
        }
    }

    fun buildOpenRouterToolsJson(): JSONArray {
        return JSONArray().apply {
            put(JSONObject().apply {
                put("type", "function")
                put("function", JSONObject().apply {
                    put("name", "get_project_structure")
                    put("description", "Obtiene la lista de todos los archivos y carpetas del proyecto")
                })
            })
            put(JSONObject().apply {
                put("type", "function")
                put("function", JSONObject().apply {
                    put("name", "read_file")
                    put("description", "Lee el contenido de un archivo del proyecto")
                    put("parameters", JSONObject().apply {
                        put("type", "object")
                        put("properties", JSONObject().apply {
                            put("path", JSONObject().put("type", "string"))
                        })
                        put("required", JSONArray().put("path"))
                    })
                })
            })
            put(JSONObject().apply {
                put("type", "function")
                put("function", JSONObject().apply {
                    put("name", "edit_file")
                    put("description", "Edita un archivo reemplazando target_content por replacement_content")
                    put("parameters", JSONObject().apply {
                        put("type", "object")
                        put("properties", JSONObject().apply {
                            put("path", JSONObject().put("type", "string"))
                            put("target_content", JSONObject().put("type", "string"))
                            put("replacement_content", JSONObject().put("type", "string"))
                        })
                        put("required", JSONArray().put("path").put("target_content").put("replacement_content"))
                    })
                })
            })
            put(JSONObject().apply {
                put("type", "function")
                put("function", JSONObject().apply {
                    put("name", "create_file")
                    put("description", "Crea un nuevo archivo con contenido")
                    put("parameters", JSONObject().apply {
                        put("type", "object")
                        put("properties", JSONObject().apply {
                            put("path", JSONObject().put("type", "string"))
                            put("content", JSONObject().put("type", "string"))
                        })
                        put("required", JSONArray().put("path").put("content"))
                    })
                })
            })
            put(JSONObject().apply {
                put("type", "function")
                put("function", JSONObject().apply {
                    put("name", "delete_file")
                    put("description", "Elimina un archivo o carpeta")
                    put("parameters", JSONObject().apply {
                        put("type", "object")
                        put("properties", JSONObject().apply {
                            put("path", JSONObject().put("type", "string"))
                        })
                        put("required", JSONArray().put("path"))
                    })
                })
            })
            put(JSONObject().apply {
                put("type", "function")
                put("function", JSONObject().apply {
                    put("name", "get_diagnostics")
                    put("description", "Obtiene los logs y errores del Linter y Consola de Diagnóstico en Vivo")
                })
            })
        }
    }
}
