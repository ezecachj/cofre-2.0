# Cofre Galáctico 2.0 — Forge 1.20.1 (versión de prueba integrada)

## Incluye
- Bloque galáctico con texturas nuevas en laterales, tapa y base.
- Almacenamiento por tipo y NBT, con contador interno de hasta 2.000.000.000 por tipo y 4.096 tipos diferentes.
- Inventario persistente en NBT; migración de los objetos de la versión 0.1.0 al abrir un mundo existente.
- Interfaz propia: lista paginada (45 tipos por página), buscador, extracción con clic, depósito con Shift+clic y botón Depositar todo.
- Mesa 3×3 y resultado de recetas vanilla.
- Botón «Fabricar del cofre»: la cuadrícula actúa como plantilla y consume ingredientes directamente del almacén.
- Sin bloque terminal adicional. Receta: 1 cofre + 8 vidrios.

## Límites / advertencias
- **NO COMPILADO NI PROBADO EN MINECRAFT EN ESTE ENTORNO.** GitHub Actions debe compilarlo; podrían necesitarse correcciones de API Forge.
- La cuadrícula de recetas se rellena con objetos del inventario del jugador como plantilla. «Fabricar del cofre» consume los ingredientes del cofre, no los de la plantilla.
- La GUI todavía no muestra los contadores exactos por encima de 64, aunque el almacenamiento interno sí conserva cantidades mayores.
- No se ha implementado galaxia animada ni libro de recetas integrado.
- Romper un cofre con cantidades enormes arroja todos los objetos al suelo: puede causar lag. **No rompas cofres cargados masivamente.**
- Guardá copia de seguridad del mundo antes de probar.

## Instalación
Subir los archivos de este proyecto a la raíz del repositorio GitHub y ejecutar Actions > Compilar Cofre Galactico. Descargar el artifact ZIP y extraer el JAR en `.minecraft/mods`. El cliente y servidor deben tener el mismo mod.
