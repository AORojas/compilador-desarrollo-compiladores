package compilador.lexico;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class TablaSimbolos {
    private final Map<String, Integer> simbolos = new LinkedHashMap<>();

    public void registrar(String lexema, int token) {
        simbolos.putIfAbsent(lexema, token);
    }

    public Integer obtener(String lexema) {
        return simbolos.get(lexema);
    }

    public Map<String, Integer> todos() {
        return Collections.unmodifiableMap(simbolos);
    }

    public boolean contiene(String lexema) {
        return simbolos.containsKey(lexema);
    }
}
