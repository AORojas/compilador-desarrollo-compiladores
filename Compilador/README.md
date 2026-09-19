# Analizador Léxico GAUCHO

Proyecto Maven Java 17 listo para abrir directamente con Visual Studio Code.

## Ejecutar

Abrir esta carpeta en VS Code y ejecutar en la terminal:

```bash
mvn clean compile
mvn exec:java
```

Por defecto se procesa:

```text
src/main/resources/prueba.gau
```

También se puede indicar otro archivo `.gau` o `.txt`:

```bash
mvn exec:java -Dexec.args="src/main/resources/otro.gau"
```

En Windows PowerShell también funciona:

```powershell
mvn exec:java -Dexec.args="src/main/resources/otro.gau"
```

## Estructura

```
└── compilador-desarrollo-compiladores/
    ├── Compilador/
    │   └── src/
    │       ├── assets/
    │       │   ├── Automata Finito.drawio
    │       │   └── Automata Finito.png
    │       ├── main/
    │       │   ├── java/
    │       │   │   └── compilador\lexico/
    │       │   │       ├── datos/
    │       │   │       │   ├── Clasificador.java
    │       │   │       │   ├── MatrizTokens.java
    │       │   │       │   └── MatrizTransicion.java
    │       │   │       ├── modelo/
    │       │   │       │   ├── TipoToken.java
    │       │   │       │   └── Token.java
    │       │   │       ├── semantica/
    │       │   │       │   └── GestorFuncionesSemanticas.java
    │       │   │       ├── AnalizadorLexico.java
    │       │   │       ├── TablaSimbolos.java
    │       │   │       └── Main.java
    │       │   └── resources/
    │       │       └── prueba.gau
    │       ├── .gitignore
    │       ├── pom.xml
    │       └── README.md
    ├── specs/
    ├── .gitignore
    └── README.md

```

## Formato del archivo fuente

El código GAUCHO se escribe en un archivo `.gau` o `.txt`. No hace falta colocar el código fuente dentro de `Main.java`.

El lexer reconoce palabras reservadas, identificadores, constantes enteras, literales de texto, operadores, delimitadores, saltos de línea y comentarios `/* ... */`.
