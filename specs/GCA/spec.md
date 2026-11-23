# Generacion de Assembler (x86-64 Windows / GCC)

Procedimiento paso a paso para implementar el **Generador de Código Intermedio a Assembler (`x86-64`)**,compilador en Windows 11 utilizando **GCC (MinGW-w64)** con sintaxis Intel y direccionamiento directo. Se va a generar como una herramienta a parte del proyecto general.

El diseño soporta tipos de datos **enteros** (`entero` / 64 bits) y **cadenas de texto** (`string` / punteros de 64 bits a arreglos de caracteres terminados en `\0`).

---

## Comando de Compilación en Windows 11 (Para usuario final)

Una vez que tu compilador genere el archivo `salida.s`, debe invocar los siguientes comandos para crear y ejecutar el binario nativo:

```cmd
gcc -no-pie -Wl,--image-base=0x400000 salida.s -o programa.exe
programa.exe
```

---

## Etapa 0: Estructura del programa y obtencion de Tabla de Símbolos y Polaca Inversa

### 1. Obtencion de la Tabla de Símbolos y Polaca Inversa:

Generar ObtencionTsPi.java debera leer el archivo `entrada.e` y extraer la tabla de simbolos y la polaca inversa que se van a encontrar en el mismo con el siguiente formato de ejemplo:

```cmd
Tabla de símbolos:
ID | Nombre | Tipo | Clase | Valor | Longitud | Ámbito
0 | principal | entero | funcion |  | 9 | 0
1 | a | entero | variable |  | 1 | 1
2 | b | entero | variable |  | 1 | 1
3 | 0 | entero | constante | 0 | 1 | 0
4 | 10 | entero | constante | 10 | 2 | 0
Análisis sintáctico finalizado correctamente.
Polaca Inversa:
[3] [1] [:=] [4] [2] [:=] [1] [3] [>] [BF] ->22 [BI] ->13 [2] [3] [>] [BF] ->22 [BI] ->20 [2] [PRINT]
```
### 2. Generacion de codigo y salida

`GeneradorAsm` puede ejecutarse como herramienta independiente: su rutina `main` lee un reporte `.e` y escribe el archivo assembler indicado. El `Main` general también utiliza las clases `ObtencionTsPi` y `GeneradorAsm` para automatizar las etapas `.e` → `.s` → `.exe` después de un análisis exitoso.

Desde `Compilador/`, se puede compilar y ejecutar el generador así:

```cmd
if not exist out mkdir out
javac -encoding UTF-8 -d out src/generadorasm/ObtencionTsPi.java src/generadorasm/GeneradorAsm.java
java -cp out generadorasm.GeneradorAsm entrada04a.e salida04a.s
gcc -no-pie -Wl,--image-base=0x400000 salida04a.s -o programa04a.exe
programa04a.exe
```

El primer argumento de `GeneradorAsm` es el reporte con la tabla de símbolos y la Polaca Inversa; el segundo es el archivo assembler de salida. Si se omite alguno, se usan `entrada.e` y `salida.s`.

### 3. Estructura de directorios requerida:

```
Compilador/
├── src/
│   ├── Main.java
│   ├── lexico/
│   │    ├── AnalizadorLexico.java
│   │    ├── Clasificador.java
│   │    ├── ContextoLexico.java
│   │    ├── Funciones.java
│   │    ├── Lector.java
│   │    └── Matrices.java
│   ├── sintactico/
│   │    ├── Condicion.java
│   │    ├── ErrorSemantico.java
│   │    ├── GestorAmbitos.java
│   │    ├── LexerCupAdapter.java
│   │    ├── parser.cup
│   │    ├── Parser.java
│   │    ├── PolacaInversa.java
│   │    ├── Simbolo.java
│   │    ├── sym.java
│   │    └── TablaSimbolos.java
│   └─── generadorasm/
│        ├── ObtencionTsPi.java
│        └── GeneradorAsm.java
├── entrada.e
├── salida.s
└── prueba.txt
specs/
├── 01-diseño/
│   └─── spec.md
├── 02-analizador-lexico/
│   └─── spec.md
├── 03-analizador-sintactico/
│   └─── spec.md
├── GC/
└── GCA/
    └─── spec.md
```

## Etapa 1: Traducción de la Tabla de Símbolos (`.data`)

Antes de recorrer la polaca inversa, recorre cada fila de la **Tabla de Símbolos** y genera las declaraciones en la sección `.data`:

### 1. Variables (`Clase == "variable"`)
* **Si `Tipo == "entero"`:**
  Reserva 8 bytes (64 bits) inicializados en `0`. Usa el prefijo `_` para evitar conflictos con palabras reservadas de Assembler.
  ```asm
  _a: .quad 0
  ```
* **Si `Tipo == "string"`:**
  Reserva un buffer fijo de bytes (por ejemplo, 256 bytes con `.space 256`) para almacenar los caracteres de la variable y facilitar su copia (`strcpy`):
  ```asm
  _nombre: .space 256
  ```

### 2. Constantes (`Clase == "constante"`)
* **Si `Tipo == "entero"`:**
  Declara una etiqueta usando su ID o nombre y asigna su valor literal.
  ```asm
  _cte3: .quad 0
  _cte4: .quad 10
  ```
* **Si `Tipo == "string"`:**
  Usa la directiva `.asciz`, que guarda los caracteres en ASCII y agrega automáticamente el byte nulo (`\0`) al final.
  ```asm
  _str5: .asciz "Hola mundo"
  ```

### 3. Funciones (`Clase == "funcion"`)
* No reservan memoria en `.data`. Se traducen como etiquetas de código en la sección `.text`.

### 4. Auxiliares Fijos del Sistema
Incluye siempre estas etiquetas al final de `.data` para manejar la entrada/salida estándar de C y buffers temporales:

```asm
_buf_str:    .space 256
fmt_int:     .asciz "%lld\n"
fmt_str:     .asciz "%s\n"
```

--- 

## Etapa 2: Recorrido de la Polaca Inversa (`.text`)

Recorre la lista de tokens de la polaca con un índice `i = 0, 1, 2...`. Mantén una variable auxiliar `ultimo_op` para recordar el último operador relacional evaluado y saber qué salto inverso generar en los `[BF]`.

### 1. Verificación de Etiquetas de Destino (Antes de cada token `i`)
Antes de procesar el token en la celda `i`, comprueba si algún salto (`[BF]` o `[BI]`) de toda la polaca apunta a `->i`.
* **Regla:** Si existe al menos un salto hacia `i`, escribe la etiqueta:
  ```asm
  etq_i:
  ```

---


### 2. Declaración y Llamada de Funciones

* **Función Principal (`principal`):**
  ```asm
  .text
      .global main
      .extern printf
      .extern strcpy
      .extern strcmp
      .extern strcat
      .extern ExitProcess

  main:
      sub rsp, 40
  ```
  *Nota: `sub rsp, 40` reserva los 32 bytes de "shadow space" exigidos por Windows 64 bits más 8 bytes de alineación de pila.*

* **Otras Funciones (`mi_funcion`):**
  * **Inicio:**
    ```asm
    mi_funcion:
        push rbp
        mov rbp, rsp
        sub rsp, 32
        mov [_parametro], rcx
    ```
    Las funciones auxiliares reciben exactamente un entero por valor, en `rcx` según la convención x64 de Windows. El generador guarda ese valor en la variable que representa el parámetro.
  * **Retorno (`[RET]`):**
    ```asm
    pop rax          # Si la función devuelve un valor o dirección
    add rsp, 32
    pop rbp
    ret
    ```
  * **Llamada (`[CALL]`):**
    ```asm
    pop rcx          # Único argumento entero, previamente apilado al evaluar la expresión
    call mi_funcion
    push rax         # Apila el valor retornado
    ```
    La forma correspondiente en Polaca es `[argumento] [ID_funcion] [CALL]`. `[CALL]` consume el argumento de la pila de evaluación y deja en ella el valor entero retornado; por ejemplo, `[3] [2] [CALL] [4] [:=]` pasa el símbolo 3, llama a la función 2 y asigna su retorno al símbolo 4.

  ### Retorno desde una función auxiliar

  El token `[RET]` indica que una función auxiliar debe:

  1. Sacar el valor de retorno de la pila.
  2. Restaurar el registro base y el espacio reservado.
  3. Ejecutar `ret`, que devuelve el control a la instrucción siguiente al `call`
     que originó la llamada.

  No hace falta agregar un salto explícito hacia `principal` en la Polaca: la
  dirección de retorno la administra el procesador mediante `call` y `ret`.
  Después de `call mi_funcion`, el generador apila el valor devuelto en `rax`
  mediante `push rax`.

  #### Reportes sin sección de entradas

  El reporte puede omitir la sección `Entradas de funciones:` cuando hay una
  sola función auxiliar. En ese caso, el generador infiere la posición inicial
  auxiliar a partir de los saltos incondicionales resueltos:

  * El generador identifica el mayor destino de un `[BI] ->N`.
  * Ese destino se usa como punto de entrada de la única función auxiliar.
  * `principal` comienza siempre en la posición `0`.

  Ejemplo parcial:

  ```text
  [BI] ->28
  ...
  [10] [12] [*] [11] [:=] [11] [RET]
  ```

  El generador interpreta `->28` como la entrada de la función auxiliar y
  separa `_func_...` en esa posición. Al procesar `[RET]`, emite `ret`; así,
  la ejecución vuelve a la instrucción posterior al `call`.

  La tabla de símbolos y la Polaca no identifican explícitamente cuál variable
  es el parámetro. Si falta esa información, el generador busca la variable de
  menor ID referenciada en el cuerpo auxiliar. Si su primera referencia es una
  lectura (no el destino de `:=`), la infiere como parámetro. Esto coincide con
  el orden de declaración del compilador actual (el parámetro se declara antes
  que las variables locales). Si su primera referencia es una asignación, no
  hace falta copiar `rcx` a esa variable; el argumento igualmente se consume
  al llamar. Si hay varias funciones auxiliares, el reporte debe incluir sus
  posiciones, porque no se pueden inferir inequívocamente sus límites solo con
  la Polaca.

  Para proporcionar explícitamente posiciones y parámetros, se puede agregar:

  ```text
  Entradas de funciones:
  ID | Índice de función | ID del parámetro
  1 | 0 |
  2 | 35 | 11
  ```

  El ID del parámetro es opcional: si se omite, el generador usa la misma
  inferencia por la Polaca.

---


### 3. Lectura de Operandos (`[ID]`)

Cuando encuentras un número entre corchetes `[ID]`, consulta su `Tipo` y `Clase` en la Tabla de Símbolos:

* **Si es `entero` (variable o constante):**
  Apila su **valor** numérico de 64 bits:
  ```asm
  push qword ptr [_etiqueta]
  ```
* **Si es `string` (variable o constante):**
  Apila la **dirección de memoria** (puntero) donde comienza la cadena usando `lea` (*Load Effective Address*):
  ```asm
  lea rax, [_etiqueta]
  push rax
  ```

---

### 4. Operaciones Aritméticas (Solo Enteros)

Todas extraen dos operandos de la pila (`rbx` = segundo operando, `rax` = primer operando), realizan el cálculo y vuelven a apilar el resultado.

* **Suma (`[+]`):**
  ```asm
  pop rbx
  pop rax
  add rax, rbx
  push rax
  ```
* **Resta (`[-]`):**
  ```asm
  pop rbx
  pop rax
  sub rax, rbx
  push rax
  ```
* **Multiplicación (`[*]`):**
  ```asm
  pop rbx
  pop rax
  imul rax, rbx
  push rax
  ```
* **División Entera (`[/]`):**
  `cqo` extiende el signo de `rax` hacia `rdx` antes de dividir por `rbx`. El cociente queda en `rax`.
  ```asm
  pop rbx
  pop rax
  cqo
  idiv rbx
  push rax
  ```
---

### 5. Operaciones con Strings (`[+]` Concatenación)

Si tu lenguaje permite concatenar dos strings con `[+]` (por ejemplo `s1 + " mundo"`), tu análisis semántico detecta que los operandos son de tipo `string` y genera una llamada a `strcpy` y `strcat` sobre `_buf_str`:

```asm
pop rbx                  # Dirección del 2do string (origen)
pop rax                  # Dirección del 1er string (base)

lea rcx, [_buf_str]      # Destino: buffer temporal
mov rdx, rax             # Origen: 1er string
call strcpy              # Copia el 1er string al buffer

lea rcx, [_buf_str]      # Destino: buffer temporal
mov rdx, rbx             # Origen: 2do string
call strcat              # Concatena el 2do string

lea rax, [_buf_str]
push rax                 # Apila la dirección del resultado
```

---

### 6. Asignación (`[:=]`)

En tu polaca el patrón es `[valor_o_direccion] [ID_destino] [:=]`.

* **Si el destino es `entero` (ej. `a = 10`):**
  ```asm
  pop rbx              # Descarta el valor previo apilado del destino
  pop rax              # Extrae el resultado a asignar
  mov [_a], rax        # Almacena el entero en la variable
  ```
* **Si el destino es `string` (ej. `nombre = "Ana"`):**
  En Assembler no se puede mover una cadena entera con `mov`; se copia byte a byte hasta el `\0` usando `strcpy(destino, origen)`:
  ```asm
  pop rcx              # 1er parámetro (rcx): dirección de la variable destino (_nombre)
  pop rdx              # 2do parámetro (rdx): dirección del string origen
  call strcpy
  ```

---

### 7. Comparaciones Relacionales (`[>]`, `[<]`, `[>=]`, `[<=]`, `[==]`, `[!=]`)

* **Si los operandos son `entero`:**
  Guarda el símbolo del operador en `ultimo_op` y compara los registros:
  ```asm
  pop rbx
  pop rax
  cmp rax, rbx
  ```
* **Si los operandos son `string` (ej. `nombre == "Ana"`):**
  Usa `strcmp(str1, str2)`. Esta función devuelve `0` en `eax` si ambas cadenas son idénticas. Guarda el operador (`==` o `!=`) en `ultimo_op`:
  ```asm
  pop rdx              # 2do parámetro (rdx): dirección del 2do string
  pop rcx              # 1er parámetro (rcx): dirección del 1er string
  call strcmp
  cmp eax, 0           # Compara el resultado de strcmp con 0
  ```

---

### 8. Condicionales (`if`) y Bucles (`until` / `while`)

Tanto el `if` como el `until` utilizan las mismas dos instrucciones de salto de la polaca: `[BF]` y `[BI]`.

* **Salto por Falso (`[BF] ->N`):**
  Consulta `ultimo_op` y emite la instrucción de salto **opuesta**:
  * `>`  -> `jle etq_N`
  * `<`  -> `jge etq_N`
  * `>=` -> `jl etq_N`
  * `<=` -> `jg etq_N`
  * `==` -> `jne etq_N`
  * `!=` -> `je etq_N`

* **Salto Incondicional (`[BI] ->N`):**
  ```asm
  jmp etq_N
  ```

* **Funcionamiento del bucle `until`:**
  1. Antes del cuerpo del bucle se coloca una etiqueta (ej. `etq_inicio:`).
  2. Se ejecutan las instrucciones internas.
  3. Al final se evalúa la condición de corte (`cmp rax, rbx` o `cmp eax, 0`).
  4. Como `until` repite *mientras la condición sea falsa*, la polaca tendrá un `[BF] ->inicio` que volverá a `etq_inicio:` hasta que la condición se vuelva verdadera.

---

### 9. Salida a pantalla (`[PRINT]`)

* **Imprimir (`[PRINT]`):**
  * **Si el operando es `entero`:**
    ```asm
    pop rdx              # 2do parámetro: valor entero
    lea rcx, [fmt_int]   # 1er parámetro: "%lld\n"
    call printf
    ```
  * **Si el operando es `string`:**
    ```asm
    pop rdx              # 2do parámetro: dirección del string
    lea rcx, [fmt_str]   # 1er parámetro: "%s\n"
    call printf
    ```

---

### 10. Fin del Programa (`principal`)

Al llegar al último índice de la polaca (si hay una etiqueta final como `etq_22:`, colócala antes):

```asm
    xor ecx, ecx
    call ExitProcess
```

---

## Ejemplo Completo Integrado (Enteros + Strings + If)

### Tabla de Símbolos de Entrada
| ID | Nombre | Tipo | Clase | Valor |
| :--- | :--- | :--- | :--- | :--- |
| 0 | principal | entero | funcion | |
| 1 | a | entero | variable | |
| 2 | msg | string | variable | |
| 3 | 0 | entero | constante | 0 |
| 4 | 10 | entero | constante | 10 |
| 5 | "Mayor a cero" | string | constante | Mayor a cero |

### Polaca Inversa
`[3] [1] [:=] [5] [2] [:=] [4] [1] [:=] [1] [3] [>] [BF] ->20 [BI] ->17 [2] [PRINT]`

### Código Assembler Resultante (`salida.s`)

```asm
.intel_syntax noprefix

.data
    _a:          .quad 0
    _msg:        .space 256
    _cte3:       .quad 0
    _cte4:       .quad 10
    _str5:       .asciz "Mayor a cero"
    _buf_str:    .space 256
    fmt_int:     .asciz "%lld\n"
    fmt_str:     .asciz "%s\n"

.text
    .global main
    .extern printf
    .extern strcpy
    .extern strcmp
    .extern strcat
    .extern ExitProcess

main:
    sub rsp, 40

    # [3] [1] [:=] -> a = 0
    push qword ptr [_cte3]
    push qword ptr [_a]
    pop rbx
    pop rax
    mov [_a], rax

    # [5] [2] [:=] -> msg = "Mayor a cero"
    lea rax, [_str5]
    push rax
    lea rax, [_msg]
    push rax
    pop rcx
    pop rdx
    call strcpy

    # [4] [1] [:=] -> a = 10
    push qword ptr [_cte4]
    push qword ptr [_a]
    pop rbx
    pop rax
    mov [_a], rax

    # [1] [3] [>] [BF] ->20 [BI] ->17 -> if (a > 0)
    push qword ptr [_a]
    push qword ptr [_cte3]
    pop rbx
    pop rax
    cmp rax, rbx
    jle etq_20
    jmp etq_17

etq_17:
    # [2] [PRINT] -> print(msg)
    lea rax, [_msg]
    push rax
    pop rdx
    lea rcx, [fmt_str]
    call printf

etq_20:
    xor ecx, ecx
    call ExitProcess
```
