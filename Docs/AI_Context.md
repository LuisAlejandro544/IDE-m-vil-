# 🤖 Manual de Contexto y Arquitectura para Agentes IA

Este documento sirve como manual de incorporación para cualquier Agente de Inteligencia Artificial que trabaje o contribuya a este repositorio.

---

## 💡 Principios de Arquitectura

1. **Estructura Polyglot**:
   - **Kotlin + Jetpack Compose**: Toda la interfaz gráfica de usuario y la lógica de estado (ViewModel/StateFlow) residen en Kotlin.
   - **C++ Native Core**: El directorio `app/src/main/cpp/` contiene librerías compartidas C++ invocadas mediante JNI (`CppEngine.kt`).
   - **Rust Embedded HTTP Server**: El directorio `app/src/main/rust/devstudio_server/` aloja el servidor de pruebas HTTP en Rust invocado desde `RustHttpServer.kt`.

2. **Servidor HTTP Localhost y Vista Previa**:
   - El servidor de vista previa corre en `http://127.0.0.1:8080`.
   - `android:usesCleartextTraffic="true"` debe permanecer activo en `AndroidManifest.xml` para permitir conexiones HTTP locales sin TLS en el WebView.

3. **Persistencia**:
   - Todos los datos de archivos del usuario y mensajes del chat se gestionan con **Room Database** (`AppDatabase.kt`).
   - Evitar manipulaciones directas de archivos en disco sin pasar por `ProjectFileDao` / `IdeRepository`.

---

## ⚠️ Reglas Importantes para la Modificación de Código

- **Conservar Signaturas JNI**: Si modificas funciones nativas en `devstudio_cpp.cpp` o `lib.rs`, actualiza de manera idéntica los métodos `external fun` en `CppEngine.kt` y `RustHttpServer.kt`.
- **Compatibilidad con Compose**: Mantiene la reactividad utilizando `StateFlow` y `collectAsStateWithLifecycle()`.
- **Manejo de Errores Robustos**: El servidor HTTP nativo debe contar con fallback para servir los archivos del proyecto incluso si las librerías dinámicas `.so` están en proceso de recompilación.
