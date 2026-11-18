# Compilador GAUCHO

Trabajo práctico universitario de desarrollo de compiladores (Grupo D), implementado en Java.

## Estado del proyecto

El analizador léxico está finalizado y se conserva intacto. El analizador sintáctico valida la estructura GAUCHO y construye el AST; el semántico valida declaraciones, ámbitos, llamadas a funciones y anidamiento; el generador produce Polaca Inversa. La generación de Assembler no está implementada.

## Requisitos

- JDK con `java` y `javac`.
- Java CUP 11b-20160615 (incluido en `Compilador/cup.jar`).
- Aviso de licencia de Java CUP en `Compilador/CUP-LICENSE.txt`.
- Bash o una terminal equivalente.

## Compilar y ejecutar

Desde Bash, cambia al directorio `Compilador/` y ejecuta:

```bash
cd Compilador
java -cp cup.jar java_cup.Main -parser Parser -symbols sym -package sintactico -destdir src/sintactico src/sintactico/parser.cup
mkdir -p out
javac -encoding UTF-8 -cp cup.jar -d out src/Main.java src/lexico/*.java src/ast/*.java src/sintactico/*.java src/semantico/*.java src/gc/*.java
java -cp "out;cup.jar" Main ruta/al/archivo.gau
```

La ruta del archivo es obligatoria y puede ser relativa a `Compilador/` o absoluta. El programa informa errores léxicos, sintácticos y semánticos, imprime el AST cuando está disponible, muestra la Tabla de Símbolos y genera la Polaca Inversa si no hay errores en las fases anteriores.

## Estructura principal

- `Compilador/src/lexico/`: analizador léxico y sus componentes.
- `Compilador/src/sintactico/`: adaptador del lexer y gramática CUP.
- `Compilador/src/ast/`: nodos e impresión del AST.
- `Compilador/src/semantico/`: tabla de símbolos, gestor de ámbitos y análisis semántico.
- `Compilador/src/gc/`: generador de código intermedio y Polaca Inversa.
- `Compilador/src/Main.java`: ejecuta las fases léxica, sintáctica, semántica y generación de código intermedio.
- `specs/`: especificaciones del lenguaje y etapas del compilador.

El proyecto todavía no genera Assembler.
