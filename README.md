# Compilador GAUCHO

Trabajo práctico universitario de desarrollo de compiladores (Grupo D). El proyecto implementa un compilador educativo para el lenguaje fuente GAUCHO, escrito en Java.

## Alcance actual

- **Análisis léxico:** reconoce palabras reservadas, identificadores, constantes, operadores y delimitadores.
- **Análisis sintáctico:** usa Java CUP para reconocer funciones, bloques, declaraciones, asignaciones, condiciones y estructuras de control.
- **Análisis semántico:** mantiene tabla de símbolos y ámbitos; detecta identificadores no declarados, declaraciones duplicadas y más de tres niveles de `si`/`bucle`. Comprueba llamadas a funciones de un argumento.
- **Código intermedio:** genera Polaca Inversa para expresiones aritméticas, asignaciones, comparaciones, llamadas y control de flujo. Las condiciones `y`/`o` usan saltos `BF`/`BI` para cortocircuito.

La Polaca Inversa es la salida intermedia del proyecto; actualmente no se ejecuta. La generación de código Assembler y su ejecución quedan para una etapa posterior.

## Limitación de precedencia lógica

La precedencia entre `y` y `o` cuando aparecen mezclados sin paréntesis todavía no está definida. Para expresar claramente la intención, agrupa las condiciones con paréntesis, por ejemplo:

```gaucho
si ((a > 0 y b > 0) o c == 1)
{
    mostrar(a)
}
```

## Requisitos

- JDK con `java` y `javac`.
- Bash.
- Java CUP `11b-20160615`, disponible en `Compilador/cup.jar`.

## Compilar y ejecutar

Desde Bash, cambia al directorio `Compilador/`:

```bash
cd Compilador
java -cp cup.jar java_cup.Main -package sintactico -parser Parser -symbols sym -destdir src/sintactico src/sintactico/parser.cup
mkdir -p out
javac -encoding UTF-8 -cp cup.jar -d out src/Main.java src/lexico/*.java src/sintactico/*.java
java -cp 'out;cup.jar' Main
```

`Main` analiza `prueba-sintactico.txt` por defecto. Para analizar otro archivo:

```bash
java -cp 'out;cup.jar' Main ruta/al/archivo.txt
```

El JAR de CUP no declara `Main-Class`, por eso se invoca mediante `java -cp cup.jar java_cup.Main`. El separador `;` del classpath corresponde a Java en Windows, incluso al ejecutar desde Git Bash.

## Pruebas de regresión

Después de generar CUP y compilar las fuentes:

```bash
javac -encoding UTF-8 -cp 'out;cup.jar' -d out tests/FrontendRegressionTest.java
java -cp 'out;cup.jar' FrontendRegressionTest
```

La suite comprueba el lexer, EOF, precedencia aritmética, ámbitos, validaciones semánticas, llamadas, recuperación y destinos de salto. La validación de cortocircuito es estructural: no hay todavía una máquina virtual que ejecute la Polaca.

## Archivos principales

- `Compilador/src/lexico/`: analizador léxico y sus matrices.
- `Compilador/src/sintactico/parser.cup`: gramática CUP y acciones semánticas.
- `Compilador/src/sintactico/`: adaptador CUP, tabla de símbolos, ámbitos, Polaca Inversa y clases auxiliares.
- `Compilador/tests/FrontendRegressionTest.java`: pruebas de regresión del frontend.
- `specs/`: especificaciones y documentación del trabajo.
