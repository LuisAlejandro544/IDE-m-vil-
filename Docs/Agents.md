# 📋 Guía y Convenciones para Agentes de Código

Este documento especifica las pautas de estilo de código, flujos de trabajo y estándares para cualquier Agente de IA o desarrollador.

---

## 🎨 Estilo de Código y UI

- **Material Design 3**: Utiliza exclusivamente componentes M3 (`androidx.compose.material3`).
- **Paleta Oscura Cómoda**: La interfaz utiliza colores oscuros suaves (`#121318`, `#1A1C23`, `#2D303E`) evitando el contraste excesivo o brillante para cuidar la vista del usuario en sesiones prolongadas.
- **Iconos**: Utilizar `Icons.Default` o `Icons.AutoMirrored` para elementos de navegación.

---

## 🔧 Compilación y Verificación

- Antes de finalizar cualquier turno o entregar cambios, ejecuta la verificación con `compile_applet`.
- No alteres las versiones principales de Gradle ni plugins sin necesidad estricta.
