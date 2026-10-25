# Analizador léxico GAUCHO

Analizador léxico para GAUCHO, implementado en Java. Lee un archivo de texto carácter por carácter y devuelve tokens con su código y lexema. También mantiene una tabla de símbolos y registra errores léxicos.

## Requisitos

- JDK instalado, con `javac` y `java` disponibles en la terminal.
- Bash, PowerShell o una terminal equivalente.

## Estructura

```text
Compilador/
├── src/
│   ├── Main.java
│   └── lexico/
│       ├── AnalizadorLexico.java
│       ├── Clasificador.java
│       ├── ContextoLexico.java
│       ├── Funciones.java
│       ├── Lector.java
│       └── Matrices.java
├── prueba.txt
├── prueba-lexemas.txt
└── README.md
```

## Compilar y ejecutar desde Bash

Ejecuta estos comandos desde la carpeta raíz del proyecto:

```bash
mkdir -p out
javac -encoding UTF-8 -d out src/Main.java src/lexico/*.java
```

Para analizar el archivo de prueba predeterminado:

```bash
java -cp out Main
```

`Main` usa `prueba.txt` cuando no se proporciona una ruta. Para seleccionar otro archivo, pásalo como argumento:

```bash
java -cp out Main prueba-lexemas.txt
```

También puedes analizar otro archivo de texto:

```bash
java -cp out Main ruta/al/archivo.txt
```

## Ejemplo de entrada

`prueba-lexemas.txt` contiene una muestra de identificadores, constantes, operadores, delimitadores, texto y un comentario:

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

La salida muestra el código del token, su lexema y la línea. Por ejemplo:

```text
Token: 260 | Lexema: entero | Línea: 1
Token: 256 | Lexema: contador | Línea: 1
Token: 269 | Lexema: = | Línea: 1
Token: 257 | Lexema: 12 | Línea: 1
```

Los saltos de línea se devuelven como token `286`. El fin de archivo es `276` y se usa para terminar el ciclo de lectura; no se imprime como token en `Main`.

## Componentes principales

- **`Main`**: selecciona el archivo, solicita tokens al analizador e imprime los resultados, errores y tabla de símbolos.
- **`Lector`**: lee caracteres con `PushbackReader`, cuenta líneas y permite devolver un carácter para que se analice de nuevo.
- **`Clasificador`**: convierte cada carácter en una de las 19 columnas de evento usadas por las matrices.
- **`Matrices`**: contiene las matrices de transición, tokens y funciones semánticas.
- **`Funciones`**: implementa las acciones semánticas, como acumular letras, finalizar identificadores y guardar constantes.
- **`ContextoLexico`**: conserva los buffers, el lexema y token actuales, la tabla de símbolos y los errores.
- **`AnalizadorLexico`**: coordina la lectura, clasificación, consulta de matrices y emisión de cada token.

El analizador reconoce `entero` (token `260`) y `si` (token `261`) como palabras reservadas; las demás palabras se tratan como identificadores (token `256`). Las constantes numéricas usan el token `257` y los literales entre comillas, el token `258`.
