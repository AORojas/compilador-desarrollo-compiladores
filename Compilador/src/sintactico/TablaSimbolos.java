package sintactico;

import java.io.PrintStream;
import java.util.LinkedHashMap;
import java.util.Map;

public final class TablaSimbolos {
    private final Map<Integer, Simbolo> simbolos = new LinkedHashMap<Integer, Simbolo>();
    private final Map<Integer, Map<String, Integer>> idsPorAmbito =
            new LinkedHashMap<Integer, Map<String, Integer>>();
    private int siguienteId;

    public Simbolo registrar(
            String nombre,
            String tipo,
            String clase,
            String valor,
            int longitud,
            int ambito) {
        if (existeEnAmbito(nombre, ambito)) {
            throw new IllegalArgumentException(
                "El símbolo '" + nombre + "' ya está registrado en el ámbito " + ambito + ".");
        }

        Simbolo simbolo = new Simbolo(
            siguienteId++, nombre, tipo, clase, valor, longitud, ambito);
        simbolos.put(simbolo.getId(), simbolo);
        Map<String, Integer> nombres = idsPorAmbito.get(ambito);
        if (nombres == null) {
            nombres = new LinkedHashMap<String, Integer>();
            idsPorAmbito.put(ambito, nombres);
        }
        nombres.put(nombre, simbolo.getId());
        return simbolo;
    }

    public Simbolo registrarConstante(String valor, String tipo, int longitud) {
        Simbolo existente = buscarConstante(valor, tipo);
        if (existente != null) {
            return existente;
        }
        Simbolo simbolo = new Simbolo(
            siguienteId++, valor, tipo, "constante", valor, longitud, 0);
        simbolos.put(simbolo.getId(), simbolo);
        return simbolo;
    }

    public Simbolo buscar(String nombre, int ambito) {
        Map<String, Integer> nombres = idsPorAmbito.get(ambito);
        if (nombres == null) {
            return null;
        }
        Integer id = nombres.get(nombre);
        return id == null ? null : simbolos.get(id);
    }

    public Simbolo buscarConstante(String valor, String tipo) {
        for (Simbolo simbolo : simbolos.values()) {
            if ("constante".equals(simbolo.getClase())
                    && tipo.equals(simbolo.getTipo())
                    && valor.equals(simbolo.getValor())) {
                return simbolo;
            }
        }
        return null;
    }

    public Simbolo buscarPorId(int id) {
        return simbolos.get(id);
    }

    public boolean existeEnAmbito(String nombre, int ambito) {
        Map<String, Integer> nombres = idsPorAmbito.get(ambito);
        return nombres != null && nombres.containsKey(nombre);
    }

    public Map<Integer, Simbolo> getSimbolos() {
        return new LinkedHashMap<Integer, Simbolo>(simbolos);
    }

    public void imprimir(PrintStream salida) {
        salida.println("Tabla de símbolos:");
        salida.println("ID | Nombre | Tipo | Clase | Valor | Longitud | Ámbito");
        for (Simbolo simbolo : simbolos.values()) {
            salida.println(
                simbolo.getId() + " | "
                    + simbolo.getNombre() + " | "
                    + simbolo.getTipo() + " | "
                    + simbolo.getClase() + " | "
                    + (simbolo.getValor() == null ? "" : simbolo.getValor()) + " | "
                    + simbolo.getLongitud() + " | "
                    + simbolo.getAmbito());
        }
    }
}
