# Compilador GAUCHO

Trabajo práctico universitario de desarrollo de compiladores (Grupo D), implementado en Java.

## Estado del proyecto

El analizador léxico está finalizado y se conserva como base funcional del proyecto. La etapa sintáctica se encuentra en desarrollo a partir de las especificaciones de `specs/`. Por ahora, el punto de entrada solo muestra los tokens reconocidos por el lexer.

## Requisitos

- JDK con `java` y `javac`.
- Bash o una terminal equivalente.

## Compilar y ejecutar

Desde Bash, cambia al directorio `Compilador/`:

```bash
cd Compilador
mkdir -p out
javac -encoding UTF-8 -d out src/Main.java src/lexico/*.java
java -cp out Main ruta/al/archivo.txt
```

La ruta del archivo es obligatoria y puede ser relativa a `Compilador/` o absoluta. El programa imprime el código, lexema, línea y columna de cada token; informa los errores léxicos y termina con un código distinto de cero si los encuentra.

## Estructura principal

- `Compilador/src/lexico/`: analizador léxico y sus componentes.
- `Compilador/src/Main.java`: ejecuta temporalmente solo el análisis léxico.
- `specs/`: especificaciones del lenguaje y etapas del compilador.

La implementación del analizador sintáctico, Java CUP, las pruebas del frontend y los archivos de compilación anteriores se retiraron para reiniciar esa etapa. La generación de Polaca Inversa y Assembler no forma parte del estado actual.
