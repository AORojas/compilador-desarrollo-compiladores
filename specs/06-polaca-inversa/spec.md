# Especificación Técnica: Generador de Código Intermedio

## 1. Objetivo y Alcance

El Generador de Código Intermedio recibe el Árbol de Sintaxis Abstracta (AST) construido a partir de las acciones desacopladas del Analizador Sintáctico y validado por el Analizador Semántico, traduciéndolo a una representación lineal llamada **Polaca Inversa**. 

Su única responsabilidad es emitir las instrucciones (operandos, operadores y saltos). No valida variables ni ámbitos (eso ya lo hizo el Analizador Semántico) y no genera código Assembler.

La implementación se encuentra organizada en el paquete `gc`, distribuida en:
- `GeneradorCodigoIntermedio`: coordina el recorrido del AST y emite las instrucciones consultando la Tabla de Símbolos.
- `PolacaInversa`: estructura de datos que almacena las instrucciones y gestiona el backpatching.

## 2. Referencias y Rutas del Proyecto

*   **Diseño del lenguaje:** `specs/01-diseño/spec.md`.
*   **Analizador Sintáctico:** `specs/03-analizador-sintáctico/spec.md` (define la integración del parser y los puntos de reducción; la gramática oficial está en `specs/01-diseño/spec.md`).
*   **Especificación del AST:** `specs/04-arbol-sintáctico/spec.md` (define las clases `NodoAST`, `Nodo`, `Hoja` y los operadores exactos instanciados por las acciones desacopladas).
*   **Implementación del AST:** `Compilador/src/ast/`.
*   **Especificación del Analizador Semántico:** `specs/05-analizador-semántico/spec.md` (define la Tabla de Símbolos resultante y las validaciones previas).

## 3. Estructura de Datos (La Polaca Inversa)

La clase `PolacaInversa` encapsula una lista (`ArrayList<Object>`) y maneja cuatro tipos de elementos:
1.  **`Referencia` (entero):** ID numérico de un símbolo registrado en la Tabla de Símbolos.
2.  **`String`:** Operadores (`+`, `:=`, `PRINT`, `PRINT_TXT`, `CALL`, `RET`, etc.).
3.  **`DestinoPendiente`:** Un "hueco" reservado para un salto no resuelto.
4.  **`DestinoSalto` (entero):** La posición final a la que salta una instrucción.

### 3.0. Formato lineal de la Polaca

Cada referencia y cada operador se representa entre corchetes. Los operandos se formatean como `[ID]` y los operadores como `[+]`, `[:=]`, `[BF]`, `[BI]`, etc.

Cuando un salto queda resuelto, su destino no se representa entre corchetes: se imprime inmediatamente después del operador como `->N`. Por ejemplo:

```text
[1] [3] [>] [BF] ->16 [BI] ->9
```

En este ejemplo, `BF` salta a la posición `16` y `BI` salta a la posición `9`. La notación `[16]` no representa un destino de salto.

### 3.1. Métodos de Backpatching
- `addSaltoPendiente(String operador)`: Agrega el operador de salto y un hueco, devolviendo el índice del hueco.
- `patch(int posicionHueco, int destino)`: Rellena el hueco con el destino real.
- `addSaltoIncondicional(int destino)`: Agrega un operador `BI` con el destino ya resuelto.
- `getPos()`: Devuelve el índice actual de la lista.
- `resolverDestinos()`: Verifica al final que no queden instancias de `DestinoPendiente`.

## 4. Recorrido del AST y Traducción (Algoritmo)

La clase `GeneradorCodigoIntermedio` recorre el árbol en **Post-Orden** (primero hijos, luego raíz), interpretando los operadores definidos en `specs/04-arbol-sintáctico/spec.md`:

### 4.1. Operandos y Literales (Hojas)
*   **Expresiones y Condiciones (`Hoja`):** Al visitar una hoja en contexto de expresión o condición, el generador busca su ID en la `TablaSimbolos` generada por el Analizador Semántico. Evalúa el valor mediante coincidencia numérica (`[0-9]+`): si es número, lo busca como constante; si es texto, lo busca como variable. Luego emite la referencia correspondiente mediante `addReferencia(id)`.
*   **Literales de Texto (`MOSTRAR_TXT`):** Al visitar un nodo con operador `"MOSTRAR_TXT"`, extrae la `Hoja` ubicada en su hijo izquierdo, consulta su ID en la Tabla de Símbolos como literal de texto (`"texto"`), emite su referencia y agrega el operador correspondiente (`"PRINT_TXT"` o `"PRINT"`).

### 4.2. Expresiones, Declaraciones y Asignaciones (Nodos)
Dado que `Nodo` tiene `izquierdo` y `derecho` (que pueden ser nulos), se valida cada acceso antes de recorrerlo:
*   **Operadores Aritméticos y Relacionales (`+`, `-`, `*`, `/`, `>`, `<`, `>=`, `<=`, `==`, `<>`):** Procesar `izquierdo`, procesar `derecho` y emitir el operador del nodo (`addOperador(operador)`).
*   **Asignación (`:=` o `=`):** Procesar `derecho` (expresión), emitir la referencia de la hoja en `izquierdo` (variable destino), y emitir el operador `":="`.
*   **Declaraciones (`DECL`):** No generan instrucciones ejecutables en la Polaca Inversa. El generador registra los identificadores del hijo derecho para asociar cada variable al ámbito correcto, incluido el *shadowing*; no emite esos identificadores como operandos.
*   **`MOSTRAR` y `RETORNAR`:** Procesar `izquierdo` y emitir `"PRINT"` o `"RET"`.
*   **Llamadas a funciones (`LLAMADA`):** Si `derecho` (argumento) no es nulo, procesarlo primero; luego emitir la referencia de la hoja en `izquierdo` (nombre de la función) y emitir `"CALL"`.

### 4.3. Estructuras de Control y Funciones (Backpatching)
*   **`SI`:**
  1. Procesar condición (`izquierdo`).
  2. Emitir un salto condicional en falso pendiente (`saltoFalso = addSaltoPendiente("BF")`).
  3. Procesar bloque (`derecho`).
  4. Rellenar el salto pendiente (`patch(saltoFalso, getPos())`).

*   **`BUCLE` (Repeat-Until):**
  1. Guardar la posición inicial (`inicio = getPos()`).
  2. Procesar bloque (`izquierdo`).
  3. Procesar condición (`derecho`).
  4. Emitir un salto condicional en falso (`BF`) y parcharlo inmediatamente apuntando a la posición de `inicio`.

*   **Nodos Estructurales (`PROGRAMA`, `FUNCIONES`, `FUNCION`, `BLOQUE`, `SENTENCIAS`):**
  * Para `"PROGRAMA"`, `"FUNCIONES"`, `"BLOQUE"` y `"SENTENCIAS"`, el generador recorre secuencialmente sus hijos no nulos (`izquierdo` y luego `derecho`).
  * Para `"FUNCION"`, omite el nodo auxiliar de firma ubicado en `izquierdo` (nombre y parámetro, ya registrados en la Tabla de Símbolos) y procesa únicamente el cuerpo del bloque ubicado en `derecho`.

## 5. Modelo de Ejecución (Máquina de Pila)

La Polaca Inversa se ejecuta en una **Máquina de Pila (Stack Machine)**, procesando de izquierda a derecha:
- **Referencia:** hace `PUSH` del valor.
- **Operador:** hace `POP` de los operandos, opera y hace `PUSH` del resultado.
- **Salto:** modifica el índice de lectura al destino.

**Ejemplo:** `[5] [3] [+] [x] [:=]`
1. `PUSH 5`
2. `PUSH 3`
3. `POP 3`, `POP 5`, suma, `PUSH 8`
4. `PUSH dir(x)`
5. `POP dir(x)`, `POP 8`, asigna.

## 6. Procedimiento Principal de Traducción

El método `generar(NodoAST raiz)` inicia el recorrido recursivo verificando primero que la raíz recibida tras las fases sintáctica y semántica no sea nula.

El procedimiento interno `procesarNodo(NodoAST nodo)` clasifica cada elemento del árbol en lenguaje lógico:

1. **Si el nodo es nulo:** Finaliza la rama sin emitir instrucciones (tolerando árboles parciales tras recuperaciones sintácticas).
2. **Si es una `Hoja`:** Delega en `emitirHoja()`, que determina si el lexema es constante entera (`[0-9]+`) o variable, consulta su identificador en la `TablaSimbolos` y agrega la referencia a la Polaca.
3. **Si es un `Nodo`:** Evalúa su atributo `operador`:
   * **Declaración (`"DECL"`):** Registra los identificadores de su hijo derecho en el ámbito activo para resolver variables y *shadowing*. No emite referencias ni operadores ejecutables a la Polaca.
   * **Función (`"FUNCION"`):** Procesa exclusivamente su hijo derecho (el bloque de sentencias), omitiendo el nodo auxiliar de firma del hijo izquierdo.
   * **Expresiones binarias (`"+"`, `"-"`, `"*"`, `"/"`, `">"`, `"<"`, `">="`, `"<="`, `"=="`, `"<>"`):** Procesa recursivamente el hijo izquierdo, luego el hijo derecho y finalmente emite el operador.
   * **Asignación (`":="` o `"="`):** Procesa primero el hijo derecho (expresión), luego emite la referencia de la hoja ubicada en el hijo izquierdo y finalmente emite el operador `":="`.
   * **Condicional (`"SI"`):** Procesa el hijo izquierdo (condición), reserva un salto pendiente `"BF"`, procesa el hijo derecho (bloque) y parcha el salto con la posición actual.
   * **Iteración (`"BUCLE"`):** Registra la posición actual como inicio, procesa el hijo izquierdo (bloque), procesa el hijo derecho (condición), reserva un salto `"BF"` y lo parcha apuntando al inicio registrado.
   * **Salida (`"MOSTRAR"` y `"MOSTRAR_TXT"`):** Para `"MOSTRAR"`, procesa el hijo izquierdo y emite `"PRINT"`. Para `"MOSTRAR_TXT"`, busca la hoja literal del hijo izquierdo en la Tabla de Símbolos, emite su referencia y agrega el operador de impresión de texto.
   * **Retorno (`"RETORNAR"`):** Procesa el hijo izquierdo y emite `"RET"`.
   * **Llamada a función (`"LLAMADA"`):** Si el hijo derecho (argumento) existe, lo procesa; luego emite la referencia de la hoja en el hijo izquierdo (función) y emite `"CALL"`.
   * **Otros nodos estructurales (`"PROGRAMA"`, `"FUNCIONES"`, `"BLOQUE"`, `"SENTENCIAS"`):** Continúa el recorrido recursivo por el hijo izquierdo y luego por el hijo derecho siempre que no sean nulos.

## 7. Formato de Salida

El `Main` imprimirá la **Polaca Inversa** de forma lineal, mostrando los operandos entre corchetes y los operadores tal cual. Los destinos resueltos de los saltos `BF` y `BI` se muestran con el prefijo `->`, separado del operador por un espacio.

**Ejemplo de salida para `x := 5 + 3`:**

```text
Polaca Inversa:
[2] [3] [+] [1] [:=]
```

> **Nota:** Donde `2` y `3` son los IDs de las constantes, y `1` es el ID de la variable `x`.

**Ejemplo de salida con saltos resueltos:**

```text
[1] [3] [>] [BF] ->16 [BI] ->9 [2] [PRINT]
```

Los elementos `BF` y `BI` siguen representándose como `[BF]` y `[BI]`. Sus destinos son `16` y `9`, respectivamente, y por eso se escriben `->16` y `->9`, sin corchetes.

---

### 8. Consideraciones y Límites de la Implementación

* **Validación previa:** El generador asume que el **AST** ya fue validado por el Analizador Semántico y que todos los símbolos (variables, constantes, literales de texto y funciones) ya existen en la `TablaSimbolos`. No vuelve a chequear declaraciones ni tipos.
* **Coherencia de operadores:** El parser genera `":="` para asignaciones y `"DECL"` con la lista de identificadores en el hijo derecho. El generador conserva compatibilidad con los nombres anteriores `"="`, `"DECLARACION"` y `"MOSTRAR_TEXTO"`; las declaraciones registran sus variables por ámbito sin emitir instrucciones, y las firmas de `"FUNCION"` no se traducen a la máquina de pila.
* **Sin optimizaciones:** La generación es puramente estructural. No realiza optimizaciones de código (como plegado de constantes).
* **Evaluación en cortocircuito:** El cortocircuito de las condiciones (`Y` / `O`) se delega al Generador, el cual debe emitir los saltos correspondientes (`BF` y `BI`) respetando las listas de verdaderos y falsos.


