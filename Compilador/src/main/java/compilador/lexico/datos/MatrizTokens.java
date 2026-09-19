package compilador.lexico.datos;

import compilador.lexico.modelo.TipoToken;

public final class MatrizTokens {
    private MatrizTokens() {}

    public static int tokenDirecto(int estado, int evento) {
        return switch (estado) {
            case 3 -> evento == Clasificador.IGUAL ? TipoToken.IGUAL : TipoToken.ASIG;
            case 5 -> evento == Clasificador.IGUAL ? TipoToken.MENOR_IGUAL
                    : evento == Clasificador.MAYOR ? TipoToken.DISTINTO : TipoToken.MENOR;
            case 8 -> evento == Clasificador.IGUAL ? TipoToken.MAYOR_IGUAL : TipoToken.MAYOR;
            case 14 -> TipoToken.SUMA;
            case 15 -> TipoToken.RESTA;
            case 16 -> TipoToken.MULTIPLICACION;
            case 17 -> TipoToken.PAREN_IZQ;
            case 18 -> TipoToken.PAREN_DER;
            case 19 -> TipoToken.LLAVE_IZQ;
            case 20 -> TipoToken.LLAVE_DER;
            case 21 -> TipoToken.COMA;
            case 22 -> TipoToken.FIN_DE_LINEA;
            default -> -1;
        };
    }
}
