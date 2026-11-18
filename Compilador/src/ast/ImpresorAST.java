package ast;

import java.util.ArrayList;
import java.util.List;

public final class ImpresorAST {
    private ImpresorAST() {
    }

    public static void imprimir(NodoAST raiz) {
        if (raiz != null) {
            System.out.println(etiqueta(raiz));
            imprimirHijos(raiz, "");
        }
    }

    private static void imprimirHijos(NodoAST nodo, String prefijo) {
        if (!(nodo instanceof Nodo)) {
            return;
        }

        Nodo rama = (Nodo) nodo;
        List<NodoAST> hijos = new ArrayList<NodoAST>(2);
        if (rama.izquierdo != null) {
            hijos.add(rama.izquierdo);
        }
        if (rama.derecho != null) {
            hijos.add(rama.derecho);
        }

        for (int i = 0; i < hijos.size(); i++) {
            boolean ultimo = i == hijos.size() - 1;
            NodoAST hijo = hijos.get(i);
            System.out.println(prefijo + (ultimo ? "└── " : "├── ") + etiqueta(hijo));
            imprimirHijos(hijo, prefijo + (ultimo ? "    " : "│   "));
        }
    }

    private static String etiqueta(NodoAST nodo) {
        if (nodo instanceof Hoja) {
            return "Hoja(" + ((Hoja) nodo).valor + ")";
        }
        return "Nodo(" + ((Nodo) nodo).operador + ")";
    }
}
