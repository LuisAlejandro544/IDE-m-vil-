# 🗺️ DevStudio - Roadmap Técnico

## Próximo Gran Hito: Integración del Módulo React ⚛️

### Concepto del Módulo:
- **Descarga Bajo Demanda**: Para preservar el tamaño reducido del APK principal de DevStudio, el módulo de React se descargará de manera opcional/separada.
- **Componentes Incluidos**:
  1. `react.production.min.js` (Core de React)
  2. `react-dom.production.min.js` (ReactDOM)
  3. `babel.min.js` (Soporte JSX / Babel Standalone para transpilar `.jsx` y `.tsx` directamente en el navegador / WebView)

### Ventajas:
- **Cero sobrepeso en la APK base**.
- **Soporte completo para componentes React, Hooks y JSX**.
- **Sincronización con GitHub Releases** para obtener actualizaciones del bundle de React.
