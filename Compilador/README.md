# Analizador léxico GAUCHO

Analizador léxico en Java. La etapa sintáctica está en desarrollo; el `Main` actual ejecuta únicamente el lexer y muestra los tokens reconocidos.

## Requisitos

- JDK con `java` y `javac`.
- Bash.

## Compilar y ejecutar desde Bash

Ejecuta estos comandos desde `Compilador/`:

```bash
mkdir -p out
javac -encoding UTF-8 -d out src/Main.java src/lexico/*.java
java -cp out Main ruta/al/archivo.txt
```

La ruta es obligatoria y puede ser relativa a `Compilador/` o absoluta. La salida incluye código del token, lexema, línea y columna. Si se detectan errores léxicos, se imprimen y el proceso termina con código distinto de cero.

## Componentes

- **`Main`**: lee el archivo indicado y presenta los tokens.
- **`Lector`**: lee caracteres, cuenta líneas y columnas y permite devolver el carácter de lookahead.
- **`Clasificador`**: asigna caracteres a las clases de evento del autómata.
- **`Matrices`**: contiene las transiciones, códigos de tokens y acciones semánticas.
- **`Funciones`**: implementa las acciones del analizador léxico.
- **`ContextoLexico`**: conserva buffers, lexema actual y errores.
- **`AnalizadorLexico`**: coordina lectura, clasificación y reconocimiento.

`prueba.txt` y demás muestras de prueba fueron retiradas durante la limpieza. Para probar, pasa la ruta a cualquier archivo de entrada GAUCHO.
