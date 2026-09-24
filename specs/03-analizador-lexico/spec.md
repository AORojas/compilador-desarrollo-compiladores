# Especificación de Diseño Arquitectónico y Técnico: Analizador Léxico GAUCHO

**Grupo:** Grupo D  
**Lenguaje de Implementación:** Java  
**Estado:** Cerrado para desarrollo de Fase Léxica  
**Paradigma:** Analizador Léxico Orientado a Tablas (Table-Driven Lexer) con Despachador de Acciones Semánticas.

---

## 1. Decisiones Globales y Alfabeto del Lenguaje

### 1.1 Decisiones Técnicas Fundamentales
*   **D1 (Tipo de Datos):** Único tipo compatible: **entero con signo de 32 bits** (Rango: `-2.147.483.648` a `2.147.483.647`).
*   **D2 (Finalización de Línea):** El salto de línea (`\n`) actúa como el token terminador de sentencias. No se utiliza el punto y coma (`;`) ni el retorno de carro (`\r`) como terminador.
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

```text
src/
└── main/
    ├── java/
    |   ├── compilador/
    |   |   └── lexico/
    |   |       ├── datos/
    |   |       |   ├── Clasificador.java
    |   |       │   ├── MatrizAcciones.java
    |   |       │   ├── MatrizTokens.java
    |   |       │   └── MatrizTransicion.java
    |   |       ├── modelo/
    |   |       │   ├── TipoToken.java
    |   |       │   └── Token.java
    |   |       ├── semantica/
    |   |       │   └── GestorFuncionesSemanticas.java
    |   |       ├── AnalizadorLexico.java
    |   |       └── TablaSimbolos.java
    |   └── Main.java
    └── resources/
        └── prueba.gau
```

### Roles de los Archivos Centrales:
1.  **`AnalizadorLexico.java`**: posee el lazo principal (`proximoToken()`). Lee caracteres, traduce el carácter a su ID de columna, consulta la matriz de transición y la matriz de acciones, y devuelve el siguiente token.
2.  **`GestorFuncionesSemanticas.java`**: contiene los buffers y las funciones semánticas auxiliares que modifican el estado del lexema.
3.  **Paquete `datos/`**: almacena las matrices definitorias del lenguaje y del flujo del autómata.

> En la implementación actual, además de la transición, la matriz de transición centraliza la decisión de flujo mediante `resolver(...)`, manteniendo `proximoToken()` reducido a una secuencia de llamadas y control del ciclo principal.

---

## 3. Clasificación de Caracteres a Columnas (Eventos)

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
| **15** | `\n` | Salto de línea |
| **16** | ` `, `\t`, `\r` | Espacios en blanco / tabulador / retorno de carro |
| **17** | Cualquier otro | Carácter inválido |

---

## 4. Matrices del Autómata Finito

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

### 4.2 Matriz de Asignación de Tokens (`MatrizTokens.java`)

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

### 4.3 Matriz de Identificadores de Funciones Semánticas (`MatrizAcciones.java`)

*   `1` -> **F1** | `2` -> **F2** | `3` -> **F3** | `4` -> **F4** | `5` -> **F5** | `6` -> **F6** | `7` -> **F7**
*   `8` -> **F8** | `9` -> **F9** | `10` -> **F10** | `11` -> **F11** | `12` -> **F12** | `13` -> **FE**

| Estado | Letra (0) | Dígito (1) | = (2) | < (3) | > (4) | / (5) | * (6) | " (7) | + (8) | - (9) | ( (10) | ) (11) | { (12) | } (13) | , (14) | LF (15) | BL (16) | Otro (17) |
| :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: |
| **E0** | 1 | 2 | 1 | 1 | 1 | 1 | 1 | 3 | 1 | 1 | 1 | 1 | 1 | 1 | 1 | 8 | 4 | 13 |
| **E1** | 5 | 5 | 7 | 7 | 7 | 7 | 7 | 7 | 7 | 7 | 7 | 7 | 7 | 7 | 7 | 7 | 7 | 7 |
| **E2** | 13 | 6 | 9 | 9 | 9 | 9 | 9 | 9 | 9 | 9 | 9 | 9 | 9 | 9 | 9 | 9 | 9 | 9 |
| **E3** | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 |
| **E4** | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 |
| **E5** | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 |
| **E6** | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 |
| **E7** | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 |
| **E8** | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 |
| **E9** | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 |
| **E10** | 10 | 10 | 10 | 10 | 10 | 10 | 12 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 |
| **E11** | 12 | 12 | 12 | 12 | 12 | 12 | 12 | 12 | 12 | 12 | 12 | 12 | 12 | 12 | 12 | 12 | 12 | 12 |
| **E12** | 12 | 12 | 12 | 12 | 12 | 12 | 12 | 12 | 12 | 12 | 12 | 12 | 12 | 12 | 12 | 12 | 12 | 12 |
| **E13** | 3 | 3 | 3 | 3 | 3 | 3 | 3 | 11 | 3 | 3 | 3 | 3 | 3 | 3 | 3 | 3 | 3 | 3 |
| **E14** | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 |
| **E15** | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 |
| **E16** | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 |
| **E17** | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 |
| **E18** | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 |
| **E19** | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 |
| **E20** | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 |
| **E21** | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 | 10 |
| **E22** | 8 | 8 | 8 | 8 | 8 | 8 | 8 | 8 | 8 | 8 | 8 | 8 | 8 | 8 | 8 | 8 | 8 | 8 |

---

## 5. Especificación Completa de Funciones Semánticas

Las acciones semánticas se implementan dentro de `GestorFuncionesSemanticas.java` y se invocan desde la matriz de acciones según el par `(estado, evento)`.

### 5.1 Buffers internos

```java
private final StringBuilder bufferLetras = new StringBuilder();
private final StringBuilder bufferDigitos = new StringBuilder();
private final StringBuilder bufferTexto = new StringBuilder();
```

### 5.2 Descripción de cada función

*   **F1 (Inicializar Letras):** limpia el `bufferLetras` y guarda la primera letra.
*   **F2 (Inicializar Dígitos):** limpia el `bufferDigitos` y guarda el primer dígito.
*   **F3 (Abrir/Acumular Cuerpo Texto):** si está en estado base, limpia `bufferTexto`; si ya está dentro del literal, acumula el carácter.
*   **F4 (Ignorar Caracteres Blancos):** descarta espacios, tabs y CR.
*   **F5 (Acumular Letras):** agrega el carácter actual al identificador.
*   **F6 (Acumular Dígitos):** agrega el dígito actual a la constante.
*   **F7 (Retornar ID o Palabra Reservada):** hace `unread` del delimitador y retorna la palabra reservada o `ID`.
*   **F8 (Retornar Fin de Línea):** marca el fin de la línea y genera `FIN_DE_LINEA`.
*   **F9 (Retornar Constante Entera):** valida rango y devuelve `CTE`.
*   **F10 (Retornar Token Directo):** resuelve operadores o delimitadores de un solo carácter.
*   **F11 (Cerrar Literal):** devuelve `LITERAL_TXT` y registra el contenido.
*   **F12 (Gestionar Bloque Comentario):** ignora el contenido hasta `*/`.
*   **FE (Error Léxico):** dispara error `E1` por carácter no permitido.

---

## 6. Lógica de Despacho y Motor de Control

El flujo principal del analizador debe seguir esta secuencia:

1. Lee un carácter.
2. Lo clasifica con `Clasificador.obtenerColumna(c)`.
3. Consulta la matriz de transición para decidir el siguiente estado.
4. Consulta la matriz de acciones para ejecutar la función semántica correspondiente.
5. Si corresponde, devuelve el token o continúa leyendo.

```java
int evento = Clasificador.obtenerColumna(c);
int siguiente = MatrizTransicion.siguiente(estadoActual, evento);
int accion = MatrizAcciones.accion(estadoActual, evento);

if (accion != -1) {
    MatrizAcciones.ejecutarAccion(estadoActual, evento, c, gestor);
}

estadoActual = siguiente;
```

En la implementación actual esta lógica queda encapsulada en `MatrizTransicion.resolver(...)`, dejando `proximoToken()` más limpio y con menos ramificaciones manuales.

---

## 7. Tokens del Lenguaje

| Nombre | Código |
| --- | ---: |
| `ID` | 256 |
| `CTE` | 257 |
| `LITERAL_TXT` | 258 |
| `PRINCIPAL` | 259 |
| `ENTERO` | 260 |
| `SI` | 261 |
| `BUCLE` | 262 |
| `HASTA` | 263 |
| `MOSTRAR` | 264 |
| `MOSTRAR_TXT` | 265 |
| `Y` | 266 |
| `O` | 267 |
| `RETORNAR` | 268 |
| `ASIG` | 269 |
| `IGUAL` | 270 |
| `DISTINTO` | 271 |
| `MENOR` | 272 |
| `MENOR_IGUAL` | 273 |
| `MAYOR` | 274 |
| `MAYOR_IGUAL` | 275 |
| `EOF` | 276 |
| `SUMA` | 277 |
| `RESTA` | 278 |
| `MULTIPLICACION` | 279 |
| `DIVISION` | 280 |
| `PAREN_IZQ` | 281 |
| `PAREN_DER` | 282 |
| `LLAVE_IZQ` | 283 |
| `LLAVE_DER` | 284 |
| `COMA` | 285 |
| `FIN_DE_LINEA` | 286 |

---

## 8. Palabras Reservadas

La lista oficial soportada por el analizador es:

- `principal`
- `entero`
- `si`
- `bucle`
- `hasta`
- `mostrar`
- `mostrarTexto`
- `y`
- `o`
- `retornar`

---

## 9. Reglas de Error Léxico

Se consideran errores léxicos:

- cualquier carácter fuera del alfabeto definido
- literal de texto sin cierre
- comentario sin cerrar
- constante numérica inválida
- constante numérica fuera del rango permitido

La constante entera soportada debe cumplir:

$$
-2^{31} \leq valor \leq 2^{31}-1
$$

---

## 10. Conclusión

La especificación mantiene la estructura original de matrices del autómata para preservar el diseño table-driven, pero refleja la implementación actual en la que la lógica de resolución del estado se centraliza en `MatrizTransicion`, mientras `MatrizAcciones` sigue siendo responsable de la ejecución de las acciones semánticas. Este modelo permite una mejor modularización, reutilización y generación del compilador en futuras iteraciones.

```cpp
estado = 0;
while (estado != EF)
{
    c = leer();
    evento = clasificar(c);
    nuevoEstado = matrizTransicion[estado][evento];
    idAccion = matrizAcciones[estado][evento];

    if (idAccion != -1)
        ejecutarFuncionSemantica(idAccion, c, estado, evento);

    estado = nuevoEstado;
}
return tokenReconocido;
```

Con este enfoque, la estructura del compilador queda ordenada: matrices para transición, matrices para acciones y funciones semánticas para la lógica de cada token.
