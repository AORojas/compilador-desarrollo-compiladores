import java.lang.reflect.*;
import lexico.AnalizadorLexico;
import sintactico.Parser;

public class ParserInspect {
    public static void main(String[] args) throws Exception {
        try (AnalizadorLexico a = new AnalizadorLexico("prueba-sintactico.txt")) {
            Parser p = new Parser(a);
            callInt(p, "consumir", 260);
            callInt(p, "consumir", 259);
            callInt(p, "consumir", 281);
            callInt(p, "consumir", 282);
            callInt(p, "consumir", 283);
            System.out.println("before declaracion: " + getField(p, "tokenActual") + " | " + getField(p, "lexemaActual"));
            callNoArg(p, "declaracion");
            System.out.println("after declaracion: " + getField(p, "tokenActual") + " | " + getField(p, "lexemaActual"));
        }
    }

    static Object getField(Parser p, String name) throws Exception {
        Field f = Parser.class.getDeclaredField(name);
        f.setAccessible(true);
        return f.get(p);
    }

    static void callInt(Parser p, String name, int v) throws Exception {
        Method m = Parser.class.getDeclaredMethod(name, int.class);
        m.setAccessible(true);
        m.invoke(p, v);
    }

    static void callNoArg(Parser p, String name) throws Exception {
        Method m = Parser.class.getDeclaredMethod(name);
        m.setAccessible(true);
        m.invoke(p);
    }
}
