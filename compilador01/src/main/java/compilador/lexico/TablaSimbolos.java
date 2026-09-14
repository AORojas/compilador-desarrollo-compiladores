package compilador.lexico;

import java.util.HashMap;
import java.util.Map;

public class TablaSimbolos {
    private final Map<String, Simbolo> tabla = new HashMap<>();

    public void registrar(String lexema, int codigo) {
        tabla.putIfAbsent(lexema, new Simbolo(lexema, codigo));
    }

    public Simbolo buscar(String lexema) {
        return tabla.get(lexema);
    }

    public Map<String, Simbolo> getTabla() {
        return new HashMap<>(tabla);
    }

    public static class Simbolo {
        private final String nombre;
        private final int codigo;

        public Simbolo(String nombre, int codigo) {
            this.nombre = nombre;
            this.codigo = codigo;
        }

        public String getNombre() {
            return nombre;
        }

        public int getCodigo() {
            return codigo;
        }
    }
}
