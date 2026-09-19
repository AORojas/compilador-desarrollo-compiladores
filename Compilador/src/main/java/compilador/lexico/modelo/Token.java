package compilador.lexico.modelo;

public final class Token {
    private final int codigo;
    private final String nombre;
    private final String lexema;
    private final int linea;

    public Token(int codigo, String lexema, int linea) {
        this.codigo = codigo;
        this.nombre = nombreDe(codigo);
        this.lexema = lexema;
        this.linea = linea;
    }

    public int getCodigo() { return codigo; }
    public String getNombre() { return nombre; }
    public String getLexema() { return lexema; }
    public int getLinea() { return linea; }

    public static String nombreDe(int codigo) {
        return switch (codigo) {
            case TipoToken.ID -> "ID";
            case TipoToken.CTE -> "CTE";
            case TipoToken.LITERAL_TXT -> "LITERAL_TXT";
            case TipoToken.PRINCIPAL -> "PRINCIPAL";
            case TipoToken.ENTERO -> "ENTERO";
            case TipoToken.SI -> "SI";
            case TipoToken.BUCLE -> "BUCLE";
            case TipoToken.HASTA -> "HASTA";
            case TipoToken.MOSTRAR -> "MOSTRAR";
            case TipoToken.MOSTRAR_TXT -> "MOSTRAR_TXT";
            case TipoToken.Y -> "Y";
            case TipoToken.O -> "O";
            case TipoToken.RETORNAR -> "RETORNAR";
            case TipoToken.ASIG -> "ASIG";
            case TipoToken.IGUAL -> "IGUAL";
            case TipoToken.DISTINTO -> "DISTINTO";
            case TipoToken.MENOR -> "MENOR";
            case TipoToken.MENOR_IGUAL -> "MENOR_IGUAL";
            case TipoToken.MAYOR -> "MAYOR";
            case TipoToken.MAYOR_IGUAL -> "MAYOR_IGUAL";
            case TipoToken.EOF -> "EOF";
            case TipoToken.SUMA -> "SUMA";
            case TipoToken.RESTA -> "RESTA";
            case TipoToken.MULTIPLICACION -> "MULTIPLICACION";
            case TipoToken.DIVISION -> "DIVISION";
            case TipoToken.PAREN_IZQ -> "PAREN_IZQ";
            case TipoToken.PAREN_DER -> "PAREN_DER";
            case TipoToken.LLAVE_IZQ -> "LLAVE_IZQ";
            case TipoToken.LLAVE_DER -> "LLAVE_DER";
            case TipoToken.COMA -> "COMA";
            case TipoToken.FIN_DE_LINEA -> "FIN_DE_LINEA";
            default -> "DESCONOCIDO";
        };
    }

    @Override
    public String toString() {
        return "Token{" +
                "codigo=" + codigo +
                ", nombre='" + nombre + '\'' +
                ", lexema='" + lexema.replace("\n", "\\n").replace("\r", "\\r") + '\'' +
                ", linea=" + linea +
                '}';
    }
}
