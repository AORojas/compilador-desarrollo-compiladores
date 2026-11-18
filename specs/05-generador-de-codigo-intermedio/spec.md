# Especificación Técnica: Generador de Código Intermedio

## 1. Objetivo y Alcance

El Generador de Código Intermedio recibe el Árbol de Sintaxis Abstracta (AST) y lo traduce a una representación lineal llamada **Polaca Inversa**. 

Su única responsabilidad es emitir las instrucciones (operandos, operadores y saltos). No valida variables ni ámbitos (eso ya lo hizo el Semántico) y no genera código Assembler.

La implementación se encuentra organizada en el paquete `gc`, distribuida en:
- `GeneradorCodigoIntermedio`: coordina el recorrido del AST y emite las instrucciones.
- `PolacaInversa`: estructura de datos que almacena las instrucciones y gestiona el backpatching.

## 2. Referencias y Rutas del Proyecto

*   **Estructura del AST (Obligatorio):** `Compilador/src/ast/` (Clases `Nodo`, `Hoja`, `NodoAST`).
*   **Diseño del lenguaje:** `specs/01-diseño/spec.md`.
*   **Especificación del Analizador Semántico:** `specs/04-analizador-semantico/spec.md`.

## 3. Estructura de Datos (La Polaca Inversa)

La clase `PolacaInversa` encapsula una lista (`ArrayList<Object>`) y maneja cuatro tipos de elementos:
1.  **`Referencia` (int):** ID numérico de un símbolo.
2.  **`String`:** Operadores (`+`, `:=`, `PRINT`).
3.  **`DestinoPendiente`:** Un "hueco" reservado para un salto no resuelto.
4.  **`DestinoSalto` (int):** La posición final a la que salta una instrucción.

### 3.0. Formato lineal de la Polaca

Cada referencia y cada operador se representa entre corchetes. Los operandos se
formatean como `[ID]` y los operadores como `[+]`, `[:=]`, `[BF]`, `[BI]`, etc.

Cuando un salto queda resuelto, su destino no se representa entre corchetes:
se imprime inmediatamente después del operador como `->N`. Por ejemplo:

```text
[1] [3] [>] [BF] ->16 [BI] ->9
```

En este ejemplo, `BF` salta a la posición `16` y `BI` salta a la posición `9`.
La notación `[16]` no representa un destino de salto.

### 3.1. Métodos de Backpatching
- `addSaltoPendiente(String operador)`: Agrega el operador de salto y un hueco, devolviendo el índice del hueco.
- `patch(int posicionHueco, int destino)`: Rellena el hueco con el destino real.
- `addSaltoIncondicional(int destino)`: Agrega un operador `BI` con el destino ya resuelto.
- `getPos()`: Devuelve el índice actual de la lista.
- `resolverDestinos()`: Verifica al final que no queden `DestinoPendiente`.

## 4. Recorrido del AST y Traducción (Algoritmo)

La clase `GeneradorCodigoIntermedio` recorre el árbol en **Post-Orden** (primero hijos, luego raíz). A medida que lo recorre, emite instrucciones:

### 4.1. Operandos (Hojas)
Al visitar una `Hoja`, el generador debe buscar su ID en la `TablaSimbolos`. 
*   Para saber si es una constante o una variable, se evalúa el valor con la expresión regular `[0-9]+`. 
*   Si es número, se busca como constante. Si es texto, se busca como variable.
*   Se emite `polaca.addReferencia(id)`.

### 4.2. Expresiones y Asignaciones (Nodos)
Dado que `Nodo` tiene `izquierdo` y `derecho` (que pueden ser nulos), se valida cada acceso:
*   **Operadores Aritméticos (`+`, `-`, `*`, `/`):** Procesar `izquierdo`, procesar `derecho` y emitir `polaca.addOperador(nodo.operador)`.
*   **Asignación (`=`):** Procesar `derecho` (expresión), emitir referencia de `izquierdo`, y emitir `polaca.addOperador(":=")`.
*   **`MOSTRAR` y `RETORNAR`:** Procesar `izquierdo` y emitir `polaca.addOperador("PRINT")` o `"RET"`.
*   **Llamadas a funciones (`LLAMADA`):** Procesar `derecho` (argumento), emitir referencia de `izquierdo` (nombre de la función) y emitir `polaca.addOperador("CALL")`.

### 4.3. Estructuras de Control (Backpatching)
*   **`SI`:**
  1. Procesar condición (`izquierdo`).
  2. Emitir `int saltoFalso = polaca.addSaltoPendiente("BF")`.
  3. Procesar bloque (`derecho`).
  4. Hacer `polaca.patch(saltoFalso, polaca.getPos())`.

*   **`BUCLE` (Repeat-Until):**
  1. Guardar `int inicio = polaca.getPos()`.
  2. Procesar bloque (`izquierdo`).
  3. Procesar condición (`derecho`).
  4. Emitir un salto condicional en falso (`BF`) hacia la posición de `inicio` (`polaca.patch(polaca.addSaltoPendiente("BF"), inicio)`).

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

## 6. Fragmentos Clave de Implementación

El método `generar(NodoAST nodo)` delega en un recorrido recursivo. Esqueleto exacto:

```java
public void generar(NodoAST raiz) {
    if (raiz != null) {
        procesarNodo(raiz);
    }
}

private void procesarNodo(NodoAST nodo) {
    if (nodo instanceof Hoja) {
        emitirHoja((Hoja) nodo);
    } else if (nodo instanceof Nodo) {
        Nodo n = (Nodo) nodo;
        
        // Expresiones binarias (Aritméticas y Relacionales)
        if (n.operador.equals("+") || n.operador.equals("-") || 
            n.operador.equals("*") || n.operador.equals("/") ||
            n.operador.equals(">") || n.operador.equals("<") || 
            n.operador.equals(">=") || n.operador.equals("<=") || 
            n.operador.equals("==") || n.operador.equals("<>")) {
            
            procesarNodo(n.izquierdo);
            procesarNodo(n.derecho);
            polaca.addOperador(n.operador);
        }
        // Asignación
        else if (n.operador.equals("=")) {
            procesarNodo(n.derecho);
            emitirHoja((Hoja) n.izquierdo);
            polaca.addOperador(":=");
        }
        // Estructura SI
        else if (n.operador.equals("SI")) {
            procesarNodo(n.izquierdo);
            int saltoFalso = polaca.addSaltoPendiente("BF");
            procesarNodo(n.derecho);
            polaca.patch(saltoFalso, polaca.getPos());
        }
        // Estructura BUCLE (Repeat-Until)
        else if (n.operador.equals("BUCLE")) {
            int inicio = polaca.getPos();
            procesarNodo(n.izquierdo); // Bloque
            procesarNodo(n.derecho);   // Condición
            
            // Si es falso, vuelve al inicio.
            int saltoAlInicio = polaca.addSaltoPendiente("BF"); 
            polaca.patch(saltoAlInicio, inicio);
        }
        // Mostrar (PRINT)
        else if (n.operador.equals("MOSTRAR")) {
            procesarNodo(n.izquierdo);
            polaca.addOperador("PRINT");
        }
        // Retornar (RET)
        else if (n.operador.equals("RETORNAR")) {
            procesarNodo(n.izquierdo);
            polaca.addOperador("RET");
        }
        // Llamadas a funciones
        else if (n.operador.equals("LLAMADA")) {
            procesarNodo(n.derecho); // Argumento
            emitirHoja((Hoja) n.izquierdo); // Nombre de la función
            polaca.addOperador("CALL");
        }
        // Listas y otros nodos
        else {
            if (n.izquierdo != null) procesarNodo(n.izquierdo);
            if (n.derecho != null) procesarNodo(n.derecho);
        }
    }
}

private void emitirHoja(Hoja hoja) {
    // Si el valor es un número (regex), buscar constante. Si no, buscar variable.
    // int id = tablaSimbolos.buscarId(hoja.valor);
    // polaca.addReferencia(id);
}
```
* Nota sobre `emitirHoja`: El método `emitirHoja(Hoja hoja)` debe buscar el ID del símbolo en la Tabla de Símbolos. Si el valor de la hoja coincide con la expresión regular `[0-9]+`, se busca como constante; si no, se busca como variable. Finalmente, se emite `polaca.addReferencia(id)`.

## 7. Formato de Salida

El `Main` imprimirá la **Polaca Inversa** de forma lineal, mostrando los
operandos entre corchetes y los operadores tal cual. Los destinos resueltos de
los saltos `BF` y `BI` se muestran con el prefijo `->`, separado del operador
por un espacio.

**Ejemplo de salida para `x = 5 + 3`:**

```text
Polaca Inversa:
[2] [3] [+] [1] [:=]
```

> **Nota:** Donde `2` y `3` son los IDs de las constantes, y `1` es el ID de la variable `x`.

**Ejemplo de salida con saltos resueltos:**

```text
[1] [3] [>] [BF] ->16 [BI] ->9 [2] [PRINT]
```

Los elementos `BF` y `BI` siguen representándose como `[BF]` y `[BI]`. Sus
destinos son `16` y `9`, respectivamente, y por eso se escriben `->16` y `->9`,
sin corchetes.

---

### 8. Consideraciones y Límites de la Implementación

* **Validación previa:** El generador asume que el **AST** ya fue validado por el Semántico. No vuelve a chequear si una variable existe.
* **Sin optimizaciones:** La generación es puramente estructural. No realiza optimizaciones de código (como plegado de constantes).
* **Evaluación en cortocircuito:** El cortocircuito de las condiciones (`y` / `o`) se delega al Generador, el cual debe emitir los saltos correspondientes (`BF` y `BI`) respetando las listas de verdaderos y falsos.
