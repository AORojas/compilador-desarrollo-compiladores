package lexico;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContextoLexico {
    public final StringBuilder bufferLetras = new StringBuilder();
    public final StringBuilder bufferDigitos = new StringBuilder();
    public final StringBuilder bufferTexto = new StringBuilder();
    public final StringBuilder bufferLexema = new StringBuilder();
    public final Map<String, String> tablaSimbolos = new HashMap<String, String>();
    public final List<String> errores = new ArrayList<String>();
    public String lexemaActual = "";
    public int tokenActual = -1;
    public int filaActual = 0;
    public int colActual = 0;
    public boolean debeHacerUnread = false;
    public Lector lector;

    public void reset() {
        bufferLetras.setLength(0);
        bufferDigitos.setLength(0);
        bufferTexto.setLength(0);
        bufferLexema.setLength(0);
        lexemaActual = "";
        tokenActual = -1;
        debeHacerUnread = false;
    }
}
