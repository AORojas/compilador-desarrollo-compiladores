# Spec — Diseño del lenguaje GAUCHO

**Grupo:** Grupo D · **Lenguaje de implementación:** Java 
**Estado:** Abierta para desarrollo. Todo cambio acá impacta en las specs de las fases siguientes.

---

## 1. Decisiones globales

| # | Decisión | Valor |
|---|---|---|
| D1 | Tipo de datos | Único tipo: entero con signo (16, 32 o 64 bits a informar por el grupo) |
| D2 | Finalización de línea | Salto de línea (\n). No se utiliza el punto y coma ; |
| D3 | Declaración | Obligatoria y previa al primer uso (error semántico si no se cumple) |
| D4 | Alcance | Soporta funciones locales y variables locales dentro de estructuras de control |
| D5 | Control de Overflow | Estricto en operaciones aritméticas. Lanza error en ejecución y cancela |
| D6 | Nivel de anidamiento | Máximo 3 niveles en estructuras de control. Gestionado semánticamente |
| D7 | Evaluación de condiciones | Cortocircuito obligatorio para operadores lógicos y / o |
| D8 | Gestión de Memoria | Pila de registros de activación (soporta recursión) |
| D9 | Comentarios | De línea o de bloque (a elección), descartados por el analizador léxico |
| D10 | Plataforma destino | Código Assembler ejecutable (ej. x86 con MASM/NASM) |


---

## 2. Alfabeto

| Clase | Caracteres |
|---|---|
| L | a–z, A–Z |
| D | 0–9 |
| SIM | + - * / ( ) { } < > = |
| BL | espacio, tabulador |
| SL | salto de línea LF, CRLF(\n, \r\n) $\rightarrow$ Token de fin de sentencia |
| OTRO | cualquier otro carácter $\rightarrow$ error léxico |

---

## 3. Palabras reservadas
principal · entero · si · bucle · hasta · mostrar · mostrarTexto · y · o · retornar
(Se reconocen como identificadores en el autómata y se resuelven mediante búsqueda en la tabla de símbolos).

---

## 4. Tabla de tokens

| Código | Token | Lexema |
|---|---|---|
| 256 | ID | identificador |
| 257 | CTE | constante entera |
| 258 | LITERAL_TXT | literal de texto (para rotular salidas) |
| 259 | PRINCIPAL | principal |
| 260 | ENTERO | entero |
| 261 | SI | si |
| 262 | BUCLE | bucle |
| 263 | HASTA | hasta |
| 264 | MOSTRAR | mostrar |
| 265 | MOSTRAR_TXT | mostrarTexto |
| 266 | Y | y |
| 267 | O | o |
| 268 | RETORNAR | retornar |
| 269 | ASIG | = |
| 270 | IGUAL | == |
| 271 | DISTINTO | <> |
| 272 | MENOR | < |
| 273 | MENOR_O_IGUAL | <= |
| 274 | MAYOR | > |
| 275 | MAYOR_O_IGUAL | >= |
| 276 | EOF | fin de archivo |
| 277 | SUMA | + |
| 278 | RESTA | - |
| 279 | MULTIPLICACION | * |
| 280 | DIVISION | / |
| 281 | PAREN_IZQ | ( |
| 282 | PAREN_DER | ) |
| 283 | LLAVE_IZQ | { |
| 284 | LLAVE_DER | } |
| 285 | COMA | , |
| 286 | FIN_DE_LINEA | salto de línea o CRLF |


---

## 5. Estructura del programa
La unidad de compilación es un conjunto de funciones. El punto de entrada obligatorio es una función con el nombre reservado principal. Las funciones admiten exactamente un parámetro pasado por valor y devuelven un entero.

---

## 6. Gramática

```
<programa>          ::= <lista_funciones>

<lista_funciones>   ::= <lista_funciones> <funcion>
                      | <funcion>

<funcion>           ::= ENTERO ID '(' ENTERO ID ')' <bloque>
                      | ENTERO PRINCIPAL '(' ')' <bloque>

<bloque>            ::= '{' <fin_lineas> <sentencias> '}' <fin_lineas>

<sentencias>        ::= <sentencias> <sentencia>
                      | <sentencia>

<sentencia>         ::= <declaracion> <separador>
                      | <asignacion> <separador>
                      | <seleccion> <separador>
                      | <iteracion> <separador>
                      | <salida> <separador>
                      | <retorno> <separador>

<separador>         ::= <fin_lineas>

<fin_lineas>        ::= <fin_lineas> '\n'
                      | '\n'

<declaracion>       ::= ENTERO <lista_ids>
<lista_ids>         ::= <lista_ids> ',' ID
                      | ID

<asignacion>        ::= ID ASIG <expresion>

<seleccion>         ::= SI '(' <condicion> ')' <bloque>

<iteracion>         ::= BUCLE <bloque> HASTA '(' <condicion> ')'

<salida>            ::= MOSTRAR '(' <expresion> ')'
                      | MOSTRAR_TXT '(' LITERAL_TXT ')'

<retorno>           ::= RETORNAR <expresion>

<condicion>         ::= <condicion> Y <cond_bloque>
                      | <condicion> O <cond_bloque>
                      | <cond_bloque>

<cond_bloque>       ::= <expresion> <comparador> <expresion>
                      | '(' <condicion> ')'

<comparador>        ::= IGUAL | DISTINTO | MENOR | MENOR O IGUAL | MAYOR | MAYOR O IGUAL

<expresion>         ::= <expresion> '+' <termino>
                      | <expresion> '-' <termino>
                      | <termino>

<termino>           ::= <termino> '*' <factor>
                      | <termino> '/' <factor>
                      | <factor>

<factor>            ::= ID
                      | CTE
                      | '(' <expresion> ')'
                      | ID '(' <expresion> ')'
```

**Notas sobre la gramática**
* Diseñada con recursión a la izquierda para su fácil integración con YACC.
* Los saltos de línea (\n) actúan como terminadores de sentencias en lugar del punto y coma.
* El bucle ... hasta evalúa al final (ejecuta el cuerpo al menos una vez).

---

## 7. Semántica

| Regla | Definición |
|---|---|
| R1 | Usar un ID no declarado previamente en el bloque o globales es un error semántico. |
| R2 | Redefinir un ID en el mismo ámbito de alcance es un error semántico. |
| R3 | Las estructuras de control (si, bucle) pueden anidarse hasta tres niveles. Superar este límite lanza error semántico con número de línea. El control se realiza mediante un contador en las acciones semánticas. |
| R4 | Evaluación en cortocircuito obligatoria para expresiones lógicas ligadas por y u o. |
| R5 | La tabla de símbolos debe registrar obligatoriamente: nombre, tipo, valor (constantes) y longitud. Se exporta al finalizar la compilación. |
| R6 | Las funciones gestionan sus variables en una pila de registros de activación (layout: dirección de retorno, enlace dinámico, parámetro, variables locales). |

---

## 8. Responsabilidad de cada error

| Código | Descripción | Fase que lo detecta |
|---|---|---|
| E1 | Carácter no perteneciente al alfabeto | Léxico |
| E2 | Constante numérica mal formada o fuera de límite | Léxico / Semántico |
| E3 | Estructura sintáctica inválida (falta de salto de línea, paréntesis sin cerrar, etc.) | Sintáctico |
| E4 | Variable no declarada en el alcance actual | Semántico |
| E5 | Anidamiento de estructuras de control superior a 3 niveles | Semántico |
| E6 | Error de firmas en funciones (cantidad errónea de parámetros) | Semántico |
| E7 | Overflow aritmético en operaciones matemáticas | Ejecución (Código Assembler generado) |

Nota: El compilador no debe abortar en el primer error léxico, sintáctico o semántico; debe recuperarse para reportar la mayor cantidad posible.

---

## 9. Programa de ejemplo

```java
/* 
   Módulo de cálculo matemático
   Implementa el algoritmo de factorial bajo la gramática oficial
*/
entero calcularFactorial(entero numero)
{
    entero resultado
    resultado = 1
    
    bucle
    {
        resultado = resultado * numero
        numero = numero - 1
    } hasta (numero igual 0)
    
    retornar resultado
}

/* Función de entrada al sistema */
entero principal()
{
    entero vacas, limite
    vacas = 3
    limite = 3
    
    si (vacas > 0)
    {
        entero totalGauchos
        totalGauchos = calcularFactorial(vacas)
        mostrarTexto("El total calculado es:")
        mostrar(totalGauchos)
    }
}
```

**Salida esperada:**

```text
El total calculado es:
6
```
## 10. Autómata Finito

![Autómata Finito](../../compilador01/src/assets/Autómata%20Finito.png)

## 11. Matriz de Transición de estados


| Estado Actual | Letra | Dígito | = | < | > | / | * | " | + | - | ( | ) | { | } | , | CR | Espacio / Tab | Otro |
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

## 12. Matriz de Tokens

En esta matriz, cada celda indica el código del token que se devuelve cuando el autómata llega a un estado terminal con la clase de entrada indicada. Si la combinación no produce un token directamente, se usa `-1`.

| Estado Actual | Letra | Dígito | = | < | > | / | * | " | + | - | ( | ) | { | } | , | CR | Espacio / Tab | Otro |
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

## 13. Matriz de funciones semánticas

| Estado Actual | Letra | Dígito | = | < | > | / | * | " | + | - | ( | ) | { | } | , | CR | Espacio / Tab | Otro |
| :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: |
| **E0** (Comienzo) | F1 | F2 | F1 | F1 | F1 | F1 | F1 | F3 | F1 | F1 | F1 | F1 | F1 | F1 | F1 | F8 | F4 | FE |
| **E1** (ID / Pal. Reservada) | F5 | F5 | F7 | F7 | F7 | F7 | F7 | F7 | F7 | F7 | F7 | F7 | F7 | F7 | F7 | F7 | F7 | F7 |
| **E2** (CTE / Constante) | FE | F6 | F9 | F9 | F9 | F9 | F9 | F9 | F9 | F9 | F9 | F9 | F9 | F9 | F9 | F9 | F9 | F9 |
| **E3** (ASIG) | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 |
| **E4** (IGUAL) | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 |
| **E5** (MENOR) | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 |
| **E6** (DISTINTO) | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 |
| **E7** (MENOR O IGUAL) | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 |
| **E8** (MAYOR) | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 |
| **E9** (MAYOR O IGUAL) | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 |
| **E10** (DIVISION) | F10 | F10 | F10 | F10 | F10 | F10 | F12 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 |
| **E11** (Comentario) | F12 | F12 | F12 | F12 | F12 | F12 | F12 | F12 | F12 | F12 | F12 | F12 | F12 | F12 | F12 | F12 | F12 | F12 |
| **E12** (Posible fin comentario) | F12 | F12 | F12 | F12 | F12 | F12 | F12 | F12 | F12 | F12 | F12 | F12 | F12 | F12 | F12 | F12 | F12 | F12 |
| **E13** (Literal de Texto) | F3 | F3 | F3 | F3 | F3 | F3 | F3 | F11 | F3 | F3 | F3 | F3 | F3 | F3 | F3 | F3 | F3 | F3 |
| **E14** (SUMA) | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 |
| **E15** (RESTA) | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 |
| **E16** (MULTIPLICACION) | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 |
| **E17** (PAREN_IZQ) | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 |
| **E18** (PAREN_DER) | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 |
| **E19** (LLAVE_IZQ) | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 |
| **E20** (LLAVE_DER) | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 |
| **E21** (COMA) | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 | F10 |
| **E22** (FIN_DE_LINEA) | F8 | F8 | F8 | F8 | F8 | F8 | F8 | F8 | F8 | F8 | F8 | F8 | F8 | F8 | F8 | F8 | F8 | F8 |
| **EF** (Final / Aceptación) | - | - | - | - | - | - | - | - | - | - | - | - | - | - | - | - | - | - |


---

## 14. Definición de las funciones

Cada función modifica el buffer, el cursor o el estado del analizador antes de forzar la salida por `EF` o continuar la construcción del token.

* F1 (Inicializar Buffer de Letras): limpia el buffer de identificadores y palabras reservadas y guarda el primer carácter leído en ese buffer.
* F2 (Inicializar Buffer de Dígitos): limpia el buffer de constantes enteras y guarda el primer dígito leído, sin reutilizar el mismo buffer que el de letras.
* F3 (Abrir Literal): prepara el buffer de texto para acumular un literal y descarta la comilla de apertura.
* F4 (Ignorar): no acumula nada, consume espacios, tabuladores o separadores y mantiene el estado en espera.
* F5 (Acumular en letras): agrega el carácter actual al buffer de letras para continuar la construcción de un identificador o palabra reservada.
* F6 (Acumular en dígitos): agrega el carácter actual al buffer de dígitos para continuar la construcción de una constante entera.
* F7 (Retornar ID o Palabra Reservada): en un estado de aceptación de identificador, inspecciona el siguiente carácter con lookahead para decidir si el identificador terminó o si debe seguir acumulándose. Si el siguiente carácter no pertenece a la secuencia del identificador, se debe hacer `unread` para devolverlo al lector y no perder el delimitador. Luego se normaliza la cadena, se resuelve si es palabra reservada o identificador y se ubica el token en la tabla de símbolos si corresponde.
* F8 (Retornar Fin de Línea): incrementa el contador de líneas del compilador y emite el token de fin de sentencia.
* F9 (Retornar Constante): retrocede un carácter si el autómata consumió un carácter de más, valida el rango entero y registra la constante en la tabla de símbolos.
* F10 (Retornar Token Directo): se usa para tokens unitarios o compuestos cuya clasificación ya está definida por el estado del autómata. Aquí entran los operadores simples y los casos de asignación/igualdad/distinto resueltos en la máquina, sin función especial adicional.
* F11 (Cerrar Literal): descarta la comilla de cierre, guarda el contenido limpio en la tabla de símbolos y prepara la salida final con token `LITERAL_TXT`.
* F12 (Cerrar Comentario): consume el resto del comentario cuando la barra (`/`) se reconoce como inicio de comentario y no genera token visible.
* FE (Error Léxico): reporta el carácter inválido, registra la línea y fuerza la salida hacia `EF` para continuar con la recuperación.

En Java, la implementación concreta debe distinguir claramente dos flujos separados:
- `bufferLetras` acumula letras y dígitos que pertenecen a un identificador o palabra reservada.
- `bufferDigitos` acumula únicamente dígitos para una constante entera.
- Nunca se reutiliza el mismo método ni el mismo buffer para ambas cosas; cada flujo tiene su propia función de inicialización y su propia función de acumulación.

---

## 15. Fuera de alcance

* Tipos de datos reales (punto flotante), caracteres, booleanos nativos o arreglos.
* Funciones con más de un (1) parámetro o sin valor de retorno (procedimientos void).
* Sentencias de entrada de datos por teclado (no hay funciones de lectura).
* Operadores lógicos de negación binaria o unaria (not / !).

---
