package semantico;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

public final class GestorAmbitos {
    private static final class Ambito {
        private final int profundidad;
        private final Map<String, Simbolo> variables = new HashMap<String, Simbolo>();

        private Ambito(int profundidad) {
            this.profundidad = profundidad;
        }
    }

    private final Deque<Ambito> pila = new ArrayDeque<Ambito>();
    private final TablaSimbolos tabla;

    public GestorAmbitos(TablaSimbolos tabla) {
        this.tabla = tabla;
        pila.push(new Ambito(0));
    }

    public void entrarAmbito() {
        pila.push(new Ambito(pila.peek().profundidad + 1));
    }

    public void salirAmbito() {
        if (pila.size() <= 1) {
            throw new IllegalStateException("No se puede cerrar el ámbito global.");
        }
        pila.pop();
    }

    public boolean declaradaEnAmbitoActual(String nombre) {
        return pila.peek().variables.containsKey(nombre);
    }

    public boolean declararVariable(String nombre) {
        Ambito actual = pila.peek();
        if (actual.variables.containsKey(nombre)) {
            return false;
        }

        Simbolo simbolo = tabla.registrar(nombre, "entero", "variable", null, actual.profundidad);
        actual.variables.put(nombre, simbolo);
        return true;
    }

    public boolean variableVisible(String nombre) {
        for (Ambito ambito : pila) {
            if (ambito.variables.containsKey(nombre)) {
                return true;
            }
        }
        return false;
    }

    public int profundidadActual() {
        return pila.peek().profundidad;
    }
}
