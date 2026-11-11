package sintactico;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.Map;

public final class GestorAmbitos {
    private final TablaSimbolos tabla;
    private final Deque<Integer> pila = new ArrayDeque<Integer>();
    private final Map<Integer, Integer> ambitosPadre = new LinkedHashMap<Integer, Integer>();
    private int siguienteAmbito = 1;

    public GestorAmbitos(TablaSimbolos tabla) {
        if (tabla == null) {
            throw new IllegalArgumentException("La tabla de símbolos es obligatoria.");
        }
        this.tabla = tabla;
        pila.push(0);
    }

    public int entrarAmbito() {
        int padre = getAmbitoActual();
        int nuevo = siguienteAmbito++;
        ambitosPadre.put(nuevo, padre);
        pila.push(nuevo);
        return nuevo;
    }

    public int salirAmbito() {
        if (pila.size() <= 1) {
            throw new IllegalStateException("No se puede cerrar el ámbito global.");
        }
        return pila.pop();
    }

    public int getAmbitoActual() {
        return pila.peek();
    }

    public int getProfundidad() {
        return pila.size() - 1;
    }

    public Simbolo registrar(String nombre, String tipo, String clase, String valor, int longitud) {
        return tabla.registrar(nombre, tipo, clase, valor, longitud, getAmbitoActual());
    }

    public Simbolo buscar(String nombre) {
        Integer ambito = getAmbitoActual();
        while (ambito != null) {
            Simbolo simbolo = tabla.buscar(nombre, ambito);
            if (simbolo != null) {
                return simbolo;
            }
            ambito = ambitosPadre.get(ambito);
        }
        return null;
    }

    public Simbolo buscarEnAmbitoActual(String nombre) {
        return tabla.buscar(nombre, getAmbitoActual());
    }
}
