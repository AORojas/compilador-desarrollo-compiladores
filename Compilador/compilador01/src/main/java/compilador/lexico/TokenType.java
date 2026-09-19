package compilador.lexico;

public final class TokenType {
    public static final int ID = 256;
    public static final int CTE = 257;
    public static final int LITERAL_TXT = 258;
    public static final int PRINCIPAL = 259;
    public static final int ENTERO = 260;
    public static final int SI = 261;
    public static final int BUCLE = 262;
    public static final int HASTA = 263;
    public static final int MOSTRAR = 264;
    public static final int MOSTRAR_TXT = 265;
    public static final int Y = 266;
    public static final int O = 267;
    public static final int RETORNAR = 268;
    public static final int ASIG = 269;
    public static final int IGUAL = 270;
    public static final int DISTINTO = 271;
    public static final int MENOR = 272;
    public static final int MENOR_O_IGUAL = 273;
    public static final int MAYOR = 274;
    public static final int MAYOR_O_IGUAL = 275;
    public static final int EOF = 276;
    public static final int SUMA = 277;
    public static final int RESTA = 278;
    public static final int MULTIPLICACION = 279;
    public static final int DIVISION = 280;
    public static final int PAREN_IZQ = 281;
    public static final int PAREN_DER = 282;
    public static final int LLAVE_IZQ = 283;
    public static final int LLAVE_DER = 284;
    public static final int COMA = 285;
    public static final int FIN_DE_LINEA = 286;

    private TokenType() {
    }

    public static String nombre(int token) {
        return switch (token) {
            case ID -> "ID";
            case CTE -> "CTE";
            case LITERAL_TXT -> "LITERAL_TXT";
            case PRINCIPAL -> "PRINCIPAL";
            case ENTERO -> "ENTERO";
            case SI -> "SI";
            case BUCLE -> "BUCLE";
            case HASTA -> "HASTA";
            case MOSTRAR -> "MOSTRAR";
            case MOSTRAR_TXT -> "MOSTRAR_TXT";
            case Y -> "Y";
            case O -> "O";
            case RETORNAR -> "RETORNAR";
            case ASIG -> "ASIG";
            case IGUAL -> "IGUAL";
            case DISTINTO -> "DISTINTO";
            case MENOR -> "MENOR";
            case MENOR_O_IGUAL -> "MENOR_O_IGUAL";
            case MAYOR -> "MAYOR";
            case MAYOR_O_IGUAL -> "MAYOR_O_IGUAL";
            case EOF -> "EOF";
            case SUMA -> "SUMA";
            case RESTA -> "RESTA";
            case MULTIPLICACION -> "MULTIPLICACION";
            case DIVISION -> "DIVISION";
            case PAREN_IZQ -> "PAREN_IZQ";
            case PAREN_DER -> "PAREN_DER";
            case LLAVE_IZQ -> "LLAVE_IZQ";
            case LLAVE_DER -> "LLAVE_DER";
            case COMA -> "COMA";
            case FIN_DE_LINEA -> "FIN_DE_LINEA";
            default -> "DESCONOCIDO";
        };
    }
}
