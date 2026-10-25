package lexico;

public final class Matrices {
    @FunctionalInterface
    public interface Func {
        void ejecutar(ContextoLexico ctx, int c);
    }

    public static final String E0 = "E0";
    public static final String EF = "EF";

    private static final String[] ESTADOS = {
        "E0", "E1", "E2", "E3", "E4", "E5", "E6", "E7", "E8", "E9",
        "E10", "E11", "E12", "E13", "E14", "E15", "E16", "E17", "E18",
        "E19", "E20", "E21", "E22", "EF"
    };

    public static final String[][] TRANSICION = {
        {"E1", "E2", "E3", "E5", "E8", "E10", "E16", "E13", "E14", "E15", "E17", "E18", "E19", "E20", "E21", "E0", "E22", "E0", "EF"},
        {"E1", "E1", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF"},
        {"EF", "E2", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF"},
        {"EF", "EF", "E4", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF"},
        {"EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF"},
        {"EF", "EF", "E7", "EF", "E6", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF"},
        {"EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF"},
        {"EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF"},
        {"EF", "EF", "E9", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF"},
        {"EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF"},
        {"EF", "EF", "EF", "EF", "EF", "EF", "E11", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF"},
        {"E11", "E11", "E11", "E11", "E11", "E11", "E12", "E11", "E11", "E11", "E11", "E11", "E11", "E11", "E11", "E11", "E11", "E11", "E11"},
        {"E11", "E11", "E11", "E11", "E11", "E0", "E11", "E11", "E11", "E11", "E11", "E11", "E11", "E11", "E11", "E11", "E11", "E11", "E11"},
        {"E13", "E13", "E13", "E13", "E13", "E13", "E13", "EF", "E13", "E13", "E13", "E13", "E13", "E13", "E13", "EF", "EF", "E13", "E13"},
        {"EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF"},
        {"EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF"},
        {"EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF"},
        {"EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF"},
        {"EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF"},
        {"EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF"},
        {"EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF"},
        {"EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF"},
        {"EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF"},
        {"EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF", "EF"}
    };

    public static final int[][] TOKEN = {
        {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {-1, -1, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256, 256},
        {-1, -1, 257, 257, 257, 257, 257, 257, 257, 257, 257, 257, 257, 257, 257, 257, 257, 257, 257},
        {269, 269, -1, 269, 269, 269, 269, 269, 269, 269, 269, 269, 269, 269, 269, 269, 269, 269, 269},
        {270, 270, 270, 270, 270, 270, 270, 270, 270, 270, 270, 270, 270, 270, 270, 270, 270, 270, 270},
        {272, 272, -1, 272, -1, 272, 272, 272, 272, 272, 272, 272, 272, 272, 272, 272, 272, 272, 272},
        {271, 271, 271, 271, 271, 271, 271, 271, 271, 271, 271, 271, 271, 271, 271, 271, 271, 271, 271},
        {273, 273, 273, 273, 273, 273, 273, 273, 273, 273, 273, 273, 273, 273, 273, 273, 273, 273, 273},
        {274, 274, -1, 274, 274, 274, 274, 274, 274, 274, 274, 274, 274, 274, 274, 274, 274, 274, 274},
        {275, 275, 275, 275, 275, 275, 275, 275, 275, 275, 275, 275, 275, 275, 275, 275, 275, 275, 275},
        {280, 280, 280, 280, 280, 280, -1, 280, 280, 280, 280, 280, 280, 280, 280, 280, 280, 280, 280},
        {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {-1, -1, -1, -1, -1, -1, -1, 258, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1},
        {277, 277, 277, 277, 277, 277, 277, 277, 277, 277, 277, 277, 277, 277, 277, 277, 277, 277, 277},
        {278, 278, 278, 278, 278, 278, 278, 278, 278, 278, 278, 278, 278, 278, 278, 278, 278, 278, 278},
        {279, 279, 279, 279, 279, 279, 279, 279, 279, 279, 279, 279, 279, 279, 279, 279, 279, 279, 279},
        {281, 281, 281, 281, 281, 281, 281, 281, 281, 281, 281, 281, 281, 281, 281, 281, 281, 281, 281},
        {282, 282, 282, 282, 282, 282, 282, 282, 282, 282, 282, 282, 282, 282, 282, 282, 282, 282, 282},
        {283, 283, 283, 283, 283, 283, 283, 283, 283, 283, 283, 283, 283, 283, 283, 283, 283, 283, 283},
        {284, 284, 284, 284, 284, 284, 284, 284, 284, 284, 284, 284, 284, 284, 284, 284, 284, 284, 284},
        {285, 285, 285, 285, 285, 285, 285, 285, 285, 285, 285, 285, 285, 285, 285, 285, 285, 285, 285},
        {286, 286, 286, 286, 286, 286, 286, 286, 286, 286, 286, 286, 286, 286, 286, 286, 286, 286, 286},
        {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}
    };

    public static final Func[][] FUNCION = {
        {Funciones::f1, Funciones::f2, Funciones::f10, Funciones::f10, Funciones::f10, Funciones::f10, Funciones::f10, Funciones::f3, Funciones::f10, Funciones::f10, Funciones::f10, Funciones::f10, Funciones::f10, Funciones::f10, Funciones::f10, Funciones::f4, Funciones::f8, Funciones::f4, Funciones::fe},
        {Funciones::f5, Funciones::f5, Funciones::f7, Funciones::f7, Funciones::f7, Funciones::f7, Funciones::f7, Funciones::f7, Funciones::f7, Funciones::f7, Funciones::f7, Funciones::f7, Funciones::f7, Funciones::f7, Funciones::f7, Funciones::f7, Funciones::f7, Funciones::f7, Funciones::f7},
        {Funciones::fe, Funciones::f6, Funciones::f9, Funciones::f9, Funciones::f9, Funciones::f9, Funciones::f9, Funciones::f9, Funciones::f9, Funciones::f9, Funciones::f9, Funciones::f9, Funciones::f9, Funciones::f9, Funciones::f9, Funciones::f9, Funciones::f9, Funciones::f9, Funciones::f9},
        {Funciones::f10, Funciones::f10, Funciones::f15, Funciones::f10, Funciones::f10, Funciones::f10, Funciones::f10, Funciones::f10, Funciones::f10, Funciones::f10, Funciones::f10, Funciones::f10, Funciones::f10, Funciones::f10, Funciones::f10, Funciones::f10, Funciones::f10, Funciones::f10, Funciones::f10},
        {Funciones::f10, Funciones::f10, Funciones::f10, Funciones::f10, Funciones::f10, Funciones::f10, Funciones::f10, Funciones::f10, Funciones::f10, Funciones::f10, Funciones::f10, Funciones::f10, Funciones::f10, Funciones::f10, Funciones::f10, Funciones::f10, Funciones::f10, Funciones::f10, Funciones::f10},
        {Funciones::f10, Funciones::f10, Funciones::f15, Funciones::f10, Funciones::f15, Funciones::f10, Funciones::f10, Funciones::f10, Funciones::f10, Funciones::f10, Funciones::f10, Funciones::f10, Funciones::f10, Funciones::f10, Funciones::f10, Funciones::f10, Funciones::f10, Funciones::f10, Funciones::f10},
        {Funciones::f10, Funciones::f10, Funciones::f10, Funciones::f10, Funciones::f10, Funciones::f10, Funciones::f10, Funciones::f10, Funciones::f10, Funciones::f10, Funciones::f10, Funciones::f10, Funciones::f10, Funciones::f10, Funciones::f10, Funciones::f10, Funciones::f10, Funciones::f10, Funciones::f10},
        {Funciones::f10, Funciones::f10, Funciones::f10, Funciones::f10, Funciones::f10, Funciones::f10, Funciones::f10, Funciones::f10, Funciones::f10, Funciones::f10, Funciones::f10, Funciones::f10, Funciones::f10, Funciones::f10, Funciones::f10, Funciones::f10, Funciones::f10, Funciones::f10, Funciones::f10},
        {Funciones::f10, Funciones::f10, Funciones::f15, Funciones::f10, Funciones::f10, Funciones::f10, Funciones::f10, Funciones::f10, Funciones::f10, Funciones::f10, Funciones::f10, Funciones::f10, Funciones::f10, Funciones::f10, Funciones::f10, Funciones::f10, Funciones::f10, Funciones::f10, Funciones::f10},
        {Funciones::f10, Funciones::f10, Funciones::f10, Funciones::f10, Funciones::f10, Funciones::f10, Funciones::f10, Funciones::f10, Funciones::f10, Funciones::f10, Funciones::f10, Funciones::f10, Funciones::f10, Funciones::f10, Funciones::f10, Funciones::f10, Funciones::f10, Funciones::f10, Funciones::f10},
        {Funciones::f10, Funciones::f10, Funciones::f10, Funciones::f10, Funciones::f10, Funciones::f10, null, Funciones::f10, Funciones::f10, Funciones::f10, Funciones::f10, Funciones::f10, Funciones::f10, Funciones::f10, Funciones::f10, Funciones::f10, Funciones::f10, Funciones::f10, Funciones::f10},
        repeat(Funciones::f14),
        {Funciones::f14, Funciones::f14, Funciones::f14, Funciones::f14, Funciones::f14, Funciones::f12, Funciones::f14, Funciones::f14, Funciones::f14, Funciones::f14, Funciones::f14, Funciones::f14, Funciones::f14, Funciones::f14, Funciones::f14, Funciones::f14, Funciones::f14, Funciones::f14, Funciones::f14},
        {Funciones::f13, Funciones::f13, Funciones::f13, Funciones::f13, Funciones::f13, Funciones::f13, Funciones::f13, Funciones::f11, Funciones::f13, Funciones::f13, Funciones::f13, Funciones::f13, Funciones::f13, Funciones::f13, Funciones::f13, Funciones::fe, Funciones::fe, Funciones::f13, Funciones::f13},
        repeat(Funciones::f10),
        repeat(Funciones::f10),
        repeat(Funciones::f10),
        repeat(Funciones::f10),
        repeat(Funciones::f10),
        repeat(Funciones::f10),
        repeat(Funciones::f10),
        repeat(Funciones::f10),
        repeat(Funciones::f8),
        {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null}
    };

    private Matrices() {
    }

    public static int fila(String estado) {
        for (int i = 0; i < ESTADOS.length; i++) {
            if (ESTADOS[i].equals(estado)) {
                return i;
            }
        }
        throw new IllegalArgumentException("Estado desconocido: " + estado);
    }

    private static Func[] repeat(Func func) {
        Func[] row = new Func[19];
        for (int i = 0; i < row.length; i++) {
            row[i] = func;
        }
        return row;
    }
}
