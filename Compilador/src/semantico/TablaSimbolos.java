package semantico;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class TablaSimbolos {
    private final Map<Integer, Simbolo> simbolos = new LinkedHashMap<Integer, Simbolo>();
    private final Map<String, Simbolo> funciones = new LinkedHashMap<String, Simbolo>();
    private final Map<String, Integer> parametrosPorFuncion = new LinkedHashMap<String, Integer>();
    private int siguienteId = 1;

    public Simbolo registrar(
            String nombre,
            String tipo,
            String clase,
            String valor,
            int ambito) {
        Simbolo simbolo = new Simbolo(
            siguienteId++,
            nombre,
            tipo,
            clase,
            valor,
            nombre.length(),
            ambito);
        simbolos.put(simbolo.id, simbolo);
        return simbolo;
    }

    public Simbolo registrarConstante(String valor, String tipo, int ambito) {
        Simbolo simbolo = new Simbolo(
            siguienteId++,
            valor,
            tipo,
            "constante",
            valor,
            valor.length(),
            ambito);
        simbolos.put(simbolo.id, simbolo);
        return simbolo;
    }

    public void registrarFuncion(String nombre, int cantidadParametros) {
        Simbolo funcion = registrar(nombre, "entero", "funcion", null, 0);
        if (!funciones.containsKey(nombre)) {
            funciones.put(nombre, funcion);
            parametrosPorFuncion.put(nombre, cantidadParametros);
        }
    }

    public boolean firmaCoincide(String nombre, int cantidadParametros) {
        Integer parametros = parametrosPorFuncion.get(nombre);
        return parametros != null && parametros.intValue() == cantidadParametros;
    }

    public boolean existeFuncion(String nombre) {
        return funciones.containsKey(nombre);
    }

    public int buscarId(String nombre, String clase) {
        for (Simbolo simbolo : simbolos.values()) {
            if (simbolo.nombre.equals(nombre) && simbolo.clase.equals(clase)) {
                return simbolo.id;
            }
        }
        return -1;
    }

    public List<Simbolo> obtenerSimbolos() {
        return Collections.unmodifiableList(new ArrayList<Simbolo>(simbolos.values()));
    }

    public String formatear() {
        StringBuilder salida = new StringBuilder(
            "ID | Nombre | Tipo | Clase | Valor | Longitud | Ámbito");
        for (Simbolo simbolo : simbolos.values()) {
            salida.append(System.lineSeparator())
                .append(simbolo.id).append(" | ")
                .append(simbolo.nombre).append(" | ")
                .append(simbolo.tipo).append(" | ")
                .append(simbolo.clase).append(" | ")
                .append(simbolo.valor == null ? "" : simbolo.valor).append(" | ")
                .append(simbolo.longitud).append(" | ")
                .append(simbolo.ambito);
        }
        return salida.toString();
    }
}
