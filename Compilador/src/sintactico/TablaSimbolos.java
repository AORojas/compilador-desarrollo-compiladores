package sintactico;

import java.util.LinkedHashMap;
import java.util.Map;

public class TablaSimbolos {
    private final Map<String, String> simbolos = new LinkedHashMap<String, String>();

    public void registrar(String nombre, String tipo) {
        simbolos.put(nombre, tipo);
    }

    public boolean contiene(String nombre) {
        return simbolos.containsKey(nombre);
    }

    public String tipoDe(String nombre) {
        return simbolos.get(nombre);
    }

    public Map<String, String> getSimbolos() {
        return new LinkedHashMap<String, String>(simbolos);
    }

    public void imprimir() {
        for (Map.Entry<String, String> entrada : simbolos.entrySet()) {
            System.out.println(entrada.getKey() + " -> " + entrada.getValue());
        }
    }
}
