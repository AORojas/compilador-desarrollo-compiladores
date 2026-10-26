package sintactico;

import java.util.ArrayList;
import java.util.List;

public class PolacaInversa {
    private final List<String> elementos = new ArrayList<String>();

    public void add(String valor) {
        elementos.add(valor);
    }

    public void addOperador(int token) {
        elementos.add(mapearToken(token));
    }

    public String print() {
        if (elementos.isEmpty()) {
            return "";
        }
        return String.join(" ", elementos);
    }

    public List<String> getElementos() {
        return new ArrayList<String>(elementos);
    }

    private String mapearToken(int token) {
        switch (token) {
            case 270: return "==";
            case 271: return "<>";
            case 272: return "<";
            case 273: return "<=";
            case 274: return ">";
            case 275: return ">=";
            case 277: return "+";
            case 278: return "-";
            case 279: return "*";
            case 280: return "/";
            case 266: return "AND";
            case 267: return "OR";
            case 268: return "RET";
            case 269: return ":=";
            default: return String.valueOf(token);
        }
    }
}
