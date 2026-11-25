package semantico;

import ast.Hoja;
import ast.Nodo;
import ast.NodoAST;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class AnalizadorSemantico {
    private final TablaSimbolos tablaSimbolos = new TablaSimbolos();
    private final GestorAmbitos gestorAmbitos = new GestorAmbitos(tablaSimbolos);
    private final List<String> errores = new ArrayList<String>();
    private int nivelAnidamiento;
    private int cantidadPrincipal;
    private boolean analizado;

    public void analizar(NodoAST raiz) {
        if (analizado) {
            throw new IllegalStateException("Cada analizador semántico admite un único análisis.");
        }
        analizado = true;
        if (raiz == null) {
            return;
        }

        registrarFunciones(raiz);
        analizarNodo(raiz);
        if (cantidadPrincipal == 0) {
            errores.add("E9 - Falta la función principal");
        } else if (cantidadPrincipal > 1) {
            errores.add("E9 - Función principal duplicada - 'principal'");
        }
    }

    public List<String> getErrores() {
        return Collections.unmodifiableList(errores);
    }

    public TablaSimbolos getTablaSimbolos() {
        return tablaSimbolos;
    }

    private void registrarFunciones(NodoAST nodo) {
        if (nodo == null || nodo instanceof Hoja) {
            return;
        }

        Nodo rama = (Nodo) nodo;
        if ("FUNCION".equals(rama.operador)) {
            String nombre = nombreFuncion(rama);
            if (nombre != null) {
                Nodo datosFuncion = rama.izquierdo instanceof Nodo ? (Nodo) rama.izquierdo : null;
                int cantidadParametros = datosFuncion != null && datosFuncion.izquierdo != null ? 1 : 0;
                tablaSimbolos.registrarFuncion(nombre, cantidadParametros);
                if ("principal".equals(nombre)) {
                    cantidadPrincipal++;
                }
            }
        }
        registrarFunciones(rama.izquierdo);
        registrarFunciones(rama.derecho);
    }

    private void analizarNodo(NodoAST nodo) {
        if (nodo == null) {
            return;
        }
        if (nodo instanceof Hoja) {
            analizarHoja((Hoja) nodo);
            return;
        }

        Nodo rama = (Nodo) nodo;
        String operador = rama.operador;
        if ("FUNCION".equals(operador)) {
            analizarFuncion(rama);
        } else if ("DECL".equals(operador) || "DECLARACION".equals(operador)) {
            analizarDeclaracion(rama.derecho != null ? rama.derecho : rama.izquierdo);
        } else if ("=".equals(operador) || ":=".equals(operador)) {
            analizarDestinoAsignacion(rama.izquierdo);
            analizarNodo(rama.derecho);
        } else if ("SI".equals(operador)) {
            analizarNodo(rama.izquierdo);
            entrarEstructura(rama.derecho);
        } else if ("BUCLE".equals(operador)) {
            nivelAnidamiento++;
            reportarAnidamientoSiCorresponde();
            gestorAmbitos.entrarAmbito();
            try {
                analizarNodo(rama.izquierdo);
                analizarNodo(rama.derecho);
            } finally {
                gestorAmbitos.salirAmbito();
                nivelAnidamiento--;
            }
        } else if ("LLAMADA".equals(operador)) {
            analizarLlamada(rama);
        } else if ("MOSTRAR_TXT".equals(operador) || "MOSTRAR_TEXTO".equals(operador)) {
            registrarLiteralTexto(rama.izquierdo);
        } else {
            analizarNodo(rama.izquierdo);
            analizarNodo(rama.derecho);
        }
    }

    private void analizarFuncion(Nodo funcion) {
        Nodo datosFuncion = funcion.izquierdo instanceof Nodo ? (Nodo) funcion.izquierdo : null;
        gestorAmbitos.entrarAmbito();
        try {
            if (datosFuncion != null && datosFuncion.izquierdo instanceof Hoja) {
                declarar(((Hoja) datosFuncion.izquierdo).valor);
            }
            analizarNodo(funcion.derecho);
        } finally {
            gestorAmbitos.salirAmbito();
        }
    }

    private void analizarDeclaracion(NodoAST listaIds) {
        if (listaIds instanceof Hoja) {
            declarar(((Hoja) listaIds).valor);
            return;
        }
        if (listaIds instanceof Nodo) {
            Nodo lista = (Nodo) listaIds;
            analizarDeclaracion(lista.izquierdo);
            analizarDeclaracion(lista.derecho);
        }
    }

    private void declarar(String nombre) {
        if (!gestorAmbitos.declararVariable(nombre)) {
            errores.add("E8 - Redefinición en el mismo ámbito - '" + nombre + "'");
        }
    }

    private void analizarDestinoAsignacion(NodoAST destino) {
        if (destino instanceof Hoja) {
            comprobarVariable(((Hoja) destino).valor);
        }
    }

    private void analizarLlamada(Nodo llamada) {
        String nombre = llamada.izquierdo instanceof Hoja
            ? ((Hoja) llamada.izquierdo).valor
            : null;
        int cantidadArgumentos = llamada.derecho == null ? 0 : 1;
        if (nombre == null || !tablaSimbolos.existeFuncion(nombre)) {
            errores.add("E6 - Llamada a función inexistente"
                + (nombre == null ? "" : " - '" + nombre + "'"));
        } else if (!tablaSimbolos.firmaCoincide(nombre, cantidadArgumentos)) {
            errores.add("E6 - Firma inválida para la función - '" + nombre + "'");
        }
        analizarNodo(llamada.derecho);
    }

    private void entrarEstructura(NodoAST bloque) {
        nivelAnidamiento++;
        reportarAnidamientoSiCorresponde();
        gestorAmbitos.entrarAmbito();
        try {
            analizarNodo(bloque);
        } finally {
            gestorAmbitos.salirAmbito();
            nivelAnidamiento--;
        }
    }

    private void reportarAnidamientoSiCorresponde() {
        if (nivelAnidamiento > 3) {
            errores.add("E5 - Anidamiento excedido en estructura si/bucle");
        }
    }

    private void registrarLiteralTexto(NodoAST texto) {
        if (texto instanceof Hoja) {
            String valor = ((Hoja) texto).valor;
            tablaSimbolos.registrarConstante(valor, "texto", gestorAmbitos.profundidadActual());
        }
    }

    private void comprobarVariable(String nombre) {
        if (nombre == null || !gestorAmbitos.variableVisible(nombre)) {
            errores.add("E4 - Variable no declarada"
                + (nombre == null ? "" : " - '" + nombre + "'"));
        }
    }

    private void analizarHoja(Hoja hoja) {
        if (hoja.valor == null) {
            return;
        }
        if (hoja.valor.matches("[0-9]+")) {
            tablaSimbolos.registrarConstante(
                hoja.valor,
                "entero",
                gestorAmbitos.profundidadActual());
        } else {
            comprobarVariable(hoja.valor);
        }
    }

    private String nombreFuncion(Nodo funcion) {
        if (!(funcion.izquierdo instanceof Nodo)) {
            return null;
        }
        String nombre = ((Nodo) funcion.izquierdo).operador;
        return "PRINCIPAL".equals(nombre) ? "principal" : nombre;
    }

}
