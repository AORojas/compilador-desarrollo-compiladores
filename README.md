# Compilador 2026

## Integrantes

| Nombre             | Legajo | Rol |
|--------------------|--------|-----|
|Orrego Rojas Adrian |        |     |
|Viggiano Cristian   |9125    |     |

## TP asignado

Trabajo Práctico -- Desarrollo de un Compilador

GRUPO D

## Lenguaje

- **Lenguaje fuente:** Gaucho
- **Lenguaje de implementación:** Java
- **Arquitectura destino:** _(assembler generado)_

## Cómo compilar

Desde la carpeta `Compilador/`, abrir una terminal y ejecutar:

```bash
mvn clean compile
```

Este comando compila la implementación actual del **analizador léxico** y verifica que el proyecto no tenga errores de compilación.

## Cómo ejecutar

Desde la carpeta `Compilador/`, ejecutar:

```bash
mvn exec:java
```

Este comando ejecuta el **analizador léxico** utilizando por defecto el archivo de prueba:

```text
src/main/resources/prueba.gau
```

El analizador procesa el código fuente contenido en el archivo `.gau` y muestra la información de los tokens reconocidos, incluyendo:

* Código del token.
* Nombre del token.
* Lexema.
* Línea en la que fue reconocido.

### Ejecutar otro archivo de prueba

También se puede indicar otro archivo `.gau` o `.txt` mediante la línea de comandos:

```bash
mvn exec:java -Dexec.args="src/main/resources/otro.gau"
```

En Windows PowerShell se puede utilizar el mismo comando:

```powershell
mvn exec:java -Dexec.args="src/main/resources/otro.gau"
```

> **Nota:** En esta etapa, los comandos corresponden específicamente a la compilación y ejecución del **analizador léxico**. La ruta del archivo de entrada puede modificarse para realizar diferentes pruebas sobre el análisis léxico.

## Estructura del Repositorio

```
└── compilador-desarrollo-compiladores/
    ├── Compilador/
    │   ├── src/
    │   │   ├── assets/
    │   │   │   ├── Automata Finito.drawio
    │   │   │   └── Automata Finito.png
    │   │   └── main/
    │   │       ├── java/
    │   │       │   ├── compilador/
    │   │       │   │   └── lexico/
    │   │       │   │       ├── datos/
    │   │       │   │       │   ├── Clasificador.java
    │   │       │   │       │   ├── MatrizTokens.java
    │   │       │   │       │   └── MatrizTransicion.java
    │   │       │   │       ├── modelo/
    │   │       │   │       │   ├── TipoToken.java
    │   │       │   │       │   └── Token.java
    │   │       │   │       ├── semantica/
    │   │       │   │       │   └── GestorFuncionesSemanticas.java
    │   │       │   │       ├── AnalizadorLexico.java
    │   │       │   │       └── TablaSimbolos.java
    │   │       │   └── Main.java
    │   │       └── resources/
    │   │           └── prueba.gau
    │   ├── .gitignore
    │   ├── pom.xml
    │   └── README.md
    │
    ├── specs/
    │   ├── 01-diseno/
    │   │   ├── bitacora.md
    │   │   └── spec.md
    │   │
    │   ├── AL/
    │   │   ├── bitacora.md
    │   │   └── spec.md
    │   │
    │   ├── AS/
    │   │   └── sintactico.md
    │   │
    │   ├── GC/
    │   │   └── codigo.md
    │   │
    │   └── GCA/
    │
    ├── .gitignore
    └── README.md
```

        