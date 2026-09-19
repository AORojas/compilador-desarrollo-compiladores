package compilador.lexico;

public class Token {
    private final int codigo;
    private final String lexema;
    private final int linea;

    public Token(int codigo, String lexema, int linea) {
        this.codigo = codigo;
        this.lexema = lexema;
        this.linea = linea;
    }

    public int getCodigo() {
        return codigo;
    }

    public String getLexema() {
        return lexema;
    }

    public int getLinea() {
        return linea;
    }

    @Override
    public String toString() {
        return "Token{" +
                "codigo=" + codigo +
                ", nombre='" + TokenType.nombre(codigo) + '\'' +
                ", lexema='" + lexema + '\'' +
                ", linea=" + linea +
                '}';
    }
}
