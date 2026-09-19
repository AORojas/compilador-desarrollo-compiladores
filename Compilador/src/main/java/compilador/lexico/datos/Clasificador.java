package compilador.lexico.datos;

public final class Clasificador {
    private Clasificador() {}

    public static final int LETRA = 0;
    public static final int DIGITO = 1;
    public static final int IGUAL = 2;
    public static final int MENOR = 3;
    public static final int MAYOR = 4;
    public static final int BARRA = 5;
    public static final int ASTERISCO = 6;
    public static final int COMILLA = 7;
    public static final int MAS = 8;
    public static final int MENOS = 9;
    public static final int PAREN_IZQ = 10;
    public static final int PAREN_DER = 11;
    public static final int LLAVE_IZQ = 12;
    public static final int LLAVE_DER = 13;
    public static final int COMA = 14;
    public static final int SALTO_LINEA = 15;
    public static final int BLANCO = 16;
    public static final int OTRO = 17;

    public static int obtenerColumna(int c) {
        if (Character.isLetter(c) && c < 128) return LETRA;
        if (Character.isDigit(c)) return DIGITO;
        return switch (c) {
            case '=' -> IGUAL;
            case '<' -> MENOR;
            case '>' -> MAYOR;
            case '/' -> BARRA;
            case '*' -> ASTERISCO;
            case '"' -> COMILLA;
            case '+' -> MAS;
            case '-' -> MENOS;
            case '(' -> PAREN_IZQ;
            case ')' -> PAREN_DER;
            case '{' -> LLAVE_IZQ;
            case '}' -> LLAVE_DER;
            case ',' -> COMA;
            case '\n' -> SALTO_LINEA;
            case ' ', '\t', '\r' -> BLANCO;
            default -> OTRO;
        };
    }
}
