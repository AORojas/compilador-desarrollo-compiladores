# Especificación Técnica: Analizador Léxico (GAUCHO)

## 1. Contexto y Objetivo
Desarrollar el Analizador Léxico (AL) para el lenguaje **GAUCHO** en Java.

* **Restricción de arquitectura:** Debe ser simple y sin sobreingeniería (no usar patrones de diseño complejos ni frameworks externos). Está compuesto por las clases del paquete `lexico` más el punto de entrada principal para facilitar su compilación y depuración.
* **Flujo:** El motor lee el archivo fuente carácter por carácter, consulta las matrices definidas en la especificación de diseño, ejecuta las acciones semánticas correspondientes y retorna los códigos de token (256–286).

---

## 2. Referencias y Rutas del Proyecto

| Ruta / Archivo | Propósito |
| :--- | :--- |
| `specs/01-diseño/spec.md` | **Referencia principal (Fuente única):** Define el alfabeto, palabras reservadas, tabla de tokens (Sección 4), el autómata finito y las tres matrices oficiales (Secciones 10 a 14). |
| `Compilador/src/lexico/` | **Directorio de trabajo:** Paquete que contiene las 6 clases del motor léxico. |

---

## 3. Arquitectura de Clases
El módulo léxico se organiza en el paquete `lexico` con 6 clases cohesivas, acompañadas por el driver principal en la raíz de `src`:

1. `Clasificador.java`: Traduce un carácter leído a su índice de columna correspondiente (0–18) según las clases del alfabeto definidas en `specs/01-diseño/spec.md` (Sección 2).
2. `Lector.java`: Administra la lectura del archivo fuente con soporte de retroceso (`unread`) y mantiene el conteo de líneas.
3. `ContextoLexico.java`: Contenedor de estado con los buffers separados de letras, dígitos y texto, la tabla de símbolos local y la lista de errores léxicos.
4. `Funciones.java`: Métodos estáticos (`f1`–`f15`, `fe`) que ejecutan las acciones semánticas sobre el contexto.
5. `Matrices.java`: Arreglos estáticos que implementan las tres matrices del autómata y el mapeo de estados (`E0`–`E22`, `EF`).
6. `AnalizadorLexico.java`: El motor (`yylex()`) que coordina la lectura, las transiciones y la emisión de tokens.
7. `Main.java` (raíz de `src`): Driver que abre el archivo de entrada e invoca `yylex()` de forma iterativa.

---

## 4. Matrices del Autómata (`Matrices.java`)
Para evitar duplicidad con la documentación de diseño, la definición completa de las columnas (índices 0 a 18), los estados (`E0` a `E22` y `EF`) y las tablas de valores se consulta directamente en `specs/01-diseño/spec.md`:

* **Matriz de Transición (`TRANSICION`):** Implementa la tabla de transición de estados definida en `specs/01-diseño/spec.md` (Sección 11).
* **Matriz de Tokens (`TOKEN`):** Implementa la tabla de emisión de códigos de token definida en `specs/01-diseño/spec.md` (Sección 12). Las celdas sin emisión directa se representan con `-1`.
* **Matriz de Funciones Semánticas (`FUNCION`):** Implementa la tabla de acciones definida en `specs/01-diseño/spec.md` (Sección 13), enlazando cada celda con su método correspondiente en `Funciones.java` mediante una interfaz funcional (`Func`), o con `null` cuando solo transiciona.

*(Nota de implementación: Para operadores compuestos de dos caracteres como `==`, `<>`, `<=` y `>=`, se utiliza la función auxiliar `f15` para acumular el segundo símbolo sin retroceso antes de emitir el token).*

---

## 5. Especificación de las Funciones Semánticas (`Funciones.java`)
Las funciones semánticas se implementan como métodos estáticos públicos sin retorno que reciben el contexto léxico (`ctx`) y el carácter actual (`c`), siguiendo las definiciones conceptuales de `specs/01-diseño/spec.md` (Sección 14):

* **`f1` (Iniciar letras):** Limpia el buffer de letras y agrega el carácter inicial.
* **`f2` (Iniciar dígitos):** Limpia el buffer de dígitos y agrega el dígito inicial.
* **`f3` (Abrir literal):** Limpia el buffer de texto descartando la comilla de apertura.
* **`f4` (Ignorar):** Consume espacios en blanco y tabuladores sin modificar buffers.
* **`f5` (Acumular letra):** Agrega el carácter al buffer de letras.
* **`f6` (Acumular dígito):** Agrega el carácter al buffer de dígitos.
* **`f7` (Retornar ID / Palabra reservada):** Extrae el texto del buffer de letras como lexema actual, activa el retroceso de carácter (`debeHacerUnread = true`), verifica si coincide con alguna palabra reservada de `specs/01-diseño/spec.md` (Sección 3) para asignar su código (259–268), o en caso contrario asigna `256` (`ID`) y lo registra en la tabla de símbolos del contexto.
* **`f8` (Fin de línea):** Asigna el código de token `286` (`FIN_DE_LINEA`) y desactiva el retroceso (`debeHacerUnread = false`) para evitar bucles infinitos sobre saltos de línea.
* **`f9` (Retornar constante):** Extrae el texto del buffer de dígitos como lexema actual, activa el retroceso (`debeHacerUnread = true`), asigna el código `257` (`CTE`) y registra la constante en la tabla de símbolos del contexto.
* **`f10` (Token directo):** Activa el retroceso (`debeHacerUnread = true`) para que el motor obtenga el código de token directamente desde la matriz `TOKEN`.
* **`f11` (Cerrar literal):** Asigna el código `258` (`LITERAL_TXT`), guarda el contenido del buffer de texto como lexema actual y desactiva el retroceso (`debeHacerUnread = false`) consumiendo la comilla de cierre.
* **`f12` (Cerrar comentario):** Desactiva el retroceso (`debeHacerUnread = false`) al finalizar el bloque de comentario `*/`.
* **`f13` (Acumular texto):** Agrega el carácter actual al buffer de texto.
* **`f14` (Consumir comentario):** Descarta los caracteres internos del comentario sin almacenarlos.
* **`f15` (Acumular operador doble):** Agrega el segundo carácter de un operador relacional o de igualdad al acumulador de lexema y desactiva el retroceso.
* **`fe` (Error léxico):** Registra un mensaje descriptivo con la línea actual en la lista de errores del contexto (`ctx.errores`) y desactiva el retroceso (`debeHacerUnread = false`) para permitir la recuperación.

---

## 6. El Motor (`AnalizadorLexico.java`)
*Este es el corazón del programa. Debe implementarse siguiendo exactamente esta secuencia de pasos para evitar problemas de bucles infinitos:*

El método `yylex()` se encarga de reconocer y devolver el código entero del siguiente token válido del archivo fuente, propagando cualquier excepción durante la lectura o ejecución.

Su flujo de ejecución sigue estos pasos:

1. **Inicialización:** Reinicia el contexto léxico (`ctx.reset()`) para limpiar los buffers, los indicadores y el token previo, y posiciona el estado inicial del autómata en `Matrices.E0`.
2. **Ciclo de reconocimiento:** Mientras el estado actual no sea el estado final (`Matrices.EF`), ejecuta de forma iterativa las siguientes operaciones:
   * **Lectura y fin de archivo:** Solicita el siguiente carácter al `lector`. Si el valor leído corresponde al fin de archivo (`-1`), interrumpe el proceso y devuelve inmediatamente el código `276` (token `EOF`).
   * **Cálculo de coordenadas:** Obtiene el índice de fila correspondiente al estado actual en `Matrices` y determina el índice de columna clasificando el carácter leído mediante `Clasificador.getEvento(c)`. Actualiza ambos valores en el contexto (`filaActual` y `colActual`).
   * **Ejecución de acción semántica:** Consulta la función asignada en `Matrices.FUNCION` para esa fila y columna. Si existe una función definida (no nula), la ejecuta pasándole el contexto y el carácter actual.
   * **Retroceso de carácter (`unread`):** Verifica si la acción ejecutada activó la bandera `debeHacerUnread` en el contexto (por ejemplo, en funciones de cierre de lexema como `f7` o `f9`). De ser así, devuelve el carácter leído al flujo mediante `lector.unread(c)` y restablece la bandera a falso **antes** de evaluar el corte del ciclo.
   * **Corte por reconocimiento:** Comprueba si el contexto ya tiene asignado un código de token válido (distinto de `-1`). Si es así, abandona el ciclo inmediatamente.
   * **Transición de estado:** Si aún no se completó el token, actualiza el estado actual consultando la tabla `Matrices.TRANSICION` con la fila y columna calculadas.
3. **Consolidación y retorno:** Al salir del ciclo, si el acumulador de caracteres (`bufferLexema`) contiene al menos un carácter, convierte su contenido a texto y lo guarda en `lexemaActual`. Finalmente, devuelve el código numérico almacenado en `tokenActual`.

---

## 7. Especificación del `Lector.java`
*Para evitar errores de entrada/salida y manejar el retroceso de caracteres correctamente, se utiliza un flujo de lectura con capacidad de retroceso (`PushbackReader`).*

La clase `Lector` administra la lectura carácter por carácter del archivo fuente y mantiene actualizado el seguimiento del número de línea actual mediante dos atributos internos privados: el lector con retroceso (`reader`) y un contador entero `linea` inicializado en `1`.

Su funcionamiento se organiza en cuatro operaciones principales:

* **Inicialización (`Lector`):** Recibe la ruta del archivo fuente como texto, abre el flujo de lectura sobre ese archivo y lo envuelve en un `PushbackReader` configurado con un búfer de retroceso de un carácter, propagando cualquier error de entrada/salida (`IOException`).
* **Avance de lectura (`nextChar`):** Lee y devuelve el siguiente carácter del flujo en formato entero. Si el carácter leído corresponde a un salto de línea (`\n`), incrementa automáticamente el contador `linea` en una unidad antes de retornarlo.
* **Retroceso de carácter (`unread`):** Recibe un carácter leído previamente para devolverlo al flujo de entrada. Comprueba primero que no sea el indicador de fin de archivo (`-1`); si es un salto de línea (`\n`), decrementa el contador `linea` en una unidad para mantener la sincronización exacta, y finalmente reincorpora el carácter al `PushbackReader`.
* **Consulta de ubicación (`getLinea`):** Devuelve el número entero correspondiente a la línea actual en la que se encuentra posicionada la lectura.

---

## 8. Estándar de Desarrollo y Estructura del Proyecto

### 8.1. Organización de Paquetes y Directorios
* Las clases lógicas del analizador (`AnalizadorLexico`, `ContextoLexico`, `Funciones`, `Lector`, `Matrices`, `Clasificador`) deben pertenecer al paquete `lexico`.
* La clase `Main` se ubica en la raíz de `src` (paquete por defecto) e importa `lexico.AnalizadorLexico`.

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
└── prueba.txt
```

### 8.2. Restricciones y Manejo de Estado
* **Colecciones estándar:** Utilizar exclusivamente colecciones estándar de Java (`List`, `Map`, `HashMap`, `ArrayList`) sin jerarquías de excepciones personalizadas.
* **Manejo de errores:** El método `yylex()` propaga `Exception` (o `IOException`) para mantener limpias las funciones semánticas. Los errores léxicos (código `E1` y `E2` de `specs/01-diseño/spec.md`) se acumulan en la lista `ctx.errores` sin abortar el análisis.
* **Reinicio de contexto (`reset()`):** Al inicio de cada llamada a `yylex()`, `ContextoLexico.reset()` vacía todos los buffers (`bufferLetras`, `bufferDigitos`, `bufferTexto`, `bufferLexema`), reinicia `tokenActual` a `-1` y restablece `debeHacerUnread` a falso.
* **Compilación y prueba:** Se dispone de un archivo `prueba.txt` en la raíz con declaraciones, condicionales, comentarios y operadores para validar la ejecución mediante compilación estándar con `javac` y `java`.


