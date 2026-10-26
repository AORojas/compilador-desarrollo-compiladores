# Especificación final para la integración de CUP con el analizador léxico y la Polaca Inversa

## 1. Objetivo

Integrar Java CUP como analizador sintáctico en un compilador Java que ya cuenta con un analizador léxico propio.

El objetivo final es:

- reconocer el lenguaje definido por la gramática
- validar la estructura sintáctica del programa
- detectar errores sintácticos y semánticos
- construir la tabla de símbolos
- generar la Polaca Inversa como salida intermedia
- ejecutar todo desde el `Main.java` existente como punto único de entrada del compilador

---

## 2. Arquitectura general

```text
Código fuente
      │
      ▼
Analizador léxico (AnalizadorLexico)
      │
      ▼
scanner() -> java_cup.runtime.Symbol
      │
      ▼
Parser CUP
      │
      ├── validación sintáctica
      ├── tabla de símbolos
      ├── validaciones semánticas
      └── generación de Polaca Inversa
```

La responsabilidad está separada de esta forma:

- léxico: reconoce tokens y devuelve `Symbol`
- sintáctico: valida la gramática
- semántico: valida alcance, tipos y funciones
- intermedia: genera la Polaca Inversa

---

## 3. Requisitos de implementación

### 3.1 Dependencia de CUP

El proyecto debe incluir la dependencia de Java CUP y la clase generada por el parser debe quedar en el paquete correspondiente.

### 3.2 Contrato del analizador léxico

El analizador léxico debe exponer una función con la firma:

```java
public Symbol scanner()
```

La clase debe devolver objetos del tipo `java_cup.runtime.Symbol` y no debe ser un simple `String` ni una estructura propia.

### 3.3 Requisitos mínimos del token

Cada token debe incluir:

- identificador del símbolo
- valor léxico (lexema)
- posición (línea y columna, recomendada)

Ejemplo:

```java
return new Symbol(sym.ID, lexema);
```

o, si se usa posición:

```java
return new Symbol(sym.ID, lexema, fila, columna);
```

---

## 4. Estructura de archivos

La estructura actual del proyecto es una compilación directa con `javac` y se mantiene bajo `Compilador/src`.

Por lo tanto, la ubicación real recomendada para el parser y sus artefactos es:

```text
Compilador/src/sintactico/
```

o, si se quiere mantener una organización más explícita pero sin romper el patrón del proyecto:

```text
Compilador/src/lexico/
Compilador/src/sintactico/
Compilador/src/semantica/
```

### 4.1 Archivos esperados

```text
Compilador/src/Main.java
Compilador/src/lexico/AnalizadorLexico.java
Compilador/src/sintactico/parser.cup
Compilador/src/sintactico/Parser.java
Compilador/src/sintactico/sym.java
Compilador/src/semantica/PolacaInversa.java
```

### 4.2 Paquetes

La compilación actual del proyecto se realiza con `javac` desde `Compilador/src`. La definición de paquetes debe evitarse mientras se mantiene esta estructura actual.

El `Main.java` debe seguir siendo el punto de entrada del programa, y se lo modifica para que inicialice el parser.

### 4.3 Regla importante

El `Main.java` actual es el punto de entrada correcto y debe adaptarse para ejecutar el parser. No se debe crear ningún entry point adicional.

---

## 5. Archivo CUP

El archivo `parser.cup` debe ir bajo:

```text
Compilador/src/sintactico/parser.cup
```

Debe incluir:

- imports de `java_cup.runtime.Symbol`
- `parser code`
- `action code`
- terminales
- no terminales
- precedencias
- gramática

Ejemplo base:

```cup
package compilador.sintactico;

import java_cup.runtime.*;

parser code {:
    public void report_error(String message, Object info) {
        System.err.println("Error sintáctico: " + message);
    }
:};

action code {:
    // funciones auxiliares
:};

terminal ENTERO, ID, ASIG, SUMA, RESTA, MULTIPLICACION, DIVISION;
terminal PAREN_IZQ, PAREN_DER, LLAVE_IZQ, LLAVE_DER, FIN_DE_LINEA;

non terminal programa, sentencia, expresion, termino, factor;

precedence left SUMA, RESTA;
precedence left MULTIPLICACION, DIVISION;

programa ::= sentencia;

sentencia ::= ID ASIG expresion FIN_DE_LINEA
            | expresion FIN_DE_LINEA
            ;

expresion ::= expresion SUMA termino
            | expresion RESTA termino
            | termino
            ;

termino ::= termino MULTIPLICACION factor
          | termino DIVISION factor
          | factor
          ;

factor ::= ID
         | ENTERO
         | PAREN_IZQ expresion PAREN_DER
         ;
```

---

## 6. Definición de terminales

Se deben mapear todos los tokens del analizador léxico.

### 6.1 Terminales con valor

```cup
terminal String ID;
terminal Integer CTE;
terminal String CADENA;
```

### 6.2 Terminales sin valor

```cup
terminal PRINCIPAL;
terminal ENTERO;
terminal SI;
terminal BUCLE;
terminal HASTA;
terminal MOSTRAR;
terminal RETORNAR;
terminal Y;
terminal O;
```

### 6.3 Operadores

```cup
terminal ASIG;
terminal IGUAL;
terminal DISTINTO;
terminal MENOR;
terminal MENOR_IGUAL;
terminal MAYOR;
terminal MAYOR_O_IGUAL;
```

### 6.4 Aritméticos

```cup
terminal SUMA;
terminal RESTA;
terminal MULTIPLICACION;
terminal DIVISION;
```

### 6.5 Delimitadores

```cup
terminal PAREN_IZQ;
terminal PAREN_DER;
terminal LLAVE_IZQ;
terminal LLAVE_DER;
terminal COMA;
terminal FIN_DE_LINEA;
```

---

## 7. Integración del scanner del léxico

El analizador léxico debe ser usado como fuente de tokens del parser.

### 7.1 Contrato requerido

```java
public class AnalizadorLexico implements java_cup.runtime.Scanner {
    public AnalizadorLexico(Reader in) { ... }
    public Symbol scanner() throws IOException { ... }
}
```

Eso permite que `Parser` haga esto:

```java
Parser parser = new Parser(lexer);
parser.parse();
```

### 7.2 Reglas de integración

- El parser no debe leer directamente del archivo fuente
- El parser no debe tener lógica de lectura del archivo
- El analizador léxico debe ser el único responsable de reconocer tokens
- La función `scanner()` es la interfaz que CUP usa para obtener cada token

---

## 8. Modificación del Main.java

El `Main.java` actual debe cambiar para que, además de testear el léxico, inicie la ejecución del parser.

### 8.1 Flujo esperado

```java
public class Main {
    public static void main(String[] args) {
        try {
            Reader reader = new FileReader("src/main/resources/prueba.gau");
            AnalizadorLexico lexer = new AnalizadorLexico(reader);
            Parser parser = new Parser(lexer);
            parser.parse();

            System.out.println("Análisis sintáctico finalizado.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

### 8.2 Funcionalidad

El `Main` debe:

1. abrir el archivo fuente
2. crear el analizador léxico
3. instanciar el parser con ese lexer
4. ejecutar `parse()`
5. mostrar la salida final de la Polaca Inversa por consola
6. reportar errores de compilación si los hubiera

---

## 9. Tabla de símbolos

Antes de generar código intermedio, debe existir una tabla de símbolos con la información mínima:

- nombre
- tipo
- alcance
- clase

Ejemplos:

```text
nombre = a
 tipo = ENTERO
 alcance = global
 clase = VARIABLE
```

```text
nombre = suma
 tipo = ENTERO
 alcance = global
 clase = FUNCION
```

Se debe validar:

- variables declaradas antes de usarse
- duplicación de nombres
- funciones inexistentes
- cantidad incorrecta de parámetros

---

## 10. Polaca Inversa

La Polaca Inversa deben generarse como representación intermedia del programa.

### 10.1 Estructura sugerida

```java
ArrayList<String>
```

Funciones esperadas:

- `add(String elemento)`
- `getPos()`
- `patch(int pos, String valor)`
- `print()`

### 10.2 Ejemplos

#### Variable

Entrada:

```text
a
```

Salida:

```text
a
```

#### Constante

Entrada:

```text
10
```

Salida:

```text
10
```

#### Operación

Entrada:

```text
a+b
```

Salida:

```text
a b +
```

#### Asignación

Entrada:

```text
a=b+c
```

Salida:

```text
b c + a :=
```

---

## 11. Condiciones y backpatching

Se debe soportar la generación de condiciones con asociadas a la Polaca Inversa.

### 11.1 Relacionales

```text
a>b
```

Salida:

```text
a b >
```

### 11.2 AND / OR

```text
a>b y b<c
```

Salida esperada conceptual:

```text
a b >
 b c <
 AND
```

```text
a>b o c>d
```

Salida esperada conceptual:

```text
a b >
 c d >
 OR
```

### 11.3 Backpatching

El compilador debe usar backpatching para condiciones y bucles. La estructura recomendada es:

```java
Stack<Integer>
```

Se debe:

1. evaluar condición
2. emitir salto condicional
3. guardar posición
4. procesar bloque
5. completar la dirección del salto

---

## 12. Bucle y llamadas a funciones

### 12.1 BUCLE-HASTA

Se debe soportar la estructura:

```text
bucle {
  ...
} hasta (a > 0)
```

Generación conceptual:

```text
L1:
...
condicion
BF L1
```

### 12.2 Llamadas a funciones

Ejemplo:

```text
duplicar(a)
```

Salida conceptual:

```text
a
CALL duplicar
```

Debe validarse:

- existencia de la función
- cantidad de parámetros
- tipo de retorno

---

## 13. Manejo de errores sintácticos

Debe implementarse un manejo de errores del parser.

### 13.1 Método sugerido

```java
public void syntax_error(Symbol cur_token) {
    // mostrar línea, token y símbolo esperado
}
```

Ejemplo:

```text
Error sintáctico en línea 15.
Se esperaba ')'.
Se encontró '{'.
```

---

## 14. Salida por consola

El programa debe entregar una salida final clara por consola.

### 14.1 Salida esperada

```text
Polaca Inversa:
 a b + c :=
```

o en caso de más de una expresión:

```text
Polaca Inversa:
 a b + c :=
 d e >
 AND
```

La salida debe mostrarse al finalizar el análisis, luego de `parse()` y de resolver las acciones semánticas.

---

## 15. Generación del parser con CUP

Para generar el parser y los símbolos, se debe ejecutar la herramienta de CUP desde el proyecto.

### 15.1 Comando conceptual

```bash
java -jar cup.jar -parser Parser -symbols sym parser.cup
```

### 15.2 Resultado esperado

Se deben generar:

- `Parser.java`
- `sym.java`

y deben quedar en la carpeta:

```text
Compilador/src/sintactico/
```

### 15.3 Importante

La generación del parser debe integrarse con la compilación directa de Java (`javac`) y la estructura actual del proyecto.

---

## 16. Pruebas mínimas

Se deben crear casos simples que validen el flujo completo.

### 16.1 Expresiones

```text
a = b + c * 2;
```

### 16.2 Condicional

```text
si (a > b) {
}
```

### 16.3 Bucle

```text
bucle {
} hasta (a > 0)
```

### 16.4 Funciones

```text
entero doble(entero x) {
    retornar x * 2;
}
```

### 16.5 Integración

```text
Fuente -> Lexer -> Parser -> Tabla de símbolos -> Polaca Inversa -> Consola
```

---

## 17. Orden recomendado de implementación

1. Ajustar la estructura de paquetes y la carpeta `compilador/sintactico`.
2. Implementar o adaptar `AnalizadorLexico` para devolver `Symbol`.
3. Implementar la función `scanner()`.
4. Crear `parser.cup` con terminales, no terminales y precedencias.
5. Generar `Parser.java` y `sym.java` con CUP.
6. Modificar `Main.java` para crear el lexer, instanciar el parser y ejecutar `parse()`.
7. Crear la tabla de símbolos.
8. Implementar verificaciones semánticas.
9. Implementar la Polaca Inversa.
10. Implementar expresiones y asignaciones.
11. Implementar comparaciones y condiciones.
12. Implementar `IF` / `BUCLE-HASTA`.
13. Implementar llamadas a funciones.
14. Implementar manejo de errores sintácticos.
15. Mostrar la Polaca Inversa por consola.
16. Ejecutar pruebas unitarias y de integración.

---

## 18. Criterios de aceptación

La implementación se considera terminada cuando:

- el parser se genera con CUP sin errores
- el scanner del léxico cumple la interfaz de `java_cup.runtime.Scanner`
- el `Main.java` modifica la prueba actual y ejecuta el parser
- existe un único punto de entrada del compilador, el `Main.java`
- la gramática valida el lenguaje definido
- la tabla de símbolos se construye correctamente
- la Polaca Inversa se genera y se imprime en consola
- los errores sintácticos se reportan con línea y token esperado

---

## 19. Conclusión

La solución propuesta separa de manera clara la responsabilidad del reconocimiento léxico del análisis sintáctico y de la generación de la Polaca Inversa. El `Main.java` actúa como punto de ejecución del compilador, usando el scanner del analizador léxico como interfaz para CUP. Con ese diseño, el proyecto queda listo para evolucionar hacia validaciones semánticas, generación intermedia y, más adelante, generación de código objeto o ensamblador.
