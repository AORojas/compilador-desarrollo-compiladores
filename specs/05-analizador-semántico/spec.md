# Especificación Técnica: Analizador Semántico

## 1. Objetivo y Alcance

El Analizador Semántico recibe el Árbol de Sintaxis Abstracta (AST) construido a partir de las acciones del Analizador Sintáctico y valida la coherencia semántica del programa.

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
- **Analizador Sintáctico:** definido en `specs/03-analizador-sintáctico/spec.md`, que valida la gramática oficial del lenguaje y ejecuta las acciones CUP.
- **Especificación del AST:** definida en `specs/04-arbol-sintáctico/spec.md`, que establece el diseño del árbol (`NodoAST`, `Nodo` y `Hoja`) y las acciones desacopladas mediante las cuales se instancia cada nodo.
- **Implementación del AST:** ubicada en `Compilador/src/ast/`.

Estas referencias constituyen la base para interpretar los nodos del AST producidos tras el análisis sintáctico y determinar qué validaciones corresponden a esta etapa.

## 3. Estructuras de Datos

### 3.1. Registro de la Tabla de Símbolos (`Simbolo`)

De acuerdo con la Regla R5 de `specs/01-diseño/spec.md`, cada entrada de la tabla tendrá los siguientes atributos:

- `id` (entero): identificador único autoincremental.
- `nombre` (texto): lexema de la variable, función o constante.
- `tipo` (texto): tipo de dato, por ejemplo, `"entero"` para variables y funciones, o `"texto"` para literales.
- `clase` (texto): categoría del símbolo: `"variable"`, `"funcion"` o `"constante"`.
- `valor` (texto): valor asociado al símbolo cuando corresponde. Para variables y funciones se utiliza `null`.
- `longitud` (entero): cantidad de caracteres del nombre o valor utilizado al registrar el símbolo.
- `ambito` (entero): nivel de profundidad del ámbito en el que se registró.

**Nota de arquitectura:** los símbolos de ámbitos ya cerrados no se eliminan de la tabla. El `GestorAmbitos` deja de considerarlos visibles, pero permanecen registrados para conservar la información del análisis.

### 3.2. Gestor de Ámbitos (Reglas R1 y R2)

El gestor de ámbitos utiliza una pila para administrar los espacios de declaración de variables.

- El programa comienza con un ámbito global de profundidad 0.
- Cada función abre un nuevo ámbito.
- Cada bloque de `si` y `bucle` abre un subámbito.
- Al finalizar el análisis de una función o estructura, se cierra el ámbito correspondiente.
- Se permite el *shadowing*: una variable declarada en un ámbito interno puede tener el mismo nombre que otra de un ámbito exterior.
- No se permite declarar dos veces una variable dentro del mismo ámbito.

El parámetro de una función se registra en el ámbito de esa función, junto con sus variables locales, de acuerdo con la estructura del AST definida en `specs/04-arbol-sintáctico/spec.md`.

### 3.3. Estructura de la Tabla de Símbolos (`TablaSimbolos`)

La tabla utiliza colecciones basadas en mapas enlazados (`LinkedHashMap`) para conservar el orden de registro de los símbolos.

Se mantienen tres estructuras:

- `simbolos`: almacena todos los símbolos mediante su identificador.
- `funciones`: permite consultar las funciones registradas por nombre.
- `parametrosPorFuncion`: almacena la cantidad de parámetros asociada a cada función.

Los identificadores comienzan en 1 y se incrementan automáticamente con cada registro.

## 4. Reglas de Validación y Códigos de Error

Para recorrer el AST e interpretar sus nodos, el analizador utiliza el operador de cada nodo (`Nodo.operador`) y el tipo de sus hijos (`Nodo` u `Hoja`), alineado con los operadores generados durante las reducciones de `parser.cup` (`PROGRAMA`, `FUNCIONES`, `FUNCION`, `BLOQUE`, `SENTENCIAS`, `DECL`, `:=`, `SI`, `BUCLE`, `MOSTRAR`, `MOSTRAR_TXT`, `RETORNAR`, `LLAMADA`, operadores lógicos, relacionales y aritméticos).

Las validaciones se basan en los códigos definidos en `specs/01-diseño/spec.md` (Sección 8):

- **E4 (Variable no declarada):** se reporta cuando una variable utilizada no se encuentra declarada en ninguno de los ámbitos activos.
- **E8 (Redefinición):** se reporta cuando se intenta declarar una variable que ya existe en el ámbito actual.
- **E5 (Anidamiento excedido):** al ingresar a una estructura `si` o `bucle`, se incrementa el contador de anidamiento. Si supera 3 niveles, se reporta E5.
- **E6 (Error de llamada a función):** se verifica que la función invocada exista y que la cantidad de argumentos coincida con la firma registrada. La implementación admite funciones con cero o un parámetro.
- **E9 (Error de función principal):** al finalizar el recorrido, se verifica que exista exactamente una función llamada `principal`.

**Aclaración sobre E6:** la cantidad de argumentos de una llamada se determina según la estructura del AST: si el hijo derecho del nodo `LLAMADA` es nulo, se considera que no tiene argumentos; si existe, se considera que tiene uno.

**Aclaración sobre R6:** no debe confundirse el código de error E6 con la Regla R6 de diseño, correspondiente a la disposición del registro de activación. Esta última no se implementa mediante las validaciones de este analizador semántico.

### 4.1. Ubicación de los Errores

Dado que los nodos y hojas del AST (`Nodo` y `Hoja`) no propagan números de línea ni columna, los errores se reportan indicando el identificador o el tipo de estructura afectada.

Ejemplos:

- `E4 - Variable no declarada - 'vacas'`
- `E5 - Anidamiento excedido en estructura si/bucle`
- `E8 - Redefinición en el mismo ámbito - 'vacas'`
- `E9 - Falta la función principal`

## 5. Manejo del AST Parcial (Recuperación de Errores)

Si el Parser recuperó de un error sintáctico mediante el descarte hasta `fin_lineas` y generó un AST con ramas nulas o nodos incompletos, el analizador debe comprobar la existencia de los nodos antes de acceder a sus atributos.

Los nodos nulos deben ser ignorados para evitar excepciones por referencias inexistentes.

La implementación contempla comprobaciones de nulidad y verificaciones de tipo de instancia (`Nodo` frente a `Hoja`) en los procedimientos de recorrido y procesamiento del AST.

## 6. Formato de Salida

El programa principal (`Main`) imprimirá los errores con el formato:

`[Código] - Descripción - Identificador (si aplica)`

Ejemplo:

`E4 - Variable no declarada - 'vacas'`

Luego imprimirá la Tabla de Símbolos con el siguiente formato exacto de columnas:

`ID | Nombre | Tipo | Clase | Valor | Longitud | Ámbito`

La tabla se genera mediante el método `formatear()` de `TablaSimbolos`.

## 7. Procedimientos de Implementación del Analizador Semántico

Esta sección describe los procedimientos principales utilizados en la implementación. Se incluyen únicamente los algoritmos relevantes para comprender el funcionamiento interno del analizador.

### 7.1. Inicialización y Coordinación del Análisis

La clase `AnalizadorSemantico` mantiene las referencias a la Tabla de Símbolos y al Gestor de Ámbitos, además de una lista de errores y contadores auxiliares.

El método `analizar()` coordina las etapas del proceso:

1. Comprueba que la instancia del analizador no haya sido utilizada previamente.
2. Marca la instancia como analizada.
3. Verifica que la raíz del AST (`NodoAST` entregada tras finalizar el análisis sintáctico) no sea nula.
4. Ejecuta el registro previo de funciones mediante `registrarFunciones()` sobre la raíz.
5. Inicia el análisis general del AST mediante `analizarNodo()` sobre la raíz.
6. Comprueba la existencia y unicidad de la función `principal`: si el contador de funciones principales es cero, agrega el error E9 indicando que falta la función principal; si es mayor a uno, agrega el error E9 indicando que la función principal está duplicada.

El método `getErrores()` devuelve una vista no modificable de la lista de errores. Por su parte, `getTablaSimbolos()` permite acceder a la tabla generada durante el análisis.

### 7.2. Registro Previo de Funciones

Antes de analizar las instrucciones del programa, se realiza una primera recorrida recursiva del AST mediante `registrarFunciones()`.

Este procedimiento permite conocer todas las funciones declaradas antes de validar sus llamadas, incluso cuando una función se encuentra declarada después de otra que la invoca.

El procedimiento realiza las siguientes operaciones:

1. Ignora los nodos nulos y las hojas.
2. Identifica los nodos cuyo operador sea `"FUNCION"`.
3. Obtiene el nombre de la función a partir del nodo auxiliar de datos ubicado en el hijo izquierdo del nodo `"FUNCION"`. La función principal se representa con el operador `"PRINCIPAL"` y se registra con el nombre `"principal"`.
4. Determina si posee cero o un parámetro verificando si existe una hoja asociada en el hijo izquierdo del nodo de datos de la función (0 para `PRINCIPAL`, 1 para funciones con parámetro `ENTERO`).
5. Registra la función y su cantidad de parámetros en la Tabla de Símbolos.
6. Incrementa el contador cuando encuentra una función llamada `"PRINCIPAL"` o `"principal"` (normalizando su identificador según la firma).
7. Continúa el recorrido por los subárboles izquierdo y derecho.

### 7.3. Recorrido y Clasificación de Nodos del AST

El método `analizarNodo()` realiza el recorrido semántico general del árbol evaluando el tipo de cada elemento y el operador de cada nodo:

- Si el nodo es nulo, finaliza el procesamiento de esa rama.
- Si es una `Hoja`, delega su procesamiento a `analizarHoja()`.
- Si el operador es `"FUNCION"`, ejecuta `analizarFuncion()`.
- Si el operador es `"DECL"`, ejecuta `analizarDeclaracion()` enviándole el subárbol derecho que contiene los identificadores.
- Si el operador es `":="` (o `"="`), valida el destino de la asignación enviando el hijo izquierdo a `analizarDestinoAsignacion()` y luego analiza recursivamente la expresión ubicada en el hijo derecho.
- Si el operador es `"SI"` o `"BUCLE"`, procesa sus condiciones y bloques controlando el anidamiento y los ámbitos.
- Si el operador es `"LLAMADA"`, ejecuta `analizarLlamada()`.
- Si el operador es `"MOSTRAR_TXT"`, registra el literal de texto ubicado en su hijo izquierdo.
- Para los demás nodos estructurales (`"PROGRAMA"`, `"FUNCIONES"`, `"BLOQUE"`, `"SENTENCIAS"`, operadores aritméticos, relacionales y lógicos), continúa recursivamente por sus hijos izquierdo y derecho.

Esta organización permite centralizar el recorrido del AST y delegar cada validación específica en un método auxiliar.

### 7.4. Análisis de Funciones y Gestión de sus Ámbitos

El método `analizarFuncion()` crea un nuevo ámbito para el cuerpo de cada función:

1. Extrae el nodo de datos ubicado en el hijo izquierdo del nodo `"FUNCION"` e ingresa a un nuevo ámbito invocando `entrarAmbito()` en el gestor de ámbitos.
2. Dentro de un bloque protegido para asegurar el cierre del ámbito, comprueba si el nodo de datos contiene un parámetro representado por una `Hoja` en su hijo izquierdo; de ser así, toma su valor y lo declara en el ámbito recién abierto.
3. Analiza recursivamente el cuerpo de la función ubicado en el hijo derecho del nodo `"FUNCION"` (correspondiente al subárbol `"BLOQUE"`).
4. Al finalizar (incluso ante cualquier excepción), invoca `salirAmbito()` para cerrar el ámbito de la función.

### 7.5. Procesamiento de Declaraciones de Variables

Las declaraciones se procesan mediante `analizarDeclaracion()`.

El método recibe el subárbol que contiene la lista de identificadores (nodos con operador `","` y hojas con los nombres de las variables) y realiza un recorrido recursivo:

- Si recibe una `Hoja`, utiliza su valor como nombre de variable y delega su registro al método interno `declarar()`.
- Si recibe un `Nodo` (como la concatenación por comas `","`), continúa procesando recursivamente sus hijos izquierdo y derecho.

El método `declarar()` intenta registrar la variable en el gestor de ámbitos; si el gestor indica que el nombre ya existía en el ámbito actual devolviendo falso, agrega el error E8 indicando la redefinición en el mismo ámbito junto con el nombre de la variable.

### 7.6. Validación de Variables y Destinos de Asignación

La validación de variables utiliza el método `comprobarVariable()`, el cual verifica si el nombre es nulo o si el gestor de ámbitos indica que la variable no es visible en ninguno de los ámbitos activos; en cualquiera de ambos casos, registra el error E4 especificando el identificador cuando esté disponible.

Para las asignaciones (`":="`), `analizarDestinoAsignacion()` comprueba si el hijo izquierdo es una `Hoja` y, en ese caso, invoca `comprobarVariable()` para verificar que la variable destino esté previamente declarada. La expresión ubicada en el lado derecho se analiza posteriormente mediante el recorrido general del AST.

### 7.7. Validación de Llamadas a Funciones

El método `analizarLlamada()` extrae el nombre de la función desde la `Hoja` ubicada en el hijo izquierdo del nodo `"LLAMADA"` y determina la cantidad de argumentos evaluando si su hijo derecho es nulo (cero argumentos) o contiene una expresión (un argumento).

Luego realiza dos comprobaciones secuenciales:

1. Si el nombre es nulo o la función no existe en la Tabla de Símbolos, registra el error E6 indicando la llamada a una función inexistente.
2. Si la función existe pero la cantidad de argumentos no coincide con la firma registrada, registra el error E6 indicando que la firma es inválida para esa función.

Finalmente, analiza recursivamente el hijo derecho del nodo `"LLAMADA"` para validar las variables o subexpresiones utilizadas dentro del argumento.

### 7.8. Control del Anidamiento de Estructuras

El analizador utiliza el atributo `nivelAnidamiento` para controlar la profundidad de las estructuras condicionales (`"SI"`) y repetitivas (`"BUCLE"`).

Al ingresar a un bloque `"SI"` o `"BUCLE"`, incrementa el contador y ejecuta una comprobación interna: si el nivel actual supera el máximo permitido de tres niveles, agrega el error E5 indicando que se excedió el anidamiento en estructuras `si/bucle`.

El método `entrarEstructura()` también crea un nuevo subámbito en el gestor de ámbitos para el bloque interno y garantiza su cierre al finalizar el procesamiento, decrementando nuevamente el contador de anidamiento al salir de la estructura.

### 7.9. Registro de Constantes

Las constantes encontradas durante el análisis se incorporan a la Tabla de Símbolos.

Cuando `analizarHoja()` evalúa una `Hoja` en contexto de expresión o condición, comprueba mediante coincidencia numérica (`[0-9]+`) si su valor está formado exclusivamente por dígitos:

- Si es una constante entera, solicita a la Tabla de Símbolos registrarla como constante con tipo `"entero"` en la profundidad del ámbito actual.
- En caso contrario, asume que se trata de un identificador de variable y ejecuta `comprobarVariable()` para validar su visibilidad.

Por otro lado, los literales de texto asociados al nodo `"MOSTRAR_TXT"` se procesan extrayendo la `Hoja` de su hijo izquierdo y registrando el literal en la Tabla de Símbolos con tipo `"texto"`.

### 7.10. Administración Interna de los Ámbitos

La clase `GestorAmbitos` utiliza una pila de objetos `Ambito`, donde cada ámbito almacena su nivel de profundidad y un mapa con las variables declaradas en ese nivel.

- **Inicialización:** Al instanciarse, crea el ámbito global con profundidad cero y lo coloca en la base de la pila.
- **Apertura (`entrarAmbito()`):** Consulta la profundidad del ámbito en la cima de la pila, crea un nuevo ámbito con profundidad incrementada en uno y lo apila.
- **Cierre (`salirAmbito()`):** Verifica que la pila tenga más de un elemento para impedir cerrar el ámbito global (lanzando una excepción de estado ilegal si se intenta) y desapila el ámbito superior.
- **Búsqueda (`variableVisible()`):** Recorre los ámbitos activos de la pila desde el más interno hacia el global comprobando si alguno contiene el nombre buscado; devuelve verdadero en cuanto lo encuentra o falso si no existe en ningún nivel activo.

De esta forma, una variable declarada en un ámbito exterior continúa siendo visible desde los ámbitos internos, siempre que no exista una declaración local que la oculte.

### 7.11. Registro y Consulta de Símbolos

La clase `TablaSimbolos` centraliza el almacenamiento de los símbolos:

- **Registro general (`registrar()`):** Instancia un nuevo objeto `Simbolo` asignándole el siguiente identificador autoincremental disponible, su nombre, tipo, clase, valor, longitud del nombre y ámbito, y lo guarda en el mapa principal indexado por su identificador.
- **Constantes (`registrarConstante()`):** Realiza una operación equivalente utilizando el valor de la constante como nombre y contenido del símbolo.
- **Funciones (`registrarFuncion()`):** Registra la función como símbolo y almacena adicionalmente su cantidad de parámetros en el mapa de firmas para validar llamadas posteriores.
- **Consulta y salida:** `obtenerSimbolos()` devuelve una lista no modificable con todos los registros, mientras que `formatear()` construye la representación textual tabular respetando el orden exacto de las columnas definidas.

## 8. Consideraciones y Límites de la Implementación Actual

Las siguientes consideraciones delimitan el comportamiento que efectivamente presenta la implementación:

- El analizador admite una única ejecución por instancia. Si se intenta ejecutar nuevamente `analizar()`, se produce una excepción de estado ilegal (`IllegalStateException`).
- El registro previo de funciones permite conocer sus nombres y cantidades de parámetros antes de validar llamadas.
- La cantidad de parámetros y argumentos se determina mediante posiciones específicas del AST (`FUNCION` y `LLAMADA`) y contempla cero o un elemento.
- El análisis de hojas interpreta como constantes enteras las cadenas formadas únicamente por dígitos (`[0-9]+`). Las demás hojas procesadas mediante `analizarHoja()` se consideran posibles variables.
- La validación de destinos de asignación se realiza cuando el hijo izquierdo de `":="` es una `Hoja`.
- Los literales de texto se registran cuando aparecen como hojas dentro de la estructura `"MOSTRAR_TXT"`.
- La tabla conserva los símbolos registrados, incluso cuando el ámbito al que pertenecen ya fue cerrado.
- El registro de funciones conserva en sus mapas la primera firma encontrada para cada nombre. La detección de funciones duplicadas no se realiza en `registrarFunciones()`, salvo para la función principal mediante el control E9.
- Si la raíz del AST entregada por el analizador sintáctico es nula, el método `analizar()` finaliza antes de ejecutar el control E9.
- Los errores E1, E2 y E3 corresponden a las etapas léxica y sintáctica, por lo que no se generan mediante las validaciones de esta implementación semántica.
- Las reglas relacionadas con evaluación de cortocircuito y disposición de registros de activación deben implementarse en las etapas correspondientes del compilador, según el diseño general.

La documentación de esta sección debe mantenerse sincronizada con los cambios realizados en las clases del paquete `semantico`.


