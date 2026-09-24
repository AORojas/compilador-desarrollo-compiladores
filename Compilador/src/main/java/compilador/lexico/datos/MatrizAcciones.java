package compilador.lexico.datos;

import compilador.lexico.semantica.GestorFuncionesSemanticas;

public final class MatrizAcciones {
    private MatrizAcciones() {}

    private static final int[][] ACCIONES = {
        {1, 2, 1, 1, 1, 1, 1, 3, 1, 1, 1, 1, 1, 1, 1, 8, 4, 13},
        {5, 5, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7},
        {13, 6, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9},
        {10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10},
        {10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10},
        {10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10},
        {10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10},
        {10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10},
        {10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10},
        {10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10},
        {10, 10, 10, 10, 10, 10, 12, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10},
        {12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12},
        {12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12},
        {3, 3, 3, 3, 3, 3, 3, 11, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3},
        {10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10},
        {10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10},
        {10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10},
        {10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10},
        {10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10},
        {10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10},
        {10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10},
        {10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10},
        {8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8}
    };

    public static int accion(int estado, int evento) {
        if (estado < 0 || estado >= ACCIONES.length || evento < 0 || evento >= ACCIONES[estado].length) {
            return -1;
        }
        return ACCIONES[estado][evento];
    }

    public static void ejecutarAccion(int estado, int evento, int c, GestorFuncionesSemanticas gestor) {
        int accion = accion(estado, evento);
        if (accion == -1) {
            return;
        }

        switch (accion) {
            case 1 -> gestor.iniciarLetras((char) c);
            case 2 -> gestor.iniciarDigitos((char) c);
            case 3 -> gestor.abrirTexto(estado, (char) c);
            case 4 -> gestor.ignorarBlancos();
            case 5 -> gestor.acumularLetras((char) c);
            case 6 -> gestor.acumularDigitos((char) c);
            case 8 -> gestor.finLinea();
            case 9 -> gestor.validarConstante();
            case 10 -> gestor.tokenDirecto();
            case 11 -> gestor.cerrarTexto();
            case 12 -> gestor.manejarComentario();
            case 13 -> gestor.errorLexico();
            default -> { }
        }
    }
}
