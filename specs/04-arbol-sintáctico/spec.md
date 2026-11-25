# Especificación Técnica: Árbol de Sintaxis Abstracta (AST)

## 1. Objetivo y Alcance
Esta especificación define el diseño del **Árbol de Sintaxis Abstracta (AST)** para el lenguaje GAUCHO y su integración mediante acciones semánticas invocadas desde el Analizador Sintáctico (Java CUP).

Su única responsabilidad es modelar la estructura jerárquica del código fuente mediante nodos y hojas independientes del analizador, sirviendo como entrada para la siguiente fase del compilador.

**Límites estrictos:**
* **No** valida gramática (definido en `specs/03-analizador-sintáctico/spec.md`).
* **No** valida variables ni maneja ámbitos (tarea del Analizador Semántico).
* **No** genera código intermedio (Polaca).

---

## 2. Contexto del Proyecto y Referencias

| Ruta / Archivo | Propósito |
| :--- | :--- |
| `specs/03-analizador-sintáctico/spec.md` | **Referencia del Sintáctico:** Define la integración con CUP; la gramática canónica está en `specs/01-diseño/spec.md`. |
| `Compilador/src/ast/` | **Directorio del AST:** Paquete independiente donde se implementan las clases e interfaces del árbol (`NodoAST.java`, `Nodo.java`, `Hoja.java`). |
| `Compilador/src/sintactico/parser.cup` | **Punto de acoplamiento:** Archivo de Java CUP donde se conectan las acciones semánticas que construyen el árbol durante cada reducción. |

---

## 3. Diseño de Clases (Paquete `ast`)
Se debe crear el paquete `ast` dentro de `Compilador/src/ast/` como un módulo independiente con una estructura polimórfica mínima para representar los elementos del programa:

* **`NodoAST` (Interfaz):** Actúa como tipo base común y polimórfico que implementan todos los elementos del árbol sin requerir métodos obligatorios.
* **`Hoja` (Clase):** Implementa `NodoAST` y representa los valores terminales del lenguaje. Contiene un atributo público de tipo texto llamado `valor` (por ejemplo: `"x"`, `"42"`, `"Hola mundo"`) que se inicializa mediante su constructor.
* **`Nodo` (Clase):** Implementa `NodoAST` y representa operadores, sentencias o estructuras de control. Contiene tres atributos públicos: un texto llamado `operador` (por ejemplo: `"+"`, `":="`, `"SI"`, `">"`) y dos referencias de tipo `NodoAST` llamadas `izquierdo` y `derecho`, todos inicializados mediante su constructor.

---

## 4. Acoplamiento con el Analizador Sintáctico (`parser.cup`)
Para construir el árbol de forma desacoplada durante el análisis sintáctico, las reglas definidas en `parser.cup` invocan acciones semánticas en cada reducción que instancian y retornan objetos de tipo `NodoAST`.

### 4.1. Configuración y Tipado en CUP
En la cabecera de `parser.cup` se importa el paquete `ast` y se tipan los no terminales que construyen el árbol:

```cup
import ast.*;

non terminal NodoAST programa, lista_funciones, funcion, bloque, sentencias;
non terminal NodoAST sentencia, declaracion, lista_ids, asignacion;
non terminal NodoAST seleccion, iteracion, salida, retorno;
non terminal NodoAST condicion, cond_bloque, expresion, termino, factor;
```

### 4.2. Expresiones Aritméticas y Llamadas

```cup
expresion ::= expresion:e SUMA termino:t      {: RESULT = new Nodo("+", e, t); :}
            | expresion:e RESTA termino:t     {: RESULT = new Nodo("-", e, t); :}
            | termino:t                       {: RESULT = t; :} ;

termino ::= termino:t MULTIPLICACION factor:f {: RESULT = new Nodo("*", t, f); :}
          | termino:t DIVISION factor:f       {: RESULT = new Nodo("/", t, f); :}
          | factor:f                          {: RESULT = f; :} ;

factor ::= PAREN_IZQ expresion:e PAREN_DER          {: RESULT = e; :}
         | ID:nombre PAREN_IZQ expresion:arg PAREN_DER {: RESULT = new Nodo("LLAMADA", new Hoja(nombre), arg); :}
         | ID:nombre                                {: RESULT = new Hoja(nombre); :}
         | CTE:valor                                {: RESULT = new Hoja(valor); :} ;
```

### 4.3. Declaraciones, Asignaciones y Condiciones

```cup
declaracion ::= ENTERO lista_ids:ids {: RESULT = new Nodo("DECL", new Hoja("ENTERO"), ids); :} ;

lista_ids ::= lista_ids:lista COMA ID:nombre {: RESULT = new Nodo(",", lista, new Hoja(nombre)); :}
            | ID:nombre                      {: RESULT = new Hoja(nombre); :} ;

asignacion ::= ID:nombre ASIG expresion:e 
    {: RESULT = new Nodo(":=", new Hoja(nombre), e); :} ;

cond_bloque ::= expresion:e1 MAYOR expresion:e2         {: RESULT = new Nodo(">", e1, e2); :}
              | expresion:e1 IGUAL expresion:e2         {: RESULT = new Nodo("==", e1, e2); :}
              | expresion:e1 MENOR expresion:e2         {: RESULT = new Nodo("<", e1, e2); :}
              | expresion:e1 DISTINTO expresion:e2      {: RESULT = new Nodo("<>", e1, e2); :}
              | expresion:e1 MENOR_O_IGUAL expresion:e2 {: RESULT = new Nodo("<=", e1, e2); :}
              | expresion:e1 MAYOR_O_IGUAL expresion:e2 {: RESULT = new Nodo(">=", e1, e2); :}
              | PAREN_IZQ condicion:c PAREN_DER         {: RESULT = c; :} ;

condicion ::= condicion:c1 Y cond_bloque:c2 {: RESULT = new Nodo("Y", c1, c2); :}
            | condicion:c1 O cond_bloque:c2 {: RESULT = new Nodo("O", c1, c2); :}
            | cond_bloque:c                 {: RESULT = c; :} ;
```

### 4.4. Estructura del Programa, Bloques y Control

```cup
programa ::= opcional_fin_linea lista_funciones:lf opcional_fin_linea 
    {: RESULT = new Nodo("PROGRAMA", lf, null); :} ;

lista_funciones ::= lista_funciones:lf opcional_fin_linea funcion:f 
                      {: RESULT = new Nodo("FUNCIONES", lf, f); :}
                  | funcion:f 
                      {: RESULT = f; :} ;

funcion ::= ENTERO ID:nombre PAREN_IZQ ENTERO ID:param PAREN_DER opcional_fin_linea bloque:b 
              {: 
                 Nodo datosFunc = new Nodo(nombre, new Hoja(param), null); 
                 RESULT = new Nodo("FUNCION", datosFunc, b); 
              :}
          | ENTERO PRINCIPAL PAREN_IZQ PAREN_DER opcional_fin_linea bloque:b 
              {: 
                 Nodo datosFunc = new Nodo("PRINCIPAL", null, null); 
                 RESULT = new Nodo("FUNCION", datosFunc, b); 
              :} ;

bloque ::= LLAVE_IZQ fin_lineas sentencias:s LLAVE_DER 
    {: RESULT = new Nodo("BLOQUE", s, null); :} ;

sentencias ::= sentencias:s1 sentencia:s2 {: RESULT = new Nodo("SENTENCIAS", s1, s2); :}
             | sentencia:s                {: RESULT = s; :} ;

sentencia ::= declaracion:d fin_lineas {: RESULT = d; :}
            | asignacion:a fin_lineas  {: RESULT = a; :}
            | seleccion:s fin_lineas   {: RESULT = s; :}
            | iteracion:i fin_lineas   {: RESULT = i; :}
            | salida:o fin_lineas      {: RESULT = o; :}
            | retorno:r fin_lineas     {: RESULT = r; :}
            | error fin_lineas         {: RESULT = null; :} ;

seleccion ::= SI PAREN_IZQ condicion:c PAREN_DER opcional_fin_linea bloque:b 
    {: RESULT = new Nodo("SI", c, b); :} ;

iteracion ::= BUCLE opcional_fin_linea bloque:b HASTA PAREN_IZQ condicion:c PAREN_DER 
    {: RESULT = new Nodo("BUCLE", b, c); :} ;

salida ::= MOSTRAR PAREN_IZQ expresion:e PAREN_DER 
             {: RESULT = new Nodo("MOSTRAR", e, null); :}
         | MOSTRAR_TXT PAREN_IZQ LITERAL_TXT:txt PAREN_DER 
             {: RESULT = new Nodo("MOSTRAR_TXT", new Hoja(txt), null); :} ;

retorno ::= RETORNAR expresion:e 
    {: RESULT = new Nodo("RETORNAR", e, null); :} ;
```


---

## 5. Criterios de Aceptación y Pruebas

* **Compilación:** CUP genera el parser con las acciones semánticas acopladas sin conflictos.
* **Extracción del AST:** La clase principal (`Main.java`) recupera el `NodoAST` raíz tras invocar el método de análisis del parser y extraer el valor del símbolo resultante.
* **Impresión del Árbol:** Se implementa un recorrido en consola que valida la jerarquía generada.

**Ejemplo de salida esperada para `x := 5 + 3`:**
```text
Nodo(:=)
├── Hoja(x)
└── Nodo(+)
    ├── Hoja(5)
    └── Hoja(3)
```
