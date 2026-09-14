# Spec — Analizador léxico de GAUCHO

**Grupo:** Grupo D · **Lenguaje de implementación:** Java
**Depende de:** `specs/01-diseno/spec.md`

---

## 1. Alcance e interfaz

La fase léxica es la responsable de convertir la entrada fuente en una secuencia de tokens. La interfaz pública es la función `yylex()`, que se invoca por el analizador sintáctico y devuelve un único token por llamada, representado por su código entero.

La fase no genera el listado completo de tokens de antemano; simplemente produce el siguiente token válido cada vez que se lo solicita.

La implementación debe:
- leer carácter a carácter desde el flujo de entrada,
- mapear cada carácter a un evento según el alfabeto,
- avanzar por el autómata finito,
- ejecutar acciones semánticas asociadas a cada transición,
- devolver el token correspondiente,
- y, cuando corresponde, hacer `unread` del carácter delimitador para que no se pierda en la siguiente llamada.

---

## 2. Decisiones propias de esta fase

| # | Decisión | Valor |
|---|---|---|
| L1 | Base del diseño | Autómata finito determinista (DFA) definido en la fase de diseño |
| L2 | Segmentación de buffers | Dos buffers independientes: `bufferLetras` para IDs y palabras reservadas, `bufferDigitos` para constantes enteras |
| L3 | Reconocimiento de palabras | Toda palabra se reconoce primero como identificador y luego se resuelve contra la tabla de símbolos |
| L4 | Literales de texto | Se abren con `"` y cierran con la siguiente `"`; el contenido se guarda como `LITERAL_TXT` |
| L5 | Comentarios | Se soportan comentarios de línea o bloque; el comentario no genera token visible |
| L6 | Separadores | Espacios, tabs y saltos de línea se consumen según el caso; el salto de línea emite `FIN_DE_LINEA` |
| L7 | Lookahead | Se usa para decidir si el próximo carácter pertenece al token actual; si no pertenece, se hace `unread` |
| L8 | Error léxico | Todo carácter fuera del alfabeto o token mal formado genera error y se intenta continuar |
| L9 | Fin de archivo | Se entrega el token `EOF` al final de la entrada |

---

## 3. Eventos (columnas de las matrices)

`get_evento(c)` mapea el carácter leído a una columna del autómata. Los eventos se definen según el diseño del DFA.

| Col | Evento | Caracteres |
|---|---|---|
| 0 | L | a-z, A-Z |
| 1 | D | 0-9 |
| 2 | = | = |
| 3 | < | < |
| 4 | > | > |
| 5 | / | / |
| 6 | * | * |
| 7 | " | " |
| 8 | + | + |
| 9 | - | - |
| 10 | ( | ( |
| 11 | ) | ) |
| 12 | { | { |
| 13 | } | } |
| 14 | , | , |
| 15 | CR | `\n` o `\r\n` |
| 16 | BL | espacio, tab |
| 17 | OTRO | cualquier otro carácter |

> En la implementación real es conveniente usar un enum o una tabla estática para esta conversión.

---

## 4. Estados y Diagrama

Los estados del analizador léxico están definidos en la fase de diseño y se reutilizan aquí exactamente para la implementación Java.

| Estado | Significado |
|---|---|
| E0 | Inicio / espera |
| E1 | Reconociendo identificador o palabra reservada |
| E2 | Reconociendo constante entera |
| E3 | Leyendo `=` en una asignación |
| E4 | Reconociendo `==` |
| E5 | Leyendo `<` |
| E6 | Reconociendo `<>` |
| E7 | Reconociendo `<=` |
| E8 | Leyendo `>` |
| E9 | Reconociendo `>=` |
| E10 | Leyendo `/` |
| E11 | Comentario en curso |
| E12 | Posible fin de comentario |
| E13 | Literal de texto |
| E14 | SUMA |
| E15 | RESTA |
| E16 | MULTIPLICACION |
| E17 | PAREN_IZQ |
| E18 | PAREN_DER |
| E19 | LLAVE_IZQ |
| E20 | LLAVE_DER |
| E21 | COMA |
| E22 | FIN_DE_LINEA |
| EF | Final / aceptación / salida |

### Diagrama funcional

- `E0`:
  - letra -> `E1`
  - dígito -> `E2`
  - `=` -> `E3`
  - `<` -> `E5`
  - `>` -> `E8`
  - `/` -> `E10`
  - `*` -> `E16`
  - `"` -> `E13`
  - `+` -> `E14`
  - `-` -> `E15`
  - `(` -> `E17`
  - `)` -> `E18`
  - `{` -> `E19`
  - `}` -> `E20`
  - `,` -> `E21`
  - `CR` -> `E22`
  - espacio/tab -> `E0`
  - otro -> `EF`

- `E1` (ID): acepta letras y dígitos dentro del identificador. Si llega un delimitador, finaliza con `F7` y hace `unread` cuando corresponde.
- `E2` (CTE): acumula dígitos. Si llega un delimitador, finaliza con `F9` y hace `unread` cuando corresponde.
- `E3`/`E4`: detectan `=` y `==`.
- `E5`/`E6`/`E7`: detectan `<`, `<>` y `<=`.
- `E8`/`E9`: detectan `>` y `>=`.
- `E10`/`E11`/`E12`: controlan comentarios `/* ... */` y división `/`.
- `E13`: literal de texto, consume todo hasta la `"` de cierre.
- `E14` a `E21`: tokens unitarios directos.
- `E22`: fin de sentencia, produce token `FIN_DE_LINEA`.

---

## 5. Acciones semánticas

Las acciones semánticas están definidas por la matriz de funciones del diseño. Aquí se exponen como funciones concretas que el analisador puede invocar en Java.

| Acción | Qué hace |
|---|---|
| F1 | Inicializar `bufferLetras`; limpiar buffer y guardar primer carácter |
| F2 | Inicializar `bufferDigitos`; limpiar buffer y guardar primer dígito |
| F3 | Preparar literal: abrir buffer textual y descartar la comilla inicial |
| F4 | Ignorar: espacio, tabulación o separador sin acumular nada |
| F5 | Acumular carácter en `bufferLetras` |
| F6 | Acumular dígito en `bufferDigitos` |
| F7 | Finalizar ID o palabra reservada con lookahead; hacer `unread` si corresponde; resolver palabra reservada | 
| F8 | Emitir `FIN_DE_LINEA`; actualizar conteo de líneas |
| F9 | Finalizar constante entera; validar rango; registrar en tabla de símbolos |
| F10 | Emitir token directo para operadores y delimitadores ya resueltos por el autómata |
| F11 | Cerrar literal; descartar comilla final; guardar contenido en tabla de símbolos |
| F12 | Cerrar comentario; consumir el bloque sin emitir token visible |
| FE | Error léxico; reportar carácter inválido y continuar la recuperación |

### Regla de implementación

En Java la implementación concreta debe distinguir claramente dos flujos:
- `bufferLetras` acumula letras y dígitos que forman un identificador o palabra reservada.
- `bufferDigitos` acumula únicamente dígitos que forman una constante entera.
- Nunca debe reutilizarse el mismo buffer ni la misma función para ambos casos.

---

## 6. Matriz de Nuevos Estados

`nuevo_estado[estado][evento]`

| Estado Actual | Letra | Dígito | = | < | > | / | * | " | + | - | ( | ) | { | } | , | CR | Espacio / Tab | Otro |
| :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: |
| **E0** | E1 | E2 | E3 | E5 | E8 | E10 | E16 | E13 | E14 | E15 | E17 | E18 | E19 | E20 | E21 | E22 | E0 | EF |
| **E1** | E1 | E1 | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF |
| **E2** | EF | E2 | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF |
| **E3** | EF | EF | E4 | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF |
| **E4** | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF |
| **E5** | EF | EF | E7 | EF | E6 | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF |
| **E6** | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF |
| **E7** | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF |
| **E8** | EF | EF | E9 | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF |
| **E9** | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF |
| **E10** | EF | EF | EF | EF | EF | EF | E11 | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF |
| **E11** | E11 | E11 | E11 | E11 | E11 | E11 | E12 | E11 | E11 | E11 | E11 | E11 | E11 | E11 | E11 | E11 | E11 | E11 |
| **E12** | E11 | E11 | E11 | E11 | E11 | E0 | E11 | E11 | E11 | E11 | E11 | E11 | E11 | E11 | E11 | E11 | E11 | E11 |
| **E13** | E13 | E13 | E13 | E13 | E13 | E13 | E13 | EF | E13 | E13 | E13 | E13 | E13 | E13 | E13 | E13 | E13 | E13 |
| **E14** | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF |
| **E15** | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF |
| **E16** | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF |
| **E17** | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF |
| **E18** | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF |
| **E19** | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF |
| **E20** | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF |
| **E21** | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF |
| **E22** | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF | EF |
| **EF** | - | - | - | - | - | - | - | - | - | - | - | - | - | - | - | - | - | - |

---

## 7. Matriz de Transiciones

`proceso[estado][evento]`

La matriz de acciones semánticas indica qué función ejecuta el analizador cuando el DFA se encuentra en un estado y recibe un evento. Esta parte se usa para producir el token y actualizar los buffers.

| Estado Actual | Letra | Dígito | = | < | > | / | * | " | + | - | ( | ) | { | } | , | CR | Espacio / Tab | Otro |
| :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: |
| **E0** | F1 | F2 | F1 | F1 | F1 | F1 | F1 | F3 | F1 | F1 | F1 | F1 | F1 | F1 | F1 | F8 | F4 | FE |
| **E1** | F5 | F5 | F7 | F7 | F7 | F7 | F7 | F7 | F7 | F7 | F7 | F7 | F7 | F7 | F7 | F7 | F7 | F7 |
| **E2** | FE | F6 | F9 | F9 | F9 | F9 | F9 | F9 | F9 | F9 | F9 | F9 | F9 | F9 | F9 | F9 | F9 | F9 |
| **E3** | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 |
| **E4** | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 |
| **E5** | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 |
| **E6** | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 |
| **E7** | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 |
| **E8** | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 |
| **E9** | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 |
| **E10** | F10 | F10 | F10 | F10 | F10 | F10 | F12 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 |
| **E11** | F12 | F12 | F12 | F12 | F12 | F12 | F12 | F12 | F12 | F12 | F12 | F12 | F12 | F12 | F12 | F12 | F12 | F12 |
| **E12** | F12 | F12 | F12 | F12 | F12 | F12 | F12 | F12 | F12 | F12 | F12 | F12 | F12 | F12 | F12 | F12 | F12 | F12 |
| **E13** | F3 | F3 | F3 | F3 | F3 | F3 | F3 | F11 | F3 | F3 | F3 | F3 | F3 | F3 | F3 | F3 | F3 | F3 |
| **E14** | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 |
| **E15** | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 |
| **E16** | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 |
| **E17** | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 |
| **E18** | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 |
| **E19** | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 |
| **E20** | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 |
| **E21** | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 |
| **E22** | F8 | F8 | F8 | F8 | F8 | F8 | F8 | F8 | F8 | F8 | F8 | F8 | F8 | F8 | F8 | F8 | F8 | F8 |
| **EF** | - | - | - | - | - | - | - | - | - | - | - | - | - | - | - | - | - | - |

---

## 8. Tabla de Unreads

`Sí` significa que el carácter leído no pertenece al token que se cerró y debe devolverse al flujo para ser procesado luego (`unget` / `pushback`).

| Estado | Evento que cierra el token | Unread | Motivo |
|---|---|---|---|
| E1 | cualquier delimitador no alfanumérico | Sí | finaliza identificador o palabra reservada |
| E2 | cualquier delimitador no dígito | Sí | finaliza constante numérica |
| E5 | `=` o `>` o cualquier no-`<` | Sí | resuelve `<`, `<=` o `<>` |
| E6 | cualquier carácter fuera de la secuencia | Sí | `<>` ya fue reconocido |
| E8 | `=` u otro delimitador | Sí | resuelve `>=` o finaliza `>` |
| E10 | `*` o cualquier otro carácter no `/` | Sí | detecta comentario o división |
| E13 | `"` | Sí | la comilla de cierre se consume para decidir el fin del literal |
| E22 | siguiente carácter | Sí | el fin de línea se reconoce y queda listo para la siguiente línea |

**Regla general:** cuando el token se cierra antes de consumir un carácter delimitador, ese carácter se deja en el flujo para que el siguiente `yylex()` lo procese.

---

## 9. Errores que emite esta fase

| Código | Condición | Mensaje |
|---|---|---|
| E1 | Carácter fuera del alfabeto | `Error léxico: carácter no permitido` |
| E2 | Constante numérica mal formada o fuera de rango | `Error léxico: constante entera inválida` |
| E3 | Literal de texto sin cierre | `Error léxico: literal sin comilla final` |
| E4 | Comentario no cerrado antes del final de archivo | `Error léxico: comentario no terminado` |
| E5 | Secuencia inválida de operadores compuestos | `Error léxico: secuencia de operadores inválida` |
| E6 | Fin de archivo inesperado en medio de un token | `Error léxico: fin de archivo inesperado` |

**Observación:** la estrategia recomendada es no abortar al primer error; se registra y se intenta continuar con la recuperación.

---

## 10. Traza de verificación

Entrada de prueba: `si (x == 1)`

| Estado | Lee | Evento | Acción | Nuevo estado | Unread | Retorna |
|---|---|---|---|---|---|---|
| E0 | `s` | L | F1 | E1 | No | - |
| E1 | `i` | L | F5 | E1 | No | - |
| E1 | espacio | BL | F7 | EF | Sí | `SI` |
| E0 | `(` | ( | F10 | EF | No | `PAREN_IZQ` |
| E0 | `x` | L | F1 | E1 | No | - |
| E1 | `=` | = | F7 | EF | Sí | `ID` |
| E0 | `=` | = | F1 | E3 | No | - |
| E3 | `=` | = | F10 | EF | No | `IGUAL` |
| E0 | `1` | D | F2 | E2 | No | - |
| E2 | `)` | ) | F9 | EF | Sí | `CTE` |
| E0 | `)` | ) | F10 | EF | No | `PAREN_DER` |

---

## 11. Casos de prueba de esta fase

| Entrada | Salida esperada | Qué verifica |
|---|---|---|
| `principal` | `PRINCIPAL` | resolución de palabra reservada |
| `entero` | `ENTERO` | palabra reservada de tipo |
| `resultado` | `ID` | identificador válido |
| `123` | `CTE` | constante entera |
| `x == 5` | `ID`, `IGUAL`, `CTE` | reconocimiento de comparación |
| `a <= b` | `ID`, `MENOR_O_IGUAL`, `ID` | operador compuesto |
| `"hola"` | `LITERAL_TXT` | literal de texto |
| `/* comentario */` | sin token visible | comentario ignorado |
| `a + b` | `ID`, `SUMA`, `ID` | operador aritmético |
| `a,b` | `ID`, `COMA`, `ID` | separador de lista |
| `}` | `LLAVE_DER` | delimitador de bloque |
| `@` | error léxico | carácter fuera del alfabeto |

---

## 12. Recomendaciones de codificación Java

Para implementar esta fase con claridad, conviene definirse estas piezas:

1. `enum Evento { L, D, IGUAL, MENOR, MAYOR, BARRA, ASTERISCO, COMILLA, MAS, MENOS, PAREN_IZQ, PAREN_DER, LLAVE_IZQ, LLAVE_DER, COMA, CR, BL, OTRO }`
2. `int get_evento(char c)`
3. `int[][] nuevoEstado` o `switch` por estado y evento
4. `String bufferLetras` y `String bufferDigitos`
5. `boolean unreadEnabled` o un `PushbackReader` / `BufferedReader` con `mark`/`reset` o `unread`
6. `Token` o `int` con la clasificación final y la posible tabla de símbolos
7. manejo de `EOF` como caso especial final

La implementación no debe mezclar el manejo del buffer de letras con el de dígitos. Esa separación es una de las decisiones más importantes de la fase léxica y es la base para el resto del compilador.

---