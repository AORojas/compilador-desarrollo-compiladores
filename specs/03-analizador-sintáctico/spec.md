# Especificación Técnica: Analizador Sintáctico (Gramática y Parser)

## 1. Objetivo y Alcance

El Analizador Sintáctico tiene como única responsabilidad validar que la estructura del código fuente cumpla estrictamente con la **gramática oficial de GAUCHO**. 

**Límites estrictos:**
* No valida variables ni ámbitos (eso es tarea del Analizador Semántico).
* No genera código intermedio (Polaca).

El parser valida la entrada según la gramática y ejecuta las acciones CUP definidas por la integración del AST en `specs/04-arbol-sintáctico/spec.md`. La construcción y el contrato del árbol pertenecen a esa especificación; no son validaciones gramaticales.

---

## 2. Contexto del Proyecto y Referencias

| Ruta / Archivo | Propósito |
| :--- | :--- |
| `specs/01-diseño/spec.md` | **Fuente única de verdad:** Define la gramática oficial (BNF), los tokens y las reglas del lenguaje. |
| `specs/02-analizador-lexico/spec.md` | **Referencia de Léxico:** Explica cómo funciona el analizador léxico. |
| `specs/04-arbol-sintáctico/spec.md` | **Referencia del AST:** Define las clases del árbol y las acciones CUP que lo construyen. |
| `Compilador/src/sintactico/` | Directorio de trabajo (`parser.cup`, `Parser.java`, `sym.java` y `LexerCupAdapter.java`). |

---

## 3. Integración con el Analizador Léxico

Para que Java CUP consuma los tokens del Léxico (que son códigos `int`), se utiliza la clase `LexerCupAdapter.java` dentro de `src/sintactico/`, la cual implementa la interfaz `java_cup.runtime.Scanner`.

*   **Mapeo:** Convierte el `int` devuelto por el léxico (ej: `256`) al terminal de CUP (`sym.ID`).
*   **Transporte de Datos:** Para los tokens `ID`, `CTE` y `LITERAL_TXT`, extrae el lexema (`String`) del contexto léxico y lo adjunta al objeto `Symbol`.
*   **Ubicación:** Adjunta la línea y columna al `Symbol` para reportar errores.
*   **EOF:** Al recibir el fin de archivo (`-1`), devuelve el token `sym.EOF`.

### 3.1. Configuración Base de Java CUP
El encabezado de `parser.cup` define el paquete `sintactico`, importa las clases del paquete runtime de Java CUP y configura el mecanismo de escaneo para que invoque el método de obtención del siguiente token del escáner configurado cada vez que requiera avanzar en la lectura.

---

## 4. Implementación de la Gramática en CUP

La gramática BNF de `specs/01-diseño/spec.md` es la única fuente de verdad para reglas y precedencia. `Compilador/src/sintactico/parser.cup` implementa esa gramática en Java CUP y declara los terminales asociados a los códigos de token definidos por el diseño; esta especificación no vuelve a copiar esas listas para evitar divergencias.

Las acciones de reducción que construyen valores `NodoAST` siguen el contrato y los operadores definidos en `specs/04-arbol-sintáctico/spec.md`. El paquete sintáctico se encarga de aceptar/rechazar secuencias y recuperarse de errores gramaticales; las acciones de construcción no agregan validaciones semánticas.

`Parser.java` y `sym.java` son artefactos generados desde `parser.cup` y deben mantenerse sincronizados regenerándolos con Java CUP.

---

## 5. Manejo de Errores Sintácticos
El Sintáctico reporta errores de gramática (código E3) utilizando el token `error` de CUP para recuperarse sin abortar la compilación completa.

* **Recuperación:** Ante una sentencia mal formada, el parser descarta tokens hasta consumir el siguiente `fin_lineas` y retoma el análisis en la línea posterior.
