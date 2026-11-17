# Especificación Técnica: Analizador Sintáctico y AST (GAUCHO)

## 1. Objetivo y Alcance
Esta especificación define la construcción del **Analizador Sintáctico** para el lenguaje GAUCHO. Implementado con Java CUP, su única responsabilidad es validar la estructura del código fuente y construir un **Árbol de Sintaxis Abstracta (AST)**.

**Límites estrictos:** El Sintáctico **no** valida variables (eso es tarea del Semántico), **no** maneja ámbitos ni genera código intermedio (Polaca). Su salida es el AST, el cual servirá de entrada para la siguiente fase del compilador.

---

## 2. Contexto del Proyecto y Referencias
Antes de implementar el Sintáctico, es obligatorio consultar los siguientes archivos y rutas del repositorio para entender el contexto, la gramática oficial y la interfaz de conexión:

| Ruta / Archivo | Propósito |
| :--- | :--- |
| `specs/01-diseño/spec.md` | **Referencia principal:** Define la gramática oficial, los tokens y las reglas del lenguaje GAUCHO. |
| `specs/02-analizador-lexico/spec.md` | **Referencia de Léxico:** Explica cómo funciona el analizador léxico (tokens, autómatas). |
| `Compilador/src/lexico/` | **Implementación del Léxico:** Carpeta que contiene el motor del léxico (`AnalizadorLexico.java`, `Lector.java`, `Matrices.java`, etc.). |
| `Compilador/src/sintactico/` | **Directorio de trabajo:** Carpeta donde se deben crear las nuevas clases del Sintáctico y el archivo `parser.cup`. |

**Nota:** El Analizador Léxico ya está implementado y **no debe ser modificado**. Toda la comunicación se realizará mediante un adaptador.

---

## 3. Integración con el Analizador Léxico
Para que Java CUP consuma los tokens del Léxico (que son códigos `int`), se debe crear la clase `LexerCupAdapter.java` dentro de `src/sintactico/`, implementando la interfaz `java_cup.runtime.Scanner`.

*   **Mapeo:** Convierte el `int` devuelto por el léxico (ej: `256`) al terminal de CUP (`sym.ID`).
*   **Transporte de Datos:** Para los tokens `ID`, `CTE` y `LITERAL_TXT`, extrae el lexema (`String`) del contexto léxico y lo adjunta al objeto `Symbol`. Los demás tokens no llevan lexema.
*   **Ubicación:** Adjunta la línea y columna al `Symbol` para reportar errores con precisión.
*   **EOF:** Al recibir el fin de archivo (`-1`), devuelve el token `sym.EOF`.

---

## 4. Diseño del Árbol de Sintaxis Abstracta (AST)
Se debe crear el paquete `ast` (en `src/ast/`) con la estructura mínima para representar el programa.

### 4.1. `NodoAST` (Interfaz)
Base polimórfica para todos los elementos.
```java
package ast;
public interface NodoAST {}
```

### 4.2. `Hoja` (Clase)
Representa los valores terminales.

```java
package ast;

public class Hoja implements NodoAST {
    public String valor; // Ej: "x", "42", "Hola mundo"
    public Hoja(String valor) { this.valor = valor; }
}
```

### 4.3. `Nodo` (Clase)
Representa operadores y estructuras.

```java
package ast;

public class Nodo implements NodoAST {
    public String operador; // Ej: "+", ":=", "SI", ">"
    public NodoAST izquierdo;
    public NodoAST derecho;

    public Nodo(String operador, NodoAST izquierdo, NodoAST derecho) {
        this.operador = operador;
        this.izquierdo = izquierdo;
        this.derecho = derecho;
    }
}
```

---

## 5. Reglas Gramaticales y Construcción del AST (parser.cup)
A continuación se detalla cómo cada regla gramatical construye el AST. Las acciones semánticas solo deben instanciar nodos.

### 5.1. Expresiones Aritméticas

```java
precedence left Y, O;
precedence left IGUAL, DISTINTO, MENOR, MENOR_O_IGUAL, MAYOR, MAYOR_O_IGUAL;
precedence left SUMA, RESTA;
precedence left MULTIPLICACION, DIVISION;

expresion ::= expresion:e SUMA termino:t     {: RESULT = new Nodo("+", e, t); :};
expresion ::= expresion:e RESTA termino:t    {: RESULT = new Nodo("-", e, t); :};
expresion ::= termino:t                      {: RESULT = t; :};

termino ::= termino:t MULTIPLICACION factor:f {: RESULT = new Nodo("*", t, f); :};
termino ::= termino:t DIVISION factor:f       {: RESULT = new Nodo("/", t, f); :};
termino ::= factor:f                          {: RESULT = f; :};

factor ::= PAREN_IZQ expresion:e PAREN_DER   {: RESULT = e; :};
factor ::= ID:nombre                         {: RESULT = new Hoja(nombre); :};
factor ::= CTE:valor                         {: RESULT = new Hoja(valor); :};
```

### 5.2. Asignaciones y Condiciones

```java
asignacion ::= ID:nombre ASIG expresion:e {: RESULT = new Nodo(":=", new Hoja(nombre), e); :};

cond_bloque ::= expresion:e1 MAYOR expresion:e2 {: RESULT = new Nodo(">", e1, e2); :};
cond_bloque ::= expresion:e1 IGUAL expresion:e2 {: RESULT = new Nodo("==", e1, e2); :};
// (Añadir <, <>, <=, >=)

condicion ::= condicion:c1 Y cond_bloque:c2 {: RESULT = new Nodo("Y", c1, c2); :};
condicion ::= condicion:c1 O cond_bloque:c2 {: RESULT = new Nodo("O", c1, c2); :};
condicion ::= cond_bloque:c                 {: RESULT = c; :};
```

### 5.3. Estructuras de Control y Funciones

```java
seleccion ::= SI PAREN_IZQ condicion:c PAREN_DER bloque:b 
    {: RESULT = new Nodo("SI", c, b); :};

iteracion ::= BUCLE bloque:b HASTA PAREN_IZQ condicion:c PAREN_DER 
    {: RESULT = new Nodo("BUCLE", b, c); :};

retorno ::= RETORNAR expresion:e 
    {: RESULT = new Nodo("RETORNAR", e, null); :};

salida ::= MOSTRAR PAREN_IZQ expresion:e PAREN_DER 
    {: RESULT = new Nodo("MOSTRAR", e, null); :};

funcion ::= ENTERO ID:nombre PAREN_IZQ ENTERO ID:param PAREN_DER bloque:b 
    {: 
       Nodo datosFunc = new Nodo(nombre, new Hoja(param), null); 
       RESULT = new Nodo("FUNCION", datosFunc, b); 
    :};
```

---

## 6. Manejo de Errores Sintácticos
El Sintáctico solo reporta errores de gramática (código E3). Utilizará el token `error` de CUP para recuperarse sin abortar la compilación completa.

**Recuperación:** Si falla una sentencia, el parser descarta tokens hasta encontrar un `FIN_DE_LINEA` para retomar en la siguiente.

```java
sentencia ::= asignacion | seleccion | iteracion | salida | retorno
            | error FIN_DE_LINEA {: System.err.println("Error sintáctico: línea ignorada."); :};
```

---

## 7. Criterios de Aceptación y Pruebas

* **Compilación:** CUP genera el parser sin conflictos.
* **Extracción del AST:** El `Main.java` recupera el `NodoAST` raíz tras llamar a `parse()`.
* **Impresión del Árbol:** Se implementa un recorrido que muestra el árbol en consola.

**Prueba con `x = 5 + 3`:**

```text
Nodo(=)
├── Hoja(x)
└── Nodo(+)
    ├── Hoja(5)
    └── Hoja(3)
```

