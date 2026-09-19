package compilador.lexico.datos;

public final class MatrizTransicion {
    private MatrizTransicion() {}

    public static final int EF = -1;

    // Estados: E0..E22 = 0..22. EF = -1.
    public static final int[][] MATRIZ = {
        {1,2,3,5,8,10,16,13,14,15,17,18,19,20,21,22,0,EF},
        {1,1,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF},
        {EF,2,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF},
        {EF,EF,4,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF},
        {EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF},
        {EF,EF,6,EF,7,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF},
        {EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF},
        {EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF},
        {EF,EF,9,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF},
        {EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF},
        {EF,EF,EF,EF,EF,11,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF},
        {11,11,11,11,11,11,12,11,11,11,11,11,11,11,11,11,11,11},
        {11,11,11,11,11,0,11,11,11,11,11,11,11,11,11,11,11,11},
        {13,13,13,13,13,13,13,EF,13,13,13,13,13,13,13,13,13,13},
        {EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF},
        {EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF},
        {EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF},
        {EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF},
        {EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF},
        {EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF},
        {EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF},
        {EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF},
        {EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF}
    };

    public static int siguiente(int estado, int evento) {
        return MATRIZ[estado][evento];
    }
}
