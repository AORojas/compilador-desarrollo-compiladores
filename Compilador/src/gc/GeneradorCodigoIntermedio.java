package gc;

import ast.Hoja;
import ast.Nodo;
import ast.NodoAST;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import semantico.Simbolo;
import semantico.TablaSimbolos;

public final class GeneradorCodigoIntermedio {
    private static final class SaltosCondicion {
        private final List<Integer> verdaderos = new ArrayList<Integer>();
        private final List<Integer> falsos = new ArrayList<Integer>();
    }

    private final TablaSimbolos tablaSimbolos;
    private final PolacaInversa polaca;
    private final Map<String, Deque<Simbolo>> variablesPendientes =
        new LinkedHashMap<String, Deque<Simbolo>>();
    private final Deque<Map<String, Simbolo>> ambitos = new ArrayDeque<Map<String, Simbolo>>();
    private boolean generado;

    public GeneradorCodigoIntermedio(TablaSimbolos tablaSimbolos) {
        if (tablaSimbolos == null) {
            throw new IllegalArgumentException("La Tabla de Símbolos es obligatoria.");
        }
        this.tablaSimbolos = tablaSimbolos;
        this.polaca = new PolacaInversa();
        prepararVariables();
    }

    public void generar(NodoAST raiz) {
        if (generado) {
            throw new IllegalStateException("Cada generador admite una única generación.");
        }
        generado = true;
        if (raiz != null) {
            procesarNodo(raiz);
        }
        polaca.resolverDestinos();
    }

    public PolacaInversa getPolaca() {
        return polaca;
    }

    private void prepararVariables() {
        for (Simbolo simbolo : tablaSimbolos.obtenerSimbolos()) {
            if ("variable".equals(simbolo.clase)) {
                Deque<Simbolo> coincidencias = variablesPendientes.get(simbolo.nombre);
                if (coincidencias == null) {
                    coincidencias = new ArrayDeque<Simbolo>();
                    variablesPendientes.put(simbolo.nombre, coincidencias);
                }
                coincidencias.addLast(simbolo);
            }
        }
    }

    private void procesarNodo(NodoAST nodo) {
        if (nodo instanceof Hoja) {
            emitirHoja((Hoja) nodo);
        } else if (nodo instanceof Nodo) {
            Nodo rama = (Nodo) nodo;
            String operador = rama.operador;

            if (esOperadorBinario(operador)) {
                procesarNodo(rama.izquierdo);
                procesarNodo(rama.derecho);
                polaca.addOperador(operador);
            } else if ("=".equals(operador) || ":=".equals(operador)) {
                procesarNodo(rama.derecho);
                emitirVariable(rama.izquierdo);
                polaca.addOperador(":=");
            } else if ("SI".equals(operador)) {
                procesarSi(rama);
            } else if ("BUCLE".equals(operador)) {
                procesarBucle(rama);
            } else if ("MOSTRAR".equals(operador)) {
                procesarNodo(rama.izquierdo);
                polaca.addOperador("PRINT");
            } else if ("MOSTRAR_TXT".equals(operador) || "MOSTRAR_TEXTO".equals(operador)) {
                emitirLiteralTexto(rama.izquierdo);
                polaca.addOperador("PRINT");
            } else if ("RETORNAR".equals(operador)) {
                procesarNodo(rama.izquierdo);
                polaca.addOperador("RET");
            } else if ("LLAMADA".equals(operador)) {
                procesarNodo(rama.derecho);
                emitirFuncion(rama.izquierdo);
                polaca.addOperador("CALL");
            } else if ("FUNCION".equals(operador)) {
                procesarFuncion(rama);
            } else if ("DECL".equals(operador) || "DECLARACION".equals(operador)) {
                registrarDeclaraciones(rama.derecho != null ? rama.derecho : rama.izquierdo);
            } else if ("Y".equals(operador) || "O".equals(operador)) {
                throw new IllegalStateException(
                    "Una condición lógica debe generarse dentro de SI o BUCLE.");
            } else {
                if (rama.izquierdo != null) {
                    procesarNodo(rama.izquierdo);
                }
                if (rama.derecho != null) {
                    procesarNodo(rama.derecho);
                }
            }
        }
    }

    private void procesarFuncion(Nodo funcion) {
        Nodo datosFuncion = funcion.izquierdo instanceof Nodo ? (Nodo) funcion.izquierdo : null;
        entrarAmbito();
        try {
            if (datosFuncion != null && datosFuncion.izquierdo instanceof Hoja) {
                registrarVariable(((Hoja) datosFuncion.izquierdo).valor);
            }
            procesarNodo(funcion.derecho);
        } finally {
            salirAmbito();
        }
    }

    private void procesarSi(Nodo seleccion) {
        SaltosCondicion saltos = generarCondicion(seleccion.izquierdo);
        int inicioBloque = polaca.getPos();
        parchear(saltos.verdaderos, inicioBloque);

        entrarAmbito();
        try {
            procesarNodo(seleccion.derecho);
        } finally {
            salirAmbito();
        }

        parchear(saltos.falsos, polaca.getPos());
    }

    private void procesarBucle(Nodo iteracion) {
        int inicio = polaca.getPos();
        entrarAmbito();
        try {
            procesarNodo(iteracion.izquierdo);
            SaltosCondicion saltos = generarCondicion(iteracion.derecho);
            parchear(saltos.falsos, inicio);
            parchear(saltos.verdaderos, polaca.getPos());
        } finally {
            salirAmbito();
        }
    }

    private SaltosCondicion generarCondicion(NodoAST condicion) {
        if (!(condicion instanceof Nodo)) {
            throw new IllegalStateException("La estructura de una condición está incompleta.");
        }

        Nodo rama = (Nodo) condicion;
        if ("Y".equals(rama.operador)) {
            SaltosCondicion izquierda = generarCondicion(rama.izquierdo);
            parchear(izquierda.verdaderos, polaca.getPos());
            SaltosCondicion derecha = generarCondicion(rama.derecho);
            derecha.falsos.addAll(izquierda.falsos);
            return derecha;
        }
        if ("O".equals(rama.operador)) {
            SaltosCondicion izquierda = generarCondicion(rama.izquierdo);
            parchear(izquierda.falsos, polaca.getPos());
            SaltosCondicion derecha = generarCondicion(rama.derecho);
            izquierda.verdaderos.addAll(derecha.verdaderos);
            izquierda.falsos.clear();
            izquierda.falsos.addAll(derecha.falsos);
            return izquierda;
        }
        if (!esComparador(rama.operador)) {
            throw new IllegalStateException(
                "Operador no válido dentro de una condición: " + rama.operador);
        }

        procesarNodo(rama.izquierdo);
        procesarNodo(rama.derecho);
        polaca.addOperador(rama.operador);

        SaltosCondicion saltos = new SaltosCondicion();
        saltos.falsos.add(polaca.addSaltoPendiente("BF"));
        saltos.verdaderos.add(polaca.addSaltoPendiente("BI"));
        return saltos;
    }

    private void registrarDeclaraciones(NodoAST listaIds) {
        if (listaIds instanceof Hoja) {
            registrarVariable(((Hoja) listaIds).valor);
        } else if (listaIds instanceof Nodo) {
            Nodo lista = (Nodo) listaIds;
            registrarDeclaraciones(lista.izquierdo);
            registrarDeclaraciones(lista.derecho);
        }
    }

    private void registrarVariable(String nombre) {
        Deque<Simbolo> coincidencias = variablesPendientes.get(nombre);
        if (coincidencias == null || coincidencias.isEmpty()) {
            throw new IllegalStateException(
                "No se encontró en la Tabla de Símbolos la variable '" + nombre + "'.");
        }
        ambitos.peek().put(nombre, coincidencias.removeFirst());
    }

    private void emitirHoja(Hoja hoja) {
        if (hoja.valor == null) {
            throw new IllegalArgumentException("No se puede emitir una hoja sin valor.");
        }
        int id;
        if (hoja.valor.matches("[0-9]+")) {
            id = tablaSimbolos.buscarId(hoja.valor, "constante");
        } else {
            Simbolo variable = buscarVariableVisible(hoja.valor);
            id = variable == null ? tablaSimbolos.buscarId(hoja.valor, "variable") : variable.id;
        }
        if (id < 0) {
            throw new IllegalStateException(
                "No se encontró el símbolo '" + hoja.valor + "' en la Tabla de Símbolos.");
        }
        polaca.addReferencia(id);
    }

    private void emitirVariable(NodoAST nodo) {
        if (!(nodo instanceof Hoja)) {
            throw new IllegalStateException("El destino de asignación debe ser una hoja.");
        }
        Hoja hoja = (Hoja) nodo;
        Simbolo simbolo = buscarVariableVisible(hoja.valor);
        if (simbolo == null) {
            throw new IllegalStateException(
                "No se encontró la variable '" + hoja.valor + "' en el ámbito activo.");
        }
        polaca.addReferencia(simbolo.id);
    }

    private void emitirLiteralTexto(NodoAST nodo) {
        if (!(nodo instanceof Hoja)) {
            throw new IllegalStateException("El literal de texto debe estar representado por una hoja.");
        }
        String valor = ((Hoja) nodo).valor;
        int id = tablaSimbolos.buscarId(valor, "constante");
        if (id < 0) {
            throw new IllegalStateException(
                "No se encontró el literal de texto '" + valor + "' en la Tabla de Símbolos.");
        }
        polaca.addReferencia(id);
    }

    private void emitirFuncion(NodoAST nodo) {
        if (!(nodo instanceof Hoja)) {
            throw new IllegalStateException("El nombre de la función debe estar representado por una hoja.");
        }
        String nombre = ((Hoja) nodo).valor;
        int id = tablaSimbolos.buscarId(nombre, "funcion");
        if (id < 0) {
            throw new IllegalStateException(
                "No se encontró la función '" + nombre + "' en la Tabla de Símbolos.");
        }
        polaca.addReferencia(id);
    }

    private Simbolo buscarVariableVisible(String nombre) {
        for (Map<String, Simbolo> ambito : ambitos) {
            Simbolo simbolo = ambito.get(nombre);
            if (simbolo != null) {
                return simbolo;
            }
        }
        return null;
    }

    private void entrarAmbito() {
        ambitos.push(new HashMap<String, Simbolo>());
    }

    private void salirAmbito() {
        if (ambitos.isEmpty()) {
            throw new IllegalStateException("No hay un ámbito activo para cerrar.");
        }
        ambitos.pop();
    }

    private void parchear(List<Integer> saltos, int destino) {
        for (Integer salto : saltos) {
            polaca.patch(salto.intValue(), destino);
        }
    }

    private boolean esOperadorBinario(String operador) {
        return "+".equals(operador)
            || "-".equals(operador)
            || "*".equals(operador)
            || "/".equals(operador)
            || esComparador(operador);
    }

    private boolean esComparador(String operador) {
        return ">".equals(operador)
            || "<".equals(operador)
            || ">=".equals(operador)
            || "<=".equals(operador)
            || "==".equals(operador)
            || "<>".equals(operador);
    }
}
