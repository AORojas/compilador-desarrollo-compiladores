package lexico;

import java.io.IOException;

public class AnalizadorLexico implements AutoCloseable {
    private final Lector lector;
    private final ContextoLexico ctx;

    public AnalizadorLexico(String archivo) throws IOException {
        lector = new Lector(archivo);
        ctx = new ContextoLexico();
        ctx.lector = lector; // Para que la función fe() pueda leer la línea del error
    }

    public int yylex() throws Exception {
        ctx.reset();
        String estado = Matrices.E0;

        while (!estado.equals(Matrices.EF)) {
            int c = lector.nextChar();
            
            if (c == -1) {
                if (Matrices.E0.equals(estado)
                        || "E11".equals(estado)
                        || "E12".equals(estado)) {
                    return 276;
                }
                if ("E13".equals(estado)) {
                    ctx.errores.add(
                        "Línea " + lector.getLinea() + ": Error léxico, literal de texto sin cerrar.");
                    return 276;
                }

                int fila = Matrices.fila(estado);
                int col = 18;
                ctx.filaActual = fila;
                ctx.colActual = col;
                Matrices.Func funcion = Matrices.FUNCION[fila][col];
                if (funcion != null) {
                    funcion.ejecutar(ctx, c);
                }
                return ctx.tokenActual == -1 ? 276 : ctx.tokenActual;
            }

            int fila = Matrices.fila(estado);
            int col = Clasificador.getEvento(c);

            if (Matrices.E0.equals(estado) && col != 15 && col != 17) {
                ctx.lineaToken = lector.getUltimaLineaLeida();
                ctx.columnaToken = lector.getUltimaColumnaLeida();
            }

            // Guardamos las coordenadas para que f10 resuelva el token
            ctx.filaActual = fila;
            ctx.colActual = col;

            // 1. Ejecutar la función semántica
            Matrices.Func f = Matrices.FUNCION[fila][col];
            if (f != null) {
                f.ejecutar(ctx, c);
            }

            // 2. Hacer unread si corresponde (SIEMPRE ANTES DEL BREAK)
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

            // Al terminar, pasamos lo acumulado en el buffer al lexema actual
            if (ctx.bufferLexema.length() > 0) {
                ctx.lexemaActual = ctx.bufferLexema.toString();
            }
        } 

        return ctx.tokenActual; 
    }

    public ContextoLexico getContexto() {
        return ctx;
    }

    public int getLinea() {
        return lector.getLinea();
    }

    public int getColumna() {
        return lector.getColumna();
    }

    public int getLineaToken() {
        return ctx.lineaToken;
    }

    public int getColumnaToken() {
        return ctx.columnaToken;
    }

    @Override
    public void close() throws IOException {
        lector.close();
    }
}
