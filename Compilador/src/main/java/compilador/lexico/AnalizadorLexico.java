package compilador.lexico;

import compilador.lexico.datos.Clasificador;
import compilador.lexico.datos.MatrizAcciones;
import compilador.lexico.datos.MatrizTransicion;
import compilador.lexico.modelo.TipoToken;
import compilador.lexico.modelo.Token;
import compilador.lexico.semantica.GestorFuncionesSemanticas;

import java.io.IOException;
import java.io.Reader;
import java.util.Map;

public final class AnalizadorLexico {
    private final Reader lector;
    private final TablaSimbolos tablaSimbolos = new TablaSimbolos();
    private final GestorFuncionesSemanticas gestor = new GestorFuncionesSemanticas();
    private int linea = 1;
    private int pushback = -1;

    public static final Map<String, Integer> RESERVADAS = Map.ofEntries(
            Map.entry("principal", TipoToken.PRINCIPAL),
            Map.entry("entero", TipoToken.ENTERO),
            Map.entry("si", TipoToken.SI),
            Map.entry("bucle", TipoToken.BUCLE),
            Map.entry("hasta", TipoToken.HASTA),
            Map.entry("mostrar", TipoToken.MOSTRAR),
            Map.entry("mostrarTexto", TipoToken.MOSTRAR_TXT),
            Map.entry("y", TipoToken.Y),
            Map.entry("o", TipoToken.O),
            Map.entry("retornar", TipoToken.RETORNAR)
    );

    public AnalizadorLexico(Reader lector) {
        this.lector = lector;
    }

    public Token proximoToken() throws IOException {
        int estado = 0;
        gestor.limpiarLetras();
        gestor.limpiarDigitos();
        gestor.limpiarTexto();

        while (true) {
            int c = leer();
            if (c == -1) {
                return new Token(TipoToken.EOF, "EOF", linea);
            }

            int evento = Clasificador.obtenerColumna(c);
            MatrizAcciones.ejecutarAccion(estado, evento, c, gestor);
            MatrizTransicion.Resultado resultado = MatrizTransicion.resolver(estado, evento, c, this, gestor);

            if (resultado.esContinuacion()) {
                estado = resultado.getEstado();
                continue;
            }
            if (resultado.getToken() != null) {
                return resultado.getToken();
            }
            if (resultado.getError() != null) {
                throw resultado.getError();
            }
        }
    }

    public int leerDesdeAnalizador() throws IOException {
        return leer();
    }

    public void unreadDesdeAnalizador(int c) {
        unread(c);
    }

    public void incrementarLinea() {
        linea++;
    }

    public void registrarEnTabla(String lexema, int codigo) {
        tablaSimbolos.registrar(lexema, codigo);
    }

    public Token crearToken(int codigo, String lexema) {
        return new Token(codigo, lexema, linea);
    }

    private int leer() throws IOException {
        if (pushback != -1) {
            int c = pushback;
            pushback = -1;
            return c;
        }
        return lector.read();
    }

    private void unread(int c) {
        pushback = c;
    }

    public int getLineaActual() { return linea; }
    public TablaSimbolos getTablaSimbolos() { return tablaSimbolos; }
}
