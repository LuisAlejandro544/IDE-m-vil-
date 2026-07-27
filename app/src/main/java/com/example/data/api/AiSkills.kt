package com.example.data.api

object AiSkills {

    val TOOL_USAGE_SKILL = """
        🛠️ [SKILL: GUÍA DE USO DE HERRAMIENTAS Y RESTRICCIONES]
        - Usar `get_project_structure` para explorar la jerarquía de carpetas.
        - Usar `read_file` para inspeccionar el contenido de un archivo antes de modificarlo.
        - Usar `edit_file` para ediciones quirúrgicas de líneas específicas. 
          ⛔ STIRCTAMENTE PROHIBIDO: Reescribir un archivo completo usando `edit_file`. `target_content` debe ser únicamente el bloque exacto a cambiar.
        - Usar `create_file` para crear nuevos archivos con su contenido inicial.
        - Usar `delete_file` únicamente para eliminar un archivo o carpeta.
    """.trimIndent()

    val DESIGN_SKILL = """
        🎯 [SKILL: UI/UX DESIGN & ESTÉTICA MODERNA]
        - Aplica principios de Material Design 3, jerarquía visual clara y generoso espacio negativo.
        - Usa paletas de colores coherentes (fondos oscuros elegantes #121318 / #1A1C23 o temas claros limpios, acentos vibrantes).
        - Tipografía profesional, tamaños proporcionales (rem/sp) y alto contraste para legibilidad.
        - Bordes redondeados (border-radius: 12px-16px), sombras suaves (box-shadow) y micro-interacciones (hover, active, transitions 0.2s ease).
        - Diseños pulidos que evitan la apariencia genérica o anticuada.
    """.trimIndent()

    val RESPONSIVE_SKILL = """
        📱 [SKILL: ADAPTABILIDAD & RESPONSIVE DESIGN]
        - Diseña pensando primero en móvil (Mobile-First) con adaptación fluida a tablets y pantallas anchas.
        - Usa Flexbox (`display: flex; flex-wrap: wrap`) y CSS Grid (`grid-template-columns: repeat(auto-fit, minmax(280px, 1fr))`).
        - Implementa Media Queries dinámicas (`@media (max-width: 768px)`, etc.) y unidades adaptativas (`vw`, `vh`, `clamp()`).
        - Garantiza la ausencia de desbordamientos horizontales incomodos (`overflow-x: hidden`, `max-width: 100%`).
        - Elementos táctiles accesibles con tamaño mínimo de clic de 48px / 48dp.
    """.trimIndent()

    val LOGIC_SKILL = """
        ⚡ [SKILL: LÓGICA ROBUSTA & CLEAN CODE]
        - Aplica Principio de Responsabilidad Única (SRP), modularidad y código limpio.
        - Implementa manejo explícito de errores (`try-catch`, estados de error visuales y retroalimentación al usuario).
        - Manipulación segura del DOM y datos con JavaScript ES6+ (`const/let`, arrow functions, `async/await`, destructuring).
        - Validación rigurosa de entradas de usuario antes de procesar o guardar información.
        - Evita variables globales innecesarias y fugas de memoria; optimiza algoritmos para máxima fluidez.
    """.trimIndent()

    fun getAllSkillsSystemPrompt(): String {
        return """
            🚀 HABILIDADES ESPECIALIZADAS ACTIVAS (SKILLS MD DETRÁS DE ESCENA):
            
            $TOOL_USAGE_SKILL
            
            $DESIGN_SKILL
            
            $RESPONSIVE_SKILL
            
            $LOGIC_SKILL
        """.trimIndent()
    }
}
