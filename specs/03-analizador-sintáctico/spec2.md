# Spec Arquitectura Completa: Sintáctico, Semántico y Polaca Inversa (CUP)

## 1. Estructura del Proyecto (Desde Cero)
El proyecto se organizará bajo el estándar de paquetes de Java, separando el análisis léxico del sintáctico/semántico.

```text
Compilador/src/
├── Main.java                     # Punto de entrada único
├── lexico/
│   ├── AnalizadorLexico.java     # A adaptar para CUP
│   ├── Clasificador.java
│   ├── ContextoLexico.java
│   ├── Funciones.java
│   ├── Lector.java
│   └── Matrices.java
└── sintactico/
    ├── parser.cup                # Gramática y Acciones (A crear)
    ├── Simbolo.java              # Modelo de Símbolo con puntero (A crear)
    ├── TablaSimbolos.java        # Gestor de la tabla (A crear)
    └── PolacaInversa.java        # Generador de código intermedio (A crear)
    # (Parser.java y sym.java serán generados por CUP automáticamente)


2. La Tabla de Símbolos y los Punteros (Semántico)
Para evitar guardar texto plano en la Polaca Inversa, cada variable o constante tendrá un ID numérico único (puntero).

2.1. Simbolo.java
Debe contener:

int id: El puntero numérico (0, 1, 2...).

String nombre: El lexema (ej. "x", "contador").

String tipo: "entero".

String clase: "variable", "constante", "funcion".

2.2. TablaSimbolos.java
Usar un Map<String, Simbolo> (como LinkedHashMap).

Debe tener un contador autoincremental int contadorId = 0.

Método registrar(String nombre, String tipo, String clase): Crea el Simbolo, le asigna el ID actual, incrementa el contador y lo guarda en el Map.

Método buscar(String nombre): Devuelve el Simbolo o null si no existe (para validar R1).

3. La Polaca Inversa (Código Intermedio)
La clase PolacaInversa.java debe manejar objetos (enteros para punteros, strings para operadores) y soportar backpatching.

List<Object> elementos.

addReferencia(int idSimbolo): Agrega el puntero entero.

addOperador(String op): Agrega operadores (ej. +, :=, BF).

getPos(): Devuelve el índice actual (para guardar dónde va el salto).

patch(int pos, int destino): Reemplaza un salto vacío en la posición pos con el índice destino.

print(): Imprime la polaca mostrando los punteros entre corchetes (ej. [0] [1] [+] [2] [:=]).

4. El Analizador Léxico (Adaptación para CUP)
El AnalizadorLexico.java actual debe implementar java_cup.runtime.Scanner y agregar el método next_token():

import java_cup.runtime.Symbol;
import sintactico.sym;

public class AnalizadorLexico implements AutoCloseable, java_cup.runtime.Scanner {
    // ... (tu código actual de yylex)

    public Symbol next_token() throws Exception {
        int token = yylex();
        String lexema = ctx.lexemaActual;
        switch (token) {
            case 256: return new Symbol(sym.ID, lexema);
            case 257: return new Symbol(sym.CTE, lexema);
            case 260: return new Symbol(sym.ENTERO);
            case 276: return new Symbol(sym.EOF);
            // ... mapear el resto de los tokens (259 al 286)
            default:  return new Symbol(sym.error, lexema);
        }
    }
}

5. El Corazón: parser.cup
Este archivo contiene el Sintáctico (gramática) y el Semántico (validaciones y Polaca).

package sintactico;

import java_cup.runtime.*;
import java.util.*;

parser code {:
    public TablaSimbolos tabla = new TablaSimbolos();
    public PolacaInversa polaca = new PolacaInversa();
    public int nivelAnidamiento = 0; // Para regla R3

    public void syntax_error(Symbol s) {
        System.err.println("Error sintáctico en línea " + (s.left) + ". Token inesperado.");
    }
:};

/* TERMINALES */
terminal String ID;
terminal String CTE;
terminal ENTERO, PRINCIPAL, SI, BUCLE, HASTA, MOSTRAR, RETORNAR;
terminal ASIG, SUMA, RESTA, MULTIPLICACION, DIVISION;
terminal PAREN_IZQ, PAREN_DER, LLAVE_IZQ, LLAVE_DER;
terminal IGUAL, MENOR, MAYOR, MENOR_O_IGUAL, MAYOR_O_IGUAL, DISTINTO;

/* GRAMÁTICA Y SEMÁNTICA */
non terminal programa, bloque, sentencia, declaracion, asignacion, expresion, termino, factor, condicion;

programa ::= PRINCIPAL PAREN_IZQ PAREN_DER LLAVE_IZQ bloque LLAVE_DER;

bloque ::= sentencia 
         | bloque sentencia;

sentencia ::= declaracion 
            | asignacion 
            | condicional 
            | iteracion;

/* Declaración: entero x, y */
declaracion ::= ENTERO ID 
  {:
      if (tabla.buscar((String) ((Symbol)res_stack.peek()).value) != null) {
          System.err.println("Error R2: Variable ya declarada.");
      } else {
          tabla.registrar((String) ((Symbol)res_stack.peek()).value, "entero", "variable");
      }
  :}
  ;

/* Asignación con Punteros: x = y + 5 */
asignacion ::= ID ASIG expresion
  {:
      // Validación Semántica (R1)
      Simbolo s = tabla.buscar((String) ((Symbol)res_stack.peek()).value);
      if (s != null) {
          polaca.addOperador(":=");
          polaca.addReferencia(s.getId()); // Puntero de la variable destino
      } else {
          System.err.println("Error R1: Variable no declarada.");
      }
  :}
  ;

/* Expresiones Aritméticas */
expresion ::= expresion SUMA termino { : polaca.addOperador("+"); : }
            | expresion RESTA termino { : polaca.addOperador("-"); : }
            | termino;

termino ::= termino MULTIPLICACION factor { : polaca.addOperador("*"); : }
          | termino DIVISION factor { : polaca.addOperador("/"); : }
          | factor;

factor ::= ID 
  {:
      Simbolo s = tabla.buscar((String) ((Symbol)res_stack.peek()).value);
      if (s != null) {
          polaca.addReferencia(s.getId()); // Guarda PUNTERO
      } else {
          System.err.println("Error R1: Variable no declarada.");
      }
  :}
  | CTE 
  {:
      Simbolo s = tabla.registrar((String) ((Symbol)res_stack.peek()).value, "entero", "constante");
      polaca.addReferencia(s.getId()); // Guarda PUNTERO de la constante
  :}
  | PAREN_IZQ expresion PAREN_DER;

6. El Punto de Entrada (Main.java)
El Main ya no lee tokens manualmente. Solo inicializa el Léxico, se lo pasa al Parser generado por CUP, y muestra los resultados.

import lexico.AnalizadorLexico;
import sintactico.Parser;

public class Main {
    public static void main(String[] args) throws Exception {
        AnalizadorLexico lexer = new AnalizadorLexico("prueba.txt");
        
        // El Parser se genera automáticamente con CUP
        Parser parser = new Parser(lexer); 
        parser.parse();

        System.out.println("\n--- POLACA INVERSA ---");
        parser.polaca.print();

        System.out.println("\n--- TABLA DE SÍMBOLOS ---");
        parser.tabla.imprimir();
    }
}


7. Compilación y Ejecución (Flujo CUP)
Dado que Java CUP genera código, el orden de compilación cambia. Debes descargar cup.jar y ponerlo en la raíz de tu proyecto.

1. Generar el Parser con CUP:

java -jar cup.jar -parser Parser -symbols sym src/sintactico/parser.cup
(Esto creará Parser.java y sym.java en src/sintactico/)

2. Compilar todo el proyecto:
 
javac -encoding UTF-8 -d out src/Main.java src/lexico/*.java src/sintactico/*.java

3. Ejecutar:

java -cp out Main


