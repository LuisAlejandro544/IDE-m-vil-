# 🗺️ DevStudio - Roadmap de Desarrollo

Este documento detalla el plan de evolución técnica de DevStudio, destacando las fases completadas y las próximas metas de integración de tecnologías.

---

## 🟢 Fase 1: Núcleo e Infraestructura (Completado)
- [x] **Arquitectura Políglota**: Soporte nativo para proyectos Web (HTML, CSS, JS), Kotlin y Rust.
- [x] **Editor de Código Avanzado**: Resaltado de sintaxis con `SyntaxHighlighter` y soporte para archivos `.md` con renderizado en tiempo real.
- [x] **Agente de IA Integrado**: Integración con **Google Gemini** y **OpenRouter AI**.
- [x] **Sistema de Herramientas (Tool Calling)**: Ejecución de comandos de archivos (`create_file`, `edit_file`, `delete_file`, `get_project_structure`) con buffers acumulativos streaming.
- [x] **Gestión de Archivos con Room DB**: Estructura jerárquica con `parentPath` e `isDirectory`.

---

## 🚀 Próximo Paso Prioritario: Módulo React ⚛️ (En Desarrollo / Próxima Fase)
- [ ] **Soporte para Módulo Descargable / CDN de React**:
  - Permitir al usuario habilitar o descargar el entorno ligero de **React 18 + ReactDOM + Babel Standalone**.
  - Mantener la aplicación liviana (bajo tamaño de APK) descargando los binarios/scripts de React bajo demanda.
- [ ] **Renderizado de JSX en WebView / Servidor Local Rust**:
  - Compilación e interpretación en tiempo real de componentes `.jsx` y `.tsx`.
  - Inyección dinámica de bibliotecas React UMD/CDN en la vista previa del proyecto.
- [ ] **Generador de Plantillas React**:
  - Habilidad para que el Agente de IA genere proyectos de React basados en componentes funcionales, hooks (`useState`, `useEffect`) y Tailwind CSS.

---

## 🔮 Fase 3: Futuras Integraciones (Planificadas)
- [ ] Soporte para Frameworks CSS (Tailwind CDN / Bootstrap).
- [ ] Integración con TypeScript nativo en navegador/WebView.
- [ ] Exportación directa de proyectos como ZIP y sincronización con GitHub Releases.
