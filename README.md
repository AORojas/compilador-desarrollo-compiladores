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

> **Nota:** En esta etapa, los comandos corresponden específicamente a la compilación y ejecución del **analizador léxico**. La ruta del archivo de entrada puede modificarse para realizar diferentes pruebas sobre el análisis léxico.

# Estructura del Repositorio

```text
compilador-desarrollo-compiladores/
├── Compilador/
│   ├── out/
│   ├── src/
│   │   ├── assets/
│   │   │   ├── Automata Finito.drawio
│   │   │   └── Automata Finito.png
│   │   └── lexico/
│   │       ├── AnalizadorLexico.java
│   │       ├── Clasificador.java
│   │       ├── ContextoLexico.java
│   │       ├── Funciones.java
│   │       ├── Lector.java
│   │       └── Matrices.java
│   ├── out/
│   ├── Main.java
│   ├── prueba-lexemas.txt
│   ├── prueba.txt
│   └── README.md
│
├── specs/
│   ├── 01-diseño/
│   │   ├── bitacora.md
│   │   └── spec.md
│   ├── 02-analizador-léxico/
│   │   ├── bitacora.md
│   │   └── spec.md
│   ├── 03-analizador-sintáctico/
│   │   └── sintactico.md
│   ├── 04-analizador-semántico/
│   ├── GC/
│   │   └── codigo.md
│   └── GCA/
│       └── readme.md
│
├── .env
├── .gitignore
└── README.md
```


        