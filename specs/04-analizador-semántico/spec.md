# Especificación Técnica: Analizador Semántico

## 1. Objetivo y Alcance

El Analizador Semántico recibe el Árbol de Sintaxis Abstracta (AST) generado por el Analizador Sintáctico y valida la coherencia semántica del programa.

Su responsabilidad es identificar errores relacionados con declaraciones, utilización de variables, ámbitos, llamadas a funciones, niveles de anidamiento y existencia de la función principal.

No genera código ni modifica la estructura del AST.

Su salida está compuesta por:

- Una lista de errores semánticos detectados.
- Una Tabla de Símbolos con las variables, constantes y funciones registradas.

La implementación se encuentra organizada en el paquete `semantico`, distribuida en las siguientes clases:

- `AnalizadorSemantico`: coordina el proceso de análisis y ejecuta las validaciones.
- `GestorAmbitos`: administra los ámbitos y la visibilidad de las variables.
- `TablaSimbolos`: registra y consulta los símbolos y las funciones.
- `Simbolo`: representa cada entrada de la Tabla de Símbolos.

## 2. Referencias y Rutas del Proyecto

Para implementar esta fase, el desarrollador debe consultar las definiciones originales en:

- **Reglas Semánticas (R1 a R6) y Códigos de Error (E1 a E9):** definidos en `specs/01-diseño/spec.md` (Secciones 7 y 8).
- **Estructura del AST:** definida en `Compilador/src/ast/` (clases `Nodo`, `Hoja` y `NodoAST`).
- **Gramática oficial:** definida en `specs/01-diseño/spec.md` (Sección 6).

Estas referencias constituyen la base para interpretar los nodos del AST y determinar qué validaciones corresponden a esta etapa.

## 3. Estructuras de Datos

### 3.1. Registro de la Tabla de Símbolos (`Simbolo`)

De acuerdo con la Regla R5 de `specs/01-diseño/spec.md`, cada entrada de la tabla tendrá los siguientes atributos:

- `id` (int): identificador único autoincremental.
- `nombre` (String): lexema de la variable, función o constante.
- `tipo` (String): tipo de dato, por ejemplo, `"entero"` para variables y funciones, o `"texto"` para literales.
- `clase` (String): categoría del símbolo: `"variable"`, `"funcion"` o `"constante"`.
- `valor` (String): valor asociado al símbolo cuando corresponde. Para variables y funciones se utiliza `null`.
- `longitud` (int): cantidad de caracteres del nombre o valor utilizado al registrar el símbolo.
- `ambito` (int): nivel de profundidad del ámbito en el que se registró.

**Nota de arquitectura:** los símbolos de ámbitos ya cerrados no se eliminan de la tabla. El `GestorAmbitos` deja de considerarlos visibles, pero permanecen registrados para conservar la información del análisis.

### 3.2. Gestor de Ámbitos (Reglas R1 y R2)

El gestor de ámbitos utiliza una pila para administrar los espacios de declaración de variables.

- El programa comienza con un ámbito global de profundidad 0.
- Cada función abre un nuevo ámbito.
- Cada bloque de `si` y `bucle` abre un subámbito.
- Al finalizar el análisis de una función o estructura, se cierra el ámbito correspondiente.
- Se permite el *shadowing*: una variable declarada en un ámbito interno puede tener el mismo nombre que otra de un ámbito exterior.
- No se permite declarar dos veces una variable dentro del mismo ámbito.

El parámetro de una función se registra en el ámbito de esa función, junto con sus variables locales, de acuerdo con la estructura del AST utilizada por la implementación.

### 3.3. Estructura de la Tabla de Símbolos (`TablaSimbolos`)

La tabla utiliza colecciones basadas en `LinkedHashMap` para conservar el orden de registro de los símbolos.

Se mantienen tres estructuras:

- `simbolos`: almacena todos los símbolos mediante su identificador.
- `funciones`: permite consultar las funciones registradas por nombre.
- `parametrosPorFuncion`: almacena la cantidad de parámetros asociada a cada función.

Los identificadores comienzan en 1 y se incrementan automáticamente con cada registro.

## 4. Reglas de Validación y Códigos de Error

Para recorrer el AST e interpretar sus nodos, el analizador utiliza el operador de cada nodo y el tipo de sus hijos (`Nodo` u `Hoja`).

Las validaciones se basan en los códigos definidos en `specs/01-diseño/spec.md` (Sección 8):

- **E4 (Variable no declarada):** se reporta cuando una variable utilizada no se encuentra declarada en ninguno de los ámbitos activos.
- **E8 (Redefinición):** se reporta cuando se intenta declarar una variable que ya existe en el ámbito actual.
- **E5 (Anidamiento excedido):** al ingresar a una estructura `si` o `bucle`, se incrementa el contador de anidamiento. Si supera 3 niveles, se reporta E5.
- **E6 (Error de llamada a función):** se verifica que la función invocada exista y que la cantidad de argumentos coincida con la firma registrada. La implementación actual admite funciones con cero o un parámetro.
- **E9 (Error de función principal):** al finalizar el recorrido, se verifica que exista exactamente una función llamada `principal`.

**Aclaración sobre E6:** la cantidad de argumentos de una llamada se determina según la estructura actual del AST: si el hijo derecho del nodo `LLAMADA` es nulo, se considera que no tiene argumentos; si existe, se considera que tiene uno.

**Aclaración sobre R6:** no debe confundirse el código de error E6 con la Regla R6 de diseño, correspondiente a la disposición del registro de activación. Esta última no se implementa mediante las validaciones de este analizador semántico.

### 4.1. Ubicación de los Errores

Dado que el AST actual no propaga números de línea, los errores se reportan indicando el identificador o el tipo de estructura afectada.

Ejemplos:

- `E4 - Variable no declarada - 'vacas'`
- `E5 - Anidamiento excedido en estructura si/bucle`
- `E8 - Redefinición en el mismo ámbito - 'vacas'`
- `E9 - Falta la función principal`

## 5. Manejo del AST Parcial (Recuperación de Errores)

Si el Parser recuperó de un error sintáctico y generó un AST con nodos incompletos, el analizador debe comprobar la existencia de los nodos antes de acceder a sus atributos.

Los nodos nulos deben ser ignorados para evitar excepciones por referencias inexistentes.

La implementación contempla comprobaciones de `null` y verificaciones de tipo mediante `instanceof` en los procedimientos de recorrido y procesamiento del AST.

## 6. Formato de Salida

El `Main` imprimirá los errores con el formato:

`[Código] - Descripción - Identificador (si aplica)`

Ejemplo:

`E4 - Variable no declarada - 'vacas'`

Luego imprimirá la Tabla de Símbolos con el siguiente formato exacto de columnas:

`ID | Nombre | Tipo | Clase | Valor | Longitud | Ámbito`

La tabla se genera mediante el método `formatear()` de `TablaSimbolos`.

## 7. Procedimientos de Implementación del Analizador Semántico

Esta sección describe los procedimientos principales utilizados en la implementación Java. Se incluyen únicamente los algoritmos relevantes para comprender el funcionamiento interno del analizador.

### 7.1. Inicialización y Coordinación del Análisis

La clase `AnalizadorSemantico` mantiene las referencias a la Tabla de Símbolos y al Gestor de Ámbitos, además de una lista de errores y contadores auxiliares.

El método `analizar()` coordina las etapas del proceso:

1. Comprueba que la instancia del analizador no haya sido utilizada previamente.
2. Marca la instancia como analizada.
3. Verifica que la raíz del AST no sea nula.
4. Ejecuta el registro previo de funciones.
5. Inicia el análisis general del AST.
6. Comprueba la existencia y unicidad de la función `principal`.

Fragmento representativo:

```java
registrarFunciones(raiz);
analizarNodo(raiz);

if (cantidadPrincipal == 0) {
    errores.add("E9 - Falta la función principal");
} else if (cantidadPrincipal > 1) {
    errores.add("E9 - Función principal duplicada - 'principal'");
}
```

El método `getErrores()` devuelve una vista no modificable de la lista de errores. Por su parte, `getTablaSimbolos()` permite acceder a la tabla generada durante el análisis.

### 7.2. Registro Previo de Funciones

Antes de analizar las instrucciones del programa, se realiza una primera recorrida recursiva del AST mediante `registrarFunciones()`.

Este procedimiento permite conocer todas las funciones declaradas antes de validar sus llamadas, incluso cuando una función se encuentra declarada después de otra que la invoca.

El procedimiento realiza las siguientes operaciones:

1. Ignora los nodos nulos y las hojas.
2. Identifica los nodos cuyo operador sea `FUNCION`.
3. Obtiene el nombre de la función a partir de la estructura asociada al nodo.
4. Determina si posee cero o un parámetro según la presencia del hijo izquierdo de los datos de la función.
5. Registra la función y su cantidad de parámetros en la Tabla de Símbolos.
6. Incrementa el contador cuando encuentra una función llamada `principal`.
7. Continúa el recorrido por los subárboles izquierdo y derecho.

Fragmento representativo:

```java
if ("FUNCION".equals(rama.operador)) {
    String nombre = nombreFuncion(rama);

    if (nombre != null) {
        Nodo datosFuncion = rama.izquierdo instanceof Nodo
            ? (Nodo) rama.izquierdo
            : null;

        int cantidadParametros =
            datosFuncion != null && datosFuncion.izquierdo != null ? 1 : 0;

        tablaSimbolos.registrarFuncion(nombre, cantidadParametros);

        if ("principal".equals(nombre)) {
            cantidadPrincipal++;
        }
    }
}
```

La cantidad de parámetros se determina según la representación actual del AST, que contempla como máximo un parámetro por función.

### 7.3. Recorrido y Clasificación de Nodos del AST

El método `analizarNodo()` realiza el recorrido semántico general del árbol.

Su comportamiento depende del tipo de nodo y del valor de su operador:

- Si el nodo es nulo, finaliza el procesamiento de esa rama.
- Si es una `Hoja`, delega su procesamiento a `analizarHoja()`.
- Si representa una función, ejecuta `analizarFuncion()`.
- Si representa una declaración, ejecuta `analizarDeclaracion()`.
- Si representa una asignación, valida el destino y analiza la expresión.
- Si representa una estructura `SI` o `BUCLE`, procesa sus condiciones y bloques.
- Si representa una llamada, ejecuta `analizarLlamada()`.
- Si representa una instrucción `MOSTRAR_TEXTO`, registra el literal correspondiente.
- Para los demás nodos, continúa recursivamente por sus hijos.

La identificación de las principales estructuras se realiza mediante comparaciones del operador:

```java
if ("FUNCION".equals(operador)) {
    analizarFuncion(rama);
} else if ("DECLARACION".equals(operador)) {
    analizarDeclaracion(rama.izquierdo);
} else if ("=".equals(operador) || ":=".equals(operador)) {
    analizarDestinoAsignacion(rama.izquierdo);
    analizarNodo(rama.derecho);
}
```

Esta organización permite centralizar el recorrido del AST y delegar cada validación específica en un método auxiliar.

### 7.4. Análisis de Funciones y Gestión de sus Ámbitos

El método `analizarFuncion()` crea un nuevo ámbito para el cuerpo de cada función.

Primero obtiene los datos asociados a la función y luego ingresa a un nuevo ámbito mediante `gestorAmbitos.entrarAmbito()`.

Si la estructura contiene un parámetro representado mediante una hoja en la posición esperada, lo declara dentro del ámbito de la función.

Finalmente, analiza el cuerpo de la función y cierra el ámbito.

Fragmento representativo:

```java
gestorAmbitos.entrarAmbito();

try {
    if (datosFuncion != null && datosFuncion.izquierdo instanceof Hoja) {
        declarar(((Hoja) datosFuncion.izquierdo).valor);
    }

    analizarNodo(funcion.derecho);
} finally {
    gestorAmbitos.salirAmbito();
}
```

El uso de `finally` garantiza que el ámbito se cierre incluso si se produce una excepción durante el procesamiento.

### 7.5. Procesamiento de Declaraciones de Variables

Las declaraciones se procesan mediante `analizarDeclaracion()`.

El método recibe el nodo que contiene la lista de identificadores y realiza un recorrido recursivo:

- Si recibe una `Hoja`, utiliza su valor como nombre de variable y lo declara.
- Si recibe un `Nodo`, continúa procesando sus hijos izquierdo y derecho.

El registro efectivo se delega en el método `declarar()`.

```java
private void declarar(String nombre) {
    if (!gestorAmbitos.declararVariable(nombre)) {
        errores.add("E8 - Redefinición en el mismo ámbito - '" + nombre + "'");
    }
}
```

El Gestor de Ámbitos determina si el nombre ya fue declarado en el ámbito actual. Si existe, devuelve `false` y el analizador registra E8.

### 7.6. Validación de Variables y Destinos de Asignación

La validación de variables utiliza `comprobarVariable()`.

Este método consulta al Gestor de Ámbitos para determinar si el identificador se encuentra visible en alguno de los ámbitos activos.

```java
private void comprobarVariable(String nombre) {
    if (nombre == null || !gestorAmbitos.variableVisible(nombre)) {
        errores.add("E4 - Variable no declarada"
            + (nombre == null ? "" : " - '" + nombre + "'"));
    }
}
```

Para las asignaciones, `analizarDestinoAsignacion()` comprueba si el destino es una hoja y, en ese caso, verifica que la variable esté declarada.

La expresión ubicada en el lado derecho se analiza posteriormente mediante el recorrido general del AST.

### 7.7. Validación de Llamadas a Funciones

El método `analizarLlamada()` obtiene el nombre de la función a partir del hijo izquierdo del nodo `LLAMADA`.

Luego determina la cantidad de argumentos y realiza dos comprobaciones:

1. Que la función exista en la Tabla de Símbolos.
2. Que la cantidad de argumentos coincida con la firma registrada.

Fragmento representativo:

```java
int cantidadArgumentos = llamada.derecho == null ? 0 : 1;

if (nombre == null || !tablaSimbolos.existeFuncion(nombre)) {
    errores.add("E6 - Llamada a función inexistente");
} else if (!tablaSimbolos.firmaCoincide(nombre, cantidadArgumentos)) {
    errores.add("E6 - Firma inválida para la función - '" + nombre + "'");
}

analizarNodo(llamada.derecho);
```

La expresión correspondiente al argumento también se analiza recursivamente para verificar las variables utilizadas.

### 7.8. Control del Anidamiento de Estructuras

El analizador utiliza el atributo `nivelAnidamiento` para controlar la profundidad de las estructuras condicionales y repetitivas.

Al ingresar a un bloque `SI` o `BUCLE`, incrementa el contador y comprueba si supera el máximo permitido de tres niveles.

```java
private void reportarAnidamientoSiCorresponde() {
    if (nivelAnidamiento > 3) {
        errores.add("E5 - Anidamiento excedido en estructura si/bucle");
    }
}
```

El método `entrarEstructura()` también crea un nuevo ámbito para el bloque y garantiza su cierre al finalizar el procesamiento.

Para los nodos `BUCLE`, el incremento y decremento del contador se realizan directamente dentro de `analizarNodo()`.

### 7.9. Registro de Constantes

Las constantes encontradas durante el análisis se incorporan a la Tabla de Símbolos.

Para los literales enteros, `analizarHoja()` utiliza la expresión regular `[0-9]+` para reconocer cadenas formadas exclusivamente por dígitos.

```java
if (hoja.valor.matches("[0-9]+")) {
    tablaSimbolos.registrarConstante(
        hoja.valor,
        "entero",
        gestorAmbitos.profundidadActual()
    );
} else {
    comprobarVariable(hoja.valor);
}
```

Los literales de texto asociados a `MOSTRAR_TEXTO` se procesan mediante `registrarLiteralTexto()` y se registran con tipo `"texto"`.

### 7.10. Administración Interna de los Ámbitos

La clase `GestorAmbitos` utiliza una pila de objetos `Ambito`.

Cada ámbito contiene:

- Su profundidad.
- Un mapa de variables declaradas en ese ámbito.

El constructor crea el ámbito global con profundidad cero.

Al ingresar a un nuevo ámbito, se agrega un objeto a la pila con una profundidad superior a la del ámbito anterior:

```java
public void entrarAmbito() {
    pila.push(new Ambito(pila.peek().profundidad + 1));
}
```

Al salir, se elimina el ámbito superior:

```java
public void salirAmbito() {
    if (pila.size() <= 1) {
        throw new IllegalStateException(
            "No se puede cerrar el ámbito global."
        );
    }
    pila.pop();
}
```

La búsqueda de variables visibles recorre los ámbitos activos:

```java
public boolean variableVisible(String nombre) {
    for (Ambito ambito : pila) {
        if (ambito.variables.containsKey(nombre)) {
            return true;
        }
    }
    return false;
}
```

De esta forma, una variable declarada en un ámbito exterior continúa siendo visible desde los ámbitos internos, siempre que no exista una declaración local que la oculte.

### 7.11. Registro y Consulta de Símbolos

La clase `TablaSimbolos` centraliza el almacenamiento de los símbolos.

El método `registrar()` crea una instancia de `Simbolo`, asigna el siguiente identificador disponible y la incorpora al mapa principal.

```java
Simbolo simbolo = new Simbolo(
    siguienteId++,
    nombre,
    tipo,
    clase,
    valor,
    nombre.length(),
    ambito
);

simbolos.put(simbolo.id, simbolo);
```

El método `registrarConstante()` realiza una operación equivalente, utilizando el valor de la constante como nombre y contenido del símbolo.

Las funciones se registran mediante `registrarFuncion()`, que además almacena su cantidad de parámetros para validar llamadas posteriores.

La consulta de símbolos se realiza mediante `obtenerSimbolos()`, que devuelve una lista no modificable.

Por último, `formatear()` genera la representación textual de la tabla, respetando el orden de sus columnas.

## 8. Consideraciones y Límites de la Implementación Actual

Las siguientes consideraciones delimitan el comportamiento que efectivamente presenta la implementación:

- El analizador admite una única ejecución por instancia. Si se intenta ejecutar nuevamente `analizar()`, se produce una `IllegalStateException`.
- El registro previo de funciones permite conocer sus nombres y cantidades de parámetros antes de validar llamadas.
- La cantidad de parámetros y argumentos se determina mediante posiciones específicas del AST y contempla cero o un elemento.
- El análisis de hojas interpreta como constantes enteras las cadenas que coinciden con `[0-9]+`. Las demás hojas procesadas mediante `analizarHoja()` se consideran posibles variables.
- La validación de destinos de asignación se realiza cuando el destino es una `Hoja`.
- Los literales de texto se registran cuando aparecen como hojas dentro de la estructura `MOSTRAR_TEXTO`.
- La tabla conserva los símbolos registrados, incluso cuando el ámbito al que pertenecen ya fue cerrado.
- El registro de funciones conserva en sus mapas la primera firma encontrada para cada nombre. La detección de funciones duplicadas no se realiza en `registrarFunciones()`.
- Si la raíz del AST es `null`, el método `analizar()` finaliza antes de ejecutar el control E9.
- Los errores E1, E2 y E3 corresponden principalmente a etapas léxicas y sintácticas, por lo que no se generan directamente mediante las validaciones descriptas en esta implementación semántica.
- Las reglas relacionadas con evaluación de cortocircuito y disposición de registros de activación deben implementarse en las etapas correspondientes del compilador, según el diseño general.

La documentación de esta sección debe mantenerse sincronizada con los cambios realizados en las clases del paquete `semantico`.