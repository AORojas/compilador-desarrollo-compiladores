# Analizador Léxico GAUCHO (Java)

## 1. Contexto y Objetivo
Desarrollar el Analizador Léxico (AL) para el lenguaje **GAUCHO** en Java. 
**Restricción de arquitectura:** Debe ser simple, sin sobreingeniería (no usar patrones de diseño complejos ni frameworks externos). Debe estar compuesto por **6 clases** en un solo paquete (sin subcarpetas) para facilitar su compilación y debugging.
**Flujo:** El motor leerá un archivo `.txt` carácter por carácter, consultará 3 matrices estáticas, ejecutará funciones semánticas y retornará tokens (códigos 256-286).

---

## 2. Arquitectura de Clases (6 clases)
El agente debe generar exactamente estas 6 clases (sin `package`, todas en el mismo directorio):
1. `Clasificador.java`: Traduce un `char` a un índice de columna (0-18).
2. `Lector.java`: Lee el archivo con soporte de `unread` y cuenta líneas.
3. `ContextoLexico.java`: "Mochila" con buffers, tabla de símbolos y errores.
4. `Funciones.java`: Métodos estáticos (F1-F14, FE) que modifican el contexto.
5. `Matrices.java`: Arrays estáticos con las 3 matrices y el mapeo de estados.
6. `AnalizadorLexico.java`: El motor (`yylex()`) que une todo.
7. `Main.java`: El driver que abre el archivo y llama a `yylex()` en bucle.

---

## 3. Las 3 Matrices (Copiar y pegar literalmente)

### 3.1. Columnas (Índices 0 a 18)
0: Letra | 1: Dígito | 2: `=` | 3: `<` | 4: `>` | 5: `/` | 6: `*` | 7: `"` | 8: `+` | 9: `-` | 10: `(` | 11: `)` | 12: `{` | 13: `}` | 14: `,` | 15: CR (`\r`) | 16: LF (`\n`) | 17: Espacio/Tab | 18: Otro.

### 3.2. Matriz de Transición (`TRANSICION`)
*Filas: E0 a E22 y EF.*
```text
E0:  {E1, E2, E3, E5, E8, E10, E16, E13, E14, E15, E17, E18, E19, E20, E21, E0, E22, E0, EF}
E1:  {E1, E1, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF}
E2:  {EF, E2, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF}
E3:  {EF, EF, E4, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF}
E4:  {EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF}
E5:  {EF, EF, E7, EF, E6, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF}
E6:  {EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF}
E7:  {EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF}
E8:  {EF, EF, E9, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF}
E9:  {EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF}
E10: {EF, EF, EF, EF, EF, EF, E11, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF}
E11: {E11, E11, E11, E11, E11, E11, E12, E11, E11, E11, E11, E11, E11, E11, E11, E11, E11, E11, E11}
E12: {E11, E11, E11, E11, E11, E0, E11, E11, E11, E11, E11, E11, E11, E11, E11, E11, E11, E11, E11}
E13: {E13, E13, E13, E13, E13, E13, E13, EF, E13, E13, E13, E13, E13, E13, E13, EF, EF, E13, E13}
E14: {EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF}
E15: {EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF}
E16: {EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF}
E17: {EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF}
E18: {EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF}
E19: {EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF}
E20: {EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF}
E21: {EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF}
E22: {EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF}
EF:  {EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF, EF}
### 3.3. Matriz de Tokens (TOKEN)
*Filas: E0 a E22 y EF. Si la celda es -1, no emite token.*
```text
E0:  {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}
E1:  {-1, -1, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256}
E2:  {-1, -1, 257, 257, 257, 257, 257, 257, 257, 257, 257, 257, 257, 257, 257, 257, 257, 257, 257}
E3:  {269, 269, -1, 269, 269, 269, 269, 269, 269, 269, 269, 269, 269, 269, 269, 269, 269, 269, 269}
E4:  {270, 270, 270, 270, 270, 270, 270, 270, 270, 270, 270, 270, 270, 270, 270, 270, 270, 270, 270}
E5:  {272, 272, -1, 272, -1, 272, 272, 272, 272, 272, 272, 272, 272, 272, 272, 272, 272, 272, 272}
E6:  {271, 271, 271, 271, 271, 271, 271, 271, 271, 271, 271, 271, 271, 271, 271, 271, 271, 271, 271}
E7:  {273, 273, 273, 273, 273, 273, 273, 273, 273, 273, 273, 273, 273, 273, 273, 273, 273, 273, 273}
E8:  {274, 274, -1, 274, 274, 274, 274, 274, 274, 274, 274, 274, 274, 274, 274, 274, 274, 274, 274}
E9:  {275, 275, 275, 275, 275, 275, 275, 275, 275, 275, 275, 275, 275, 275, 275, 275, 275, 275, 275}
E10: {280, 280, 280, 280, 280, 280, -1, 280, 280, 280, 280, 280, 280, 280, 280, 280, 280, 280, 280}
E11: {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}
E12: {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}
E13: {-1, -1, -1, -1, -1, -1, -1, 258, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}
E14: {277, 277, 277, 277, 277, 277, 277, 277, 277, 277, 277, 277, 277, 277, 277, 277, 277, 277, 277}
E15: {278, 278, 278, 278, 278, 278, 278, 278, 278, 278, 278, 278, 278, 278, 278, 278, 278, 278, 278}
E16: {279, 279, 279, 279, 279, 279, 279, 279, 279, 279, 279, 279, 279, 279, 279, 279, 279, 279, 279}
E17: {281, 281, 281, 281, 281, 281, 281, 281, 281, 281, 281, 281, 281, 281, 281, 281, 281, 281, 281}
E18: {282, 282, 282, 282, 282, 282, 282, 282, 282, 282, 282, 282, 282, 282, 282, 282, 282, 282, 282}
E19: {283, 283, 283, 283, 283, 283, 283, 283, 283, 283, 283, 283, 283, 283, 283, 283, 283, 283, 283}
E20: {284, 284, 284, 284, 284, 284, 284, 284, 284, 284, 284, 284, 284, 284, 284, 284, 284, 284, 284}
E21: {285, 285, 285, 285, 285, 285, 285, 285, 285, 285, 285, 285, 285, 285, 285, 285, 285, 285, 285}
E22: {286, 286, 286, 286, 286, 286, 286, 286, 286, 286, 286, 286, 286, 286, 286, 286, 286, 286, 286}
EF:  {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}
```
### 3.4. Matriz de Funciones (FUNCION)
*El agente debe crear una interfaz Func y mapear las referencias a métodos de la clase Funciones.*
*Las celdas con null significan que el autómata transiciona y no ejecuta función. Las celdas con f15 acumulan el segundo carácter de un operador de 2 símbolos.*
```text
E0:  {f1, f2, f10, f10, f10, f10, f10, f3, f10, f10, f10, f10, f10, f10, f10, f4, f8, f4, fe}
E1:  {f5, f5, f7, f7, f7, f7, f7, f7, f7, f7, f7, f7, f7, f7, f7, f7, f7, f7, f7}
E2:  {fe, f6, f9, f9, f9, f9, f9, f9, f9, f9, f9, f9, f9, f9, f9, f9, f9, f9, f9}
E3:  {f10, f10, f15, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10}
E4:  {f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10}
E5:  {f10, f10, f15, f10, f15, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10}
E6:  {f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10}
E7:  {f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10}
E8:  {f10, f10, f15, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10}
E9:  {f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10}
E10: {f10, f10, f10, f10, f10, f10, null, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10}
E11: {f14, f14, f14, f14, f14, f14, f14, f14, f14, f14, f14, f14, f14, f14, f14, f14, f14, f14, f14}
E12: {f14, f14, f14, f14, f14, f12, f14, f14, f14, f14, f14, f14, f14, f14, f14, f14, f14, f14, f14}
E13: {f13, f13, f13, f13, f13, f13, f13, f11, f13, f13, f13, f13, f13, f13, f13, fe, fe, f13, f13}
E14: {f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10}
E15: {f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10}
E16: {f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10}
E17: {f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10}
E18: {f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10}
E19: {f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10}
E20: {f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10}
E21: {f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10, f10}
E22: {f8, f8, f8, f8, f8, f8, f8, f8, f8, f8, f8, f8, f8, f8, f8, f8, f8, f8, f8}
EF:  {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null}
```
### 4. Especificación de las Funciones Semánticas
*El agente debe implementar los siguientes métodos public static void en Funciones.java. Todos reciben (ContextoLexico ctx, int c).*

- **f1 (Iniciar letras):** `ctx.bufferLetras.setLength(0); ctx.bufferLetras.append((char)c);`
- **f2 (Iniciar dígitos):** `ctx.bufferDigitos.setLength(0); ctx.bufferDigitos.append((char)c);`
- **f3 (Abrir literal):** `ctx.bufferTexto.setLength(0);`
- **f4 (Ignorar):** No hace nada.
- **f5 (Acumular letra):** `ctx.bufferLetras.append((char)c);`
- **f6 (Acumular dígito):** `ctx.bufferDigitos.append((char)c);`
- **f7 (Retornar ID/Palabra):**
    - Setea `ctx.lexemaActual = ctx.bufferLetras.toString();`
    - Setea `ctx.debeHacerUnread = true;`
    - Evalúa el lexema con un switch (ej: "entero" -> 260, "si" -> 261, etc.).
    - Si no es palabra reservada, asigna 256 (ID) y lo guarda en `ctx.tablaSimbolos`.
- **f8 (Fin de línea):** `ctx.tokenActual = 286; ctx.debeHacerUnread = false;` *(Importante: NO hacer unread para evitar bucles infinitos en saltos de línea).*
- **f9 (Retornar CTE):**
    - Setea `ctx.lexemaActual = ctx.bufferDigitos.toString();`
    - Setea `ctx.debeHacerUnread = true;`
    - Setea `ctx.tokenActual = 257;`
    - Guarda en tabla de símbolos.
- **f10 (Token directo):** `ctx.debeHacerUnread = true;` *(El token real lo saca el motor de la matriz de tokens).*
- **f11 (Cerrar literal):** `ctx.tokenActual = 258; ctx.lexemaActual = ctx.bufferTexto.toString(); ctx.debeHacerUnread = false;` *(No unread, la comilla se consume).*
- **f12 (Cerrar comentario):** `ctx.debeHacerUnread = false;`
- **f13 (Acumular texto):** `ctx.bufferTexto.append((char)c);`
- **f14 (Consumir comentario):** No hace nada.
- **f15 (NUEVA - Acumular operador doble):** `ctx.bufferLexema.append((char)c); ctx.debeHacerUnread = false;`
- **fe (Error):** Agrega un mensaje de error a `ctx.errores. ctx.debeHacerUnread = false;`

### 5. El Motor ( AnalizadorLexico.java )
*Este es el corazón del programa. El agente DEBE implementarlo exactamente así para evitar problemas de loop:*

```
public int yylex() throws Exception {
    ctx.reset();
    String estado = Matrices.E0;

    while (!estado.equals(Matrices.EF)) {
        int c = lector.nextChar();
        
        if (c == -1) {
            return 276; // Token EOF
        }

        int fila = Matrices.fila(estado);
        int col = Clasificador.getEvento(c);
        
        ctx.filaActual = fila;
        ctx.colActual = col;

        // 1. Ejecutar función
        Matrices.Func f = Matrices.FUNCION[fila][col];
        if (f != null) {
            f.ejecutar(ctx, c);
        }

        // 2. Hacer unread ANTES del break (por si f7 o f9 lo piden)
        if (ctx.debeHacerUnread) {
            lector.unread(c);
            ctx.debeHacerUnread = false;
        }

        // 3. Si hay token, cortamos
        if (ctx.tokenActual != -1) {
            break;
        }

        // 4. Transicionar
        estado = Matrices.TRANSICION[fila][col];
    }

    if (ctx.bufferLexema.length() > 0) {
        ctx.lexemaActual = ctx.bufferLexema.toString();
    }

    return ctx.tokenActual;
}

```

### 6. Especificación del Lector.java
*Para evitar errores de IOException y manejar el retroceso correctamente, usar PushbackReader.*

```
import java.io.*;

public class Lector {
    private PushbackReader reader;
    private int linea = 1;

    public Lector(String archivo) throws IOException {
        this.reader = new PushbackReader(new FileReader(archivo), 1);
    }

    public int nextChar() throws IOException {
        int c = reader.read();
        if (c == '\n') linea++;
        return c;
    }

    public void unread(int c) throws IOException {
        if (c != -1) {
            if (c == '\n') linea--;
            reader.unread(c);
        }
    }

    public int getLinea() { return linea; }
}
```
## 7. Instrucciones para el Agente de IA (Estándar de Desarrollo)

### 1. Arquitectura y Paquetes:

* Las clases lógicas del analizador (`AnalizadorLexico`, `ContextoLexico`, `Funciones`, `Lector`, `Matrices`, `Clasificador`) deben pertenecer al paquete `lexico`.
* La clase `Main` debe estar en el paquete por defecto (raíz de `src`) e importar `lexico.AnalizadorLexico`.
* **Estructura de directorios requerida:**
```
Proyecto/
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
### 2. No inventar código de más:

* No agregar excepciones personalizadas ni jerarquías de clases complejas.
* Usar exclusivamente las colecciones estándar de Java (`java.util.List`, `java.util.Map`, `java.util.HashMap`, `java.util.ArrayList`).

### 3. Manejo de Errores:

* El método `yylex()` debe declarar `throws Exception` (o `throws IOException`) para evitar ensuciar el código con bloques `try-catch` en cada función semántica o en cada lectura de carácter.
* Los errores léxicos se guardan en la lista del contexto (`ctx.errores`), no se lanzan como excepciones que detengan el programa.

### 4. Tabla de Símbolos:

* Implementar de forma simple usando `HashMap<String, String>` dentro de la clase `ContextoLexico`.

### 5. Debugging y Estado:

* La clase `ContextoLexico` debe tener un método `reset()` que se llame al inicio de cada `yylex()`. Este método debe:
    * **Limpiar los buffers** (`setLength(0)`).
    * **Resetear** `tokenActual = -1`.
    * **Resetear** `debeHacerUnread = false`.

### 6. Archivo de Prueba (`prueba.txt`):

* El agente debe generar un archivo `prueba.txt` en la raíz del proyecto que contenga declaraciones, condicionales, comentarios y operadores para probar el flujo completo.
* **Ejemplo sugerido:**

```text
x = 42
si (x == 42)
{
    mostrar("Es la respuesta")
}
/* Comentario final */
```

### 7. Instrucciones de Compilación:

* El agente debe indicar claramente cómo compilar y ejecutar usando la terminal, respetando la estructura de paquetes (ej. `javac -d out src/Main.java src/lexico/*.java` y `java -cp out Main`).


