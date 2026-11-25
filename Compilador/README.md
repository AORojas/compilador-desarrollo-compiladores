# Compilador GAUCHO

Compilador GAUCHO en Java CUP. El parser construye el AST; el analizador semántico valida declaraciones, ámbitos, llamadas a funciones y anidamiento; los generadores producen Polaca Inversa y Assembler, y GCC crea el ejecutable.

## Requisitos

- JDK instalado, con `javac` y `java` disponibles en la terminal.
- GCC para Windows (MinGW-w64), disponible en `PATH`.

## Generar, compilar y ejecutar desde CMD

Ejecuta estos comandos desde `Compilador/`:

```cmd
if not exist out mkdir out
javac -encoding UTF-8 -cp cup.jar -d out src\Main.java src\lexico\*.java src\ast\*.java src\sintactico\*.java src\semantico\*.java src\gc\*.java src\generadorasm\*.java
java -cp "out;cup.jar" Main test04a.gau
```

La ruta del fuente es obligatoria y puede ser relativa a `Compilador/` o absoluta. Si el análisis léxico, sintáctico y semántico termina sin errores, `Main` muestra el AST, la tabla de símbolos y la Polaca Inversa, y genera junto al fuente los archivos `.e`, `.s` y `.exe` con el mismo nombre base. Por ejemplo, `test04a.gau` produce `test04a.e`, `test04a.s` y `test04a.exe`. Los archivos existentes con esos nombres se sobrescriben. GCC debe estar disponible en `PATH`; si falla, `Main` informa el error y no indica que el ejecutable se generó correctamente.

## Componentes principales

- **`Main`**: ejecuta el parser y presenta el AST.
- **`ast/`**: nodos hoja, nodos binarios e impresor del árbol.
- **`sintactico/parser.cup`**: gramática y acciones que construyen el AST.
- **`sintactico/LexerCupAdapter`**: adapta los tokens y ubicaciones del lexer a Java CUP.
- **`semantico/`**: análisis de declaraciones, llamadas, funciones, anidamiento y ámbitos.
- **`gc/`**: generador de código intermedio y estructura de Polaca Inversa.
- **`cup.jar`**: generador CUP y runtime requerido para generar, compilar y ejecutar.
- **`CUP-LICENSE.txt`**: aviso de licencia del generador incluido.
- **`Lector`**: lee caracteres, cuenta líneas y columnas y permite devolver el carácter de lookahead.
- **`Clasificador`**: asigna caracteres a las clases de evento del autómata.
- **`Matrices`**: contiene las transiciones, códigos de tokens y acciones semánticas.
- **`Funciones`**: implementa las acciones del analizador léxico.
- **`ContextoLexico`**: conserva buffers, lexema actual y errores.
- **`AnalizadorLexico`**: coordina lectura, clasificación y reconocimiento.
