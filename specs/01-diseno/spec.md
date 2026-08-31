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
| SIM | + - * / ( ) { } < > ! = |
| BL | espacio, tabulador |
| SL | salto de línea (\n, \r\n) $\rightarrow$ Token de fin de sentencia |
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
| 271 | DISTINTO | != |
| 272 | MENOR | < |
| 273 | MAYOR | > |
| 274 | EOF | fin de archivo |
| — | fin de línea | El salto de línea físico actúa como delimitador sintáctico |
| — | literales | + - * / ( ) { } , se devuelven como su propio carácter |

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

<comparador>        ::= IGUAL | DISTINTO | MENOR | MAYOR

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
entero calcularFactorial(entero numero) {
    entero resultado = 1
    
    bucle {
        resultado = resultado * numero
        numero = numero - 1
    } hasta (numero == 0)
    
    retornar resultado
}

entero principal() {
    entero vacas, limite
    vacas = 3
    limite = 3
    
    si (vacas > 0) {
        entero totalGauchos
        totalGauchos = calcularFactorial(vacas)
        mostrarTexto("El total calculado es:")
        mostrar(totalGauchos)
    }
}
```

Salida esperada: 

El total calculado es:
6

---

## 10. Fuera de alcance

* Tipos de datos reales (punto flotante), caracteres, booleanos nativos o arreglos.
* Funciones con más de un (1) parámetro o sin valor de retorno (procedimientos void).
* Sentencias de entrada de datos por teclado (no hay funciones de lectura).
* Operadores lógicos de negación binaria o unaria (not / !).
