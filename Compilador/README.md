# Compilador GAUCHO

Analizador léxico y sintáctico para GAUCHO, implementado en Java. El análisis sintáctico usa Java CUP y el analizador léxico existente.

## Requisitos

- JDK instalado, con `javac` y `java` disponibles en la terminal.
- Bash.
- Java CUP `11b-20160615`, incluido como `cup.jar`.

## Estructura

```text
Compilador/
├── src/
│   ├── Main.java
│   ├── lexico/
│   └── sintactico/
│       ├── LexerCupAdapter.java
│       ├── Simbolo.java
│       ├── TablaSimbolos.java
│       ├── GestorAmbitos.java
│       ├── PolacaInversa.java
│       ├── ErrorSemantico.java
│       ├── Condicion.java
│       └── parser.cup
├── cup.jar
├── prueba-sintactico.txt
├── prueba.txt
└── prueba-lexemas.txt
```

CUP genera `Parser.java` y `sym.java` a partir de `src/sintactico/parser.cup`.

## Compilar y ejecutar desde Bash

Ejecuta estos comandos desde `Compilador/`:

```bash
java -cp cup.jar java_cup.Main -package sintactico -parser Parser -symbols sym -destdir src/sintactico src/sintactico/parser.cup
mkdir -p out
javac -encoding UTF-8 -cp cup.jar -d out src/Main.java src/lexico/*.java src/sintactico/*.java
java -cp "out;cup.jar" Main
```

El JAR de CUP no declara `Main-Class`; invoca el generador con `java -cp cup.jar java_cup.Main`, no con `java -jar`. El classpath usa `;`, separador de Java en Windows, también al ejecutar desde Bash.

`Main` analiza `prueba-sintactico.txt` por defecto. Para elegir otro archivo:

```bash
java -cp "out;cup.jar" Main ruta/al/archivo.txt
```

## Pruebas de regresión del frontend

Después de generar CUP y compilar el proyecto, ejecuta desde `Compilador/`:

```bash
javac -encoding UTF-8 -cp "out;cup.jar" -d out tests/FrontendRegressionTest.java
java -cp "out;cup.jar" FrontendRegressionTest
```

La suite comprueba tokens adyacentes y EOF, precedencia aritmética, ámbitos, errores semánticos, llamadas, recuperación y destinos de salto para condiciones agrupadas y ciclos. Verifica estructuralmente la Polaca; el proyecto todavía no incluye una máquina que la ejecute.

## Pruebas léxicas

`prueba-lexemas.txt` contiene identificadores, constantes, operadores, delimitadores, texto y un comentario:

```text
entero contador = 12
contador < 20
contador > 0
contador + 3 - 1
(contador == 14)
{contador, 12}
contador * 2
mostrar("prueba de lexemas")
/* comentario de prueba */
```

Los saltos de línea son el token `286`; el fin de archivo léxico es `276`. Identificadores, constantes numéricas y literales entre comillas usan los tokens `256`, `257` y `258`, respectivamente.

## Componentes principales

- **`Main`**: conecta el analizador léxico con CUP, ejecuta el parser y reporta errores.
- **`AnalizadorLexico`**: reconoce tokens y conserva sus lexemas y ubicaciones.
- **`LexerCupAdapter`**: convierte códigos léxicos en `java_cup.runtime.Symbol`; pasa los lexemas de identificadores, constantes y literales.
- **`Simbolo`, `TablaSimbolos` y `GestorAmbitos`**: conservan identificadores únicos y resuelven variables por ámbito.
- **`PolacaInversa`**: representa referencias, operadores y destinos de salto pendientes.
- **`Condicion`**: conserva las listas de saltos verdaderos y falsos para el backpatching de condiciones.
- **`parser.cup`**: define funciones, bloques, sentencias, expresiones aritméticas y condiciones, y ejecuta sus acciones semánticas.
- **`Lector`, `Clasificador`, `Matrices` y `Funciones`**: implementan la lectura y el reconocimiento léxico.

El parser valida sintaxis, identificadores declarados (R1), duplicados en el mismo ámbito (R2) y hasta tres niveles anidados de `si`/`bucle` (R3). Los ámbitos de `si` y `bucle` son locales; se permite ocultar nombres de ámbitos externos, pero no repetir el nombre de un parámetro en el ámbito de su función. No hay variables globales. La tabla registra variables, funciones y constantes; la Polaca usa referencias a símbolos y saltos resueltos mediante backpatching. Las condiciones `y`/`o` generan saltos de cortocircuito, y las llamadas de un argumento generan `CALL` con resolución diferida de funciones. Si hay errores, se muestra la tabla, pero no se presenta la Polaca como válida.

La salida intermedia no se ejecuta todavía: la validación de cortocircuito comprueba el grafo de flujo representado por los destinos `BF`/`BI`. La precedencia entre `y` y `o` sin paréntesis continúa pendiente de confirmación; usa paréntesis para fijar explícitamente el agrupamiento.
