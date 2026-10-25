package lexico;

public final class Funciones {
    private Funciones() {
    }

    public static void f1(ContextoLexico ctx, int c) {
        ctx.bufferLetras.setLength(0);
        ctx.bufferLetras.append((char) c);
    }

    public static void f2(ContextoLexico ctx, int c) {
        ctx.bufferDigitos.setLength(0);
        ctx.bufferDigitos.append((char) c);
    }

    public static void f3(ContextoLexico ctx, int c) {
        ctx.bufferTexto.setLength(0);
    }

    public static void f4(ContextoLexico ctx, int c) {
    }

    public static void f5(ContextoLexico ctx, int c) {
        ctx.bufferLetras.append((char) c);
    }

    public static void f6(ContextoLexico ctx, int c) {
        ctx.bufferDigitos.append((char) c);
    }

    public static void f7(ContextoLexico ctx, int c) {
        ctx.lexemaActual = ctx.bufferLetras.toString();
        ctx.debeHacerUnread = true;
        switch (ctx.lexemaActual) {
            case "principal": ctx.tokenActual = 259; break;
            case "entero": ctx.tokenActual = 260; break;
            case "si": ctx.tokenActual = 261; break;
            case "bucle": ctx.tokenActual = 262; break;
            case "hasta": ctx.tokenActual = 263; break;
            case "mostrar": ctx.tokenActual = 264; break;
            case "mostrarTexto": ctx.tokenActual = 265; break;
            case "y": ctx.tokenActual = 266; break;
            case "o": ctx.tokenActual = 267; break;
            case "retornar": ctx.tokenActual = 268; break;
            default: ctx.tokenActual = 256; ctx.tablaSimbolos.put(ctx.lexemaActual, "ID"); break;
        }
    }

    public static void f8(ContextoLexico ctx, int c) {
        ctx.lexemaActual = "\n";
        ctx.tokenActual = 286;
        ctx.debeHacerUnread = false;
    }

    public static void f9(ContextoLexico ctx, int c) {
        ctx.lexemaActual = ctx.bufferDigitos.toString();
        ctx.debeHacerUnread = true;
        ctx.tokenActual = 257;
        ctx.tablaSimbolos.put(ctx.lexemaActual, "CTE");
    }

    public static void f10(ContextoLexico ctx, int c) {
    ctx.tokenActual = Matrices.TOKEN[ctx.filaActual][ctx.colActual];
    if (ctx.bufferLexema.length() == 0) {
        ctx.bufferLexema.append((char) c);
    }
    // Si el buffer ya tiene este carácter, es porque lo estamos leyendo de nuevo
    // (bucle). En ese caso, NO hacemos unread.
    // Si el carácter es distinto al buffer, es un carácter que no pertenece al token
    // (como la 'c' del (contador), y SÍ hacemos unread.
    char actual = (char) c;
    char enBuffer = ctx.bufferLexema.charAt(0);
    ctx.debeHacerUnread = (actual != enBuffer);
}

    public static void f15(ContextoLexico ctx, int c) {
        ctx.bufferLexema.append((char) c);
        ctx.debeHacerUnread = false; // El 2do caracter del operador no se retrocede
    }

    public static void f11(ContextoLexico ctx, int c) {
        ctx.tokenActual = 258;
        ctx.lexemaActual = ctx.bufferTexto.toString();
        ctx.debeHacerUnread = false;
    }

    public static void f12(ContextoLexico ctx, int c) {
        ctx.debeHacerUnread = false;
    }

    public static void f13(ContextoLexico ctx, int c) {
        ctx.bufferTexto.append((char) c);
    }

     public static void f14(ContextoLexico ctx, int c) {
        // En comentarios no hacemos nada
    }

      public static void fe(ContextoLexico ctx, int c) {
        ctx.errores.add("Línea " + ctx.lector.getLinea() + ": Error léxico, carácter no válido '" + (char) c + "'.");
        ctx.debeHacerUnread = false;
    }
}
