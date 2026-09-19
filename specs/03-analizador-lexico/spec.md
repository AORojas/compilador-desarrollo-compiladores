# Especificación de Diseño Arquitectónico y Técnico: Analizador Léxico GAUCHO

**Grupo:** Grupo D  
**Lenguaje de Implementación:** Java  
**Estado:** Cerrado para desarrollo de Fase Léxica  
**Paradigma:** Analizador Léxico Orientado a Tablas (Table-Driven Lexer) con Despachador de Acciones Semánticas.

---

## 1. Decisiones Globales y Alfabeto del Lenguaje

### 1.1 Decisiones Técnicas Fundamentales
*   **D1 (Tipo de Datos):** Único tipo compatible: **entero con signo de 32 bits** (Rango: `-2.147.483.648` a `2.147.483.647`).
*   **D2 (Finalización de Línea):** El salto de línea (`\n`) actúa como el token terminador de sentencias **No se utiliza el punto y coma (`;`) ni el retorno de carro (`\r`) como terminador.**
*   **D3 (Gestión de Comentarios):** Los comentarios delimitados por `/*` y `*/` son procesados y totalmente descartados por el analizador léxico, impidiendo que lleguen al parser.

### 1.2 Alfabeto Oficial
El léxico clasificará los caracteres del código fuente bajo las siguientes categorías algebraicas y sintácticas:
*   **L (Letra):** `a-z`, `A-Z`
*   **D (Dígito):** `0-9`
*   **SIM (Símbolos):** `+`, `-`, `*`, `/`, `(`, `)`, `{`, `}`, `<`, `>`, `=`, `,`, `"`
*   **BL (Blancos):** Espacio en blanco (` `), tabulador (`\t`) y retorno de carro (`\r`).
*   **SL (Salto de Línea):** Exclusivamente Line Feed (`\n`).
*   **OTRO:** Cualquier carácter no listado aquí (provoca Error Léxico `E1`).

---

## 2. Organización de Archivos y Proyecto (Estructura Java)

Para cumplir con la separación estricta de responsabilidades, el código del analizador léxico se modularizará dentro del proyecto de la siguiente manera:

```text
src/
└── compilador/
    └── lexico/
        ├── datos/
        │   ├── MatrizTransicion.java
        │   ├── MatrizTokens.java
        │   └── MatrizAcciones.java
        ├── semantica/
        │   └── GestorFuncionesSemanticas.java
        ├── modelo/
        │   ├── Token.java
        │   └── TipoToken.java
        ├── TablaSimbolos.java
        └── AnalizadorLexico.java
```

### Roles de los Archivos Centrales:
1.  **`AnalizadorLexico.java`**: Posee el lazo principal (`proximoToken()`). Lee caracteres, traduce el carácter a su ID de columna, consulta las tres matrices secuencialmente y controla el estado.
2.  **`GestorFuncionesSemanticas.java`**: Contiene los buffers independientes del ciclo de vida de los strings y expone métodos individuales (`ejecutarF1` a `ejecutarFE`) invocados por el componente central.
3.  **Paquete `datos/`**: Almacena de forma dura las matrices numéricas bidimensionales (`int[][]`). No procesa datos, solo responde consultas indexadas.

---

## 3. Clasificación de Caracteres a Columnas (Eventos)

El motor de control traducirá cada carácter leído del archivo en una columna numérica (**0 a 17**) mediante el siguiente mapeo lógico:

| ID Evento (Columna) | Caracteres Mapeados | Descripción Semántica |
| :---: | :--- | :--- |
| **0** | `a-z`, `A-Z` | Letra |
| **1** | `0-9` | Dígito |
| **2** | `=` | Signo igual |
| **3** | `<` | Signo menor |
| **4** | `>` | Signo mayor |
| **5** | `/` | Barra de división / Comentarios |
| **6** | `*` | Asterisco de multiplicación |
| **7** | `"` | Comilla de apertura/cierre de texto |
| **8** | `+` | Signo suma |
| **9** | `-` | Signo resta |
| **10** | `(` | Paréntesis izquierdo |
| **11** | `)` | Paréntesis derecho |
| **12** | `{` | Llave izquierda |
| **13** | `}` | Llave derecha |
| **14** | `,` | Coma divisoria |
| **15** | `\n` | Espacio blanco que actúa como delimitador sintáctico y terminador de sentencias. |
| **16** |` ` ,`\t`,`\r`| Espacios en blanco / Tabuladores / Retorno de carro (ignorado) |
| **17** | Cualquier otro | Carácter inválido (Fuera de alfabeto) |

---

## 4. Matrices del Autómata Finito

> **Nota de Implementación para Java:** El estado `-1` representa el **Estado Final (`EF`)** de aceptación o detención, lo que indica al lazo del analizador que debe interrumpir la lectura actual y emitir el token correspondiente.

### 4.1 Matriz de Transición de Estados (`MatrizTransicion.java`)

| Estado Actual | Letra | Dígito | = | < | > | / | * | " | + | - | ( | ) | { | } | , | LF | Espacio / Tab / CR | Otro |
| :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: |
| **E0** (Comienzo) | E1 | E2 | E3 | E5 | E8 | E10 | E16 | E13 | E14 | E15 | E17 | E18 | E19 | E20 | E21 | E22 | E0 | EF |
| **E1** (ID / Pal. Reservada) | E1 | E1 | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF |
| **E2** (CTE / Constante) | EF | E2 | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF |
| **E3** (ASIG) | EF | EF | E4 | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF |
| **E4** (IGUAL) | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF |
| **E5** (MENOR) | EF | EF | E7 | EF | E6 | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF |
| **E6** (DISTINTO) | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF |
| **E7** (MENOR O IGUAL) | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF |
| **E8** (MAYOR) | EF | EF | E9 | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF |
| **E9** (MAYOR O IGUAL) | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF |
| **E10** (DIVISION) | EF | EF | EF | EF | EF | EF | E11 | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF |
| **E11** (Comentario) | E11 | E11 | E11 | E11 | E11 | E11 | E12 | E11 | E11 | E11 | E11 | E11 | E11 | E11 | E11 | E11 | E11 | E11 |
| **E12** (Posible fin comentario) | E11 | E11 | E11 | E11 | E11 | E0 | E11 | E11 | E11 | E11 | E11 | E11 | E11 | E11 | E11 | E11 | E11 | E11 |
| **E13** (Literal de Texto) | E13 | E13 | E13 | E13 | E13 | E13 | E13 | EF | E13 | E13 | E13 | E13 | E13 | E13 | E13 | E13 | E13 | E13 |
| **E14** (SUMA) | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF |
| **E15** (RESTA) | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF |
| **E16** (MULTIPLICACION) | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF |
| **E17** (PAREN_IZQ) | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF |
| **E18** (PAREN_DER) | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF |
| **E19** (LLAVE_IZQ) | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF |
| **E20** (LLAVE_DER) | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF |
| **E21** (COMA) | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF |
| **E22** (FIN_DE_LINEA) | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF |
| **EF** (Salida / Error / Finalización) | - | - | - | - | - | - | - | - | - | - | - | - | - | - | - | - | - | - |

---

### 4.2 Matriz de Asignación de Tokens (`MatrizTokens.java`)

En esta matriz, cada celda indica el código del token que se devuelve cuando el autómata llega a un estado terminal con la clase de entrada indicada. Si la combinación no produce un token directamente, se usa `-1`.

| Estado Actual | Letra | Dígito | = | < | > | / | * | " | + | - | ( | ) | { | } | , | LF | Espacio / Tab / CR | Otro |
| :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: |
| **E0** (Comienzo) | -1 | -1 | -1 | -1 | -1 | -1 | -1 | -1 | -1 | -1 | -1 | -1 | -1 | -1 | -1 | -1 | -1 | -1 |
| **E1** (ID / Pal. Reservada) | -1 | -1 | 256 | 256 | 256 | 256 | 256 | 256 | 256 | 256 | 256 | 256 | 256 | 256 | 256 | 256 | 256 | 256 |
| **E2** (CTE / Constante) | 257 | -1 | 257 | 257 | 257 | 257 | 257 | 257 | 257 | 257 | 257 | 257 | 257 | 257 | 257 | 257 | 257 | 257 |
| **E3** (ASIG) | -1 | -1 | -1 | 269 | 269 | 269 | 269 | 269 | 269 | 269 | 269 | 269 | 269 | 269 | 269 | 269 | 269 | 269 |
| **E4** (IGUAL) | 270 | 270 | 270 | 270 | 270 | 270 | 270 | 270 | 270 | 270 | 270 | 270 | 270 | 270 | 270 | 270 | 270 | 270 |
| **E5** (MENOR) | 272 | 272 | 273 | 272 | 271 | 272 | 272 | 272 | 272 | 272 | 272 | 272 | 272 | 272 | 272 | 272 | 272 | 272 |
| **E6** (DISTINTO) | 271 | 271 | 271 | 271 | 271 | 271 | 271 | 271 | 271 | 271 | 271 | 271 | 271 | 271 | 271 | 271 | 271 | 271 |
| **E7** (MENOR O IGUAL) | 273 | 273 | 273 | 273 | 273 | 273 | 273 | 273 | 273 | 273 | 273 | 273 | 273 | 273 | 273 | 273 | 273 | 273 |
| **E8** (MAYOR) | 274 | 274 | 275 | 274 | 274 | 274 | 274 | 274 | 274 | 274 | 274 | 274 | 274 | 274 | 274 | 274 | 274 | 274 |
| **E9** (MAYOR O IGUAL) | 275 | 275 | 275 | 275 | 275 | 275 | 275 | 275 | 275 | 275 | 275 | 275 | 275 | 275 | 275 | 275 | 275 | 275 |
| **E10** (DIVISION) | 280 | 280 | 280 | 280 | 280 | -1 | 280 | 280 | 280 | 280 | 280 | 280 | 280 | 280 | 280 | 280 | 280 | 280 |
| **E11** (Comentario) | -1 | -1 | -1 | -1 | -1 | -1 | -1 | -1 | -1 | -1 | -1 | -1 | -1 | -1 | -1 | -1 | -1 | -1 |
| **E12** (Posible fin comentario) | -1 | -1 | -1 | -1 | -1 | -1 | -1 | -1 | -1 | -1 | -1 | -1 | -1 | -1 | -1 | -1 | -1 | -1 |
| **E13** (Literal de Texto) | -1 | -1 | -1 | -1 | -1 | -1 | -1 | 258 | -1 | -1 | -1 | -1 | -1 | -1 | -1 | -1 | -1 | -1 |
| **E14** (SUMA) | 277 | 277 | 277 | 277 | 277 | 277 | 277 | 277 | 277 | 277 | 277 | 277 | 277 | 277 | 277 | 277 | 277 | 277 |
| **E15** (RESTA) | 278 | 278 | 278 | 278 | 278 | 278 | 278 | 278 | 278 | 278 | 278 | 278 | 278 | 278 | 278 | 278 | 278 | 278 |
| **E16** (MULTIPLICACION) | 279 | 279 | 279 | 279 | 279 | 279 | 279 | 279 | 279 | 279 | 279 | 279 | 279 | 279 | 279 | 279 | 279 | 279 |
| **E17** (PAREN_IZQ) | 281 | 281 | 281 | 281 | 281 | 281 | 281 | 281 | 281 | 281 | 281 | 281 | 281 | 281 | 281 | 281 | 281 | 281 |
| **E18** (PAREN_DER) | 282 | 282 | 282 | 282 | 282 | 282 | 282 | 282 | 282 | 282 | 282 | 282 | 282 | 282 | 282 | 282 | 282 | 282 |
| **E19** (LLAVE_IZQ) | 283 | 283 | 283 | 283 | 283 | 283 | 283 | 283 | 283 | 283 | 283 | 283 | 283 | 283 | 283 | 283 | 283 | 283 |
| **E20** (LLAVE_DER) | 284 | 284 | 284 | 284 | 284 | 284 | 284 | 284 | 284 | 284 | 284 | 284 | 284 | 284 | 284 | 284 | 284 | 284 |
| **E21** (COMA) | 285 | 285 | 285 | 285 | 285 | 285 | 285 | 285 | 285 | 285 | 285 | 285 | 285 | 285 | 285 | 285 | 285 | 285 |
| **E22** (FIN_DE_LINEA) | 286 | 286 | 286 | 286 | 286 | 286 | 286 | 286 | 286 | 286 | 286 | 286 | 286 | 286 | 286 | -1 | 286 | 286 |
| **EF** (Final / Aceptación) | - | - | - | - | - | - | - | - | - | - | - | - | - | - | - | - | - | - |

---

## 4.3 Matriz de Identificadores de Funciones Semánticas (`MatrizAcciones.java`)

Mapea un ID numérico correlativo asignado a cada función semántica calculada según la combinación activa del analizador.

*   `1` -> **F1** | `2` -> **F2** | `3` -> **F3** | `4` -> **F4** | `5` -> **F5** | `6` -> **F6** | `7` -> **F7**
*   `8` -> **F8** | `9` -> **F9** | `10` -> **F10** | `11` -> **F11** | `12` -> **F12** | `13` -> **FE**

| Estado | Letra (0) | Dígito (1) | = (2) | < (3) | > (4) | / (5) | * (6) | " (7) | + (8) | - (9) | ( (10) | ) (11) | { (12) | } (13) | , (14) | LF (15) | BL (16) | Otro (17) |
| :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: |
| **E0** | 1 | 2 | 1 | 1 | 1 | 1 | 1 | 3 | 1 | 1 | 1 | 1 | 1 | 1 | 1 | 8 | 4 | 13 |
| **E1** | 5 | 5 | 7 | 7 | 7 | 7 | 7 | 7 | 7 | 7 | 7 | 7 | 7 | 7 | 7 | 7 | 7 | 7 |
| **E2** | 13| 6 | 9 | 9 | 9 | 9 | 9 | 9 | 9 | 9 | 9 | 9 | 9 | 9 | 9 | 9 | 9 | 9 |
| **E3** | 10| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10|
| **E4** | 10| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10|
| **E5** | 10| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10|
| **E6** | 10| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10|
| **E7** | 10| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10|
| **E8** | 10| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10|
| **E9** | 10| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10|
| **E10**| 10| 10| 10| 10| 10| 10| 12| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10|
| **E11**| 12| 12| 12| 12| 12| 12| 12| 12| 12| 12| 12| 12| 12| 12| 12| 12| 12| 12|
| **E12**| 12| 12| 12| 12| 12| 12| 12| 12| 12| 12| 12| 12| 12| 12| 12| 12| 12| 12|
| **E13**| 3 | 3 | 3 | 3 | 3 | 3 | 3 | 11| 3 | 3 | 3 | 3 | 3 | 3 | 3 | 3 | 3 | 3 |
| **E14**| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10|
| **E15**| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10|
| **E16**| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10|
| **E17**| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10|
| **E18**| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10|
| **E19**| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10|
| **E20**| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10|
| **E21**| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10| 10|
| **E22**| 8 | 8 | 8 | 8 | 8 | 8 | 8 | 8 | 8 | 8 | 8 | 8 | 8 | 8 | 8 | 8 | 8 | 8 |

---

## 5. Especificación Completa de Funciones Semánticas

Las trece acciones semánticas se implementarán dentro de `GestorFuncionesSemanticas.java`. Las variables internas de buffer se definen estrictamente separadas:

```java
private StringBuilder bufferLetras = new StringBuilder();
private StringBuilder bufferDigitos = new StringBuilder();
private StringBuilder bufferTexto = new StringBuilder();
```

*   **F1 (Inicializar Letras):** Limpia completamente el `bufferLetras` (`setLength(0)`) e introduce el carácter actual recibido.
*   **F2 (Inicializar Dígitos):** Limpia por completo el `bufferDigitos`. **No comparte espacio con letras para evitar colisiones.** Almacena el primer dígito numérico leído.
*   **F3 (Abrir/Acumular Cuerpo Texto):** Si el estado actual es `E0`, limpia el `bufferTexto` y descarta la comilla inicial. Si el estado es `E13`, concatena el carácter recibido al cuerpo del literal de texto.
*   **F4 (Ignorar Caracteres Blancos):** Omite la acumulación de datos en buffers. Mantiene la ejecución limpia y previene la alteración de lexemas válidos debido a espacios, tabuladores o retornos de carro (`
`).
*   **F5 (Acumular Letras):** Adiciona el carácter actual (letra o número) al final de la secuencia en ejecución dentro de `bufferLetras`.
*   **F6 (Acumular Dígitos):** Adiciona el dígito actual al final del `bufferDigitos`.
*   **F7 (Retornar ID o Palabra Reservada):** 
    1. Llama al método de relectura (`unread()`) del lector del archivo para devolver el carácter delimitador consumido de más.
    2. Extrae la cadena de `bufferLetras`.
    3. Evalúa si el lexema coincide con alguna palabra reservada oficial: `principal`, `entero`, `si`, `bucle`, `hasta`, `mostrar`, `mostrarTexto`, `y`, `o`, `retornar`.
    4. Si coincide, extrae el token preestablecido (259 al 268). Si no coincide, es un identificador general (`ID`), asigna el ID `256` y lo registra de forma persistente dentro de la `TablaSimbolos`.
*   **F8 (Retornar Fin de Línea):** Incrementa en una unidad el contador de líneas físico del analizador léxico. Prepara la estructura para emitir el token de control `286` (`FIN_DE_LINEA`).
*   **F9 (Retornar Constante Entera):**
    1. Ejecuta el método `unread()` para devolver el delimitador consumido.
    2. Convierte el valor String de `bufferDigitos` a un número de tipo primitivo `long` para evaluar desbordamiento.
    3. **Validación Semántica:** Si el valor está fuera del rango de 32 bits firmado (`-2147483648` a `2147483647`), interrumpe la entrega normal del token y levanta el código de error `E2` (Constante numérica mal formada o fuera de límite).
    4. Si pasa la prueba, lo registra en la `TablaSimbolos` bajo el token genérico `257`.
*   **F10 (Retornar Token Directo):** No requiere alteración o lectura de buffers intermedios. Resuelve operadores matemáticos de un solo carácter o construcciones lógicas completas y devuelve su código directo.
*   **F11 (Cerrar Literal):** Descarta la comilla de cierre de la cadena. Almacena la cadena textual limpia extraída de `bufferTexto` en la `TablaSimbolos` devolviendo el identificador `258`.
*   **F12 (Gestionar Bloque Comentario):** Si la secuencia de entrada coincide con los operadores de apertura `/*`, cambia el flujo e ignora todo elemento secuencial posterior hasta emparejar con el delimitador de salida `*/`. Restablece el autómata al estado base `E0` de forma transparente.
*   **FE (Error Léxico):** Detiene el proceso secuencial en curso, extrae la línea física actual mediante el rastreador del compilador, añade un reporte detallado con el código de error `E1` y obliga a la máquina a saltar a `EF` para activar la estrategia de recuperación de pánico.

---

## 6. Lógica de Despacho y Motor de Control

El esqueleto del método principal dentro de `AnalizadorLexico.java` implementará el procesamiento dinámico orientado a tablas mediante la siguiente lógica algorítmica estructurada:

```java
public class AnalizadorLexico {
   
    public static final int EF = -1; 
    
    private int estadoActual = 0; // E0 (Estado inicial de espera)
    private GestorFuncionesSemanticas gestorSemantico;
    private CustomReader lector; // Flujo que soporta la operación unread()

    public Token proximoToken() throws IOException {
        estadoActual = 0; // Se reinicia a E0 para comenzar a buscar el siguiente token
        char caracterActual;
        
        // El lazo se controla directamente usando la abstracción de la constante
        while (estadoActual != EF) { 
            int codigoLeido = lector.read();
            if (codigoLeido == -1) { 
                return new Token(276, "EOF"); 
            }
            caracterActual = (char) codigoLeido;
            
            // 1. Obtener la columna (evento) asociada al carácter (0 a 17)
            int idEvento = Clasificador.obtenerColumna(caracterActual);
            
            // 2. Recuperar y despachar la acción semántica mapeada (F1 a FE)
            int idAccion = MatrizAcciones.getAccionSemantica(estadoActual, idEvento);
            gestorSemantico.ejecutarAccion(idAccion, caracterActual);
            
            // 3. Transicionar al siguiente estado de la máquina
            int nuevoEstado = MatrizTransicion.getSiguienteEstado(estadoActual, idEvento);
            
            // Evaluamos la transición directamente con la constante estipulada
            if (nuevoEstado == EF) {
                // Se alcanzó el corte por aceptación. Resolver Token final.
                int idToken = MatrizTokens.getTokenAsociado(estadoActual, idEvento);
                
                if (idToken == -1) {
                    // Si es una combinación inválida de la matriz de tokens,
                    // se reestabiliza el motor en E0 y se continúa la lectura.
                    estadoActual = 0; 
                    continue;
                }
                
                // Recuperar el contenido final limpio acumulado en el gestor
                String lexema = gestorSemantico.obtenerLexemaActual(idToken);
                return new Token(idToken, lexema);
            }
            
            // Si la máquina no terminó, progresa al siguiente estado intermedio
            estadoActual = nuevoEstado;
        }
        return null;
    }
}
```