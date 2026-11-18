# Compilador GAUCHO

Analizador sintáctico, semántico y generador de código intermedio en Java CUP. El parser construye el AST; el analizador semántico valida declaraciones, ámbitos, llamadas a funciones y anidamiento; el generador emite Polaca Inversa. El proyecto todavía no genera Assembler. Admite el salto de línea opcional antes de la llave de apertura de funciones, bloques `si` y bloques `bucle`.

## Requisitos

- JDK instalado, con `javac` y `java` disponibles en la terminal.
- Bash.

## Generar, compilar y ejecutar desde Bash

Ejecuta estos comandos desde `Compilador/`:

```bash
java -cp cup.jar java_cup.Main -parser Parser -symbols sym -package sintactico -destdir src/sintactico src/sintactico/parser.cup
mkdir -p out
javac -encoding UTF-8 -cp cup.jar -d out src/Main.java src/lexico/*.java src/ast/*.java src/sintactico/*.java src/semantico/*.java src/gc/*.java
java -cp "out;cup.jar" Main ruta/al/archivo.gau
```

La ruta es obligatoria y puede ser relativa a `Compilador/` o absoluta. El programa informa errores léxicos, sintácticos y semánticos, imprime el AST cuando está disponible, muestra la Tabla de Símbolos y genera la Polaca Inversa si no hay errores en las fases anteriores.

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
