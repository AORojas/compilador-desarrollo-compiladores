import lexico.AnalizadorLexico;
public class TokenDump {
    public static void main(String[] args) throws Exception {
        try (AnalizadorLexico a = new AnalizadorLexico("prueba-sintactico.txt")) {
            int t;
            while ((t = a.yylex()) != 276) {
                System.out.println(t + " | " + a.getContexto().lexemaActual);
            }
        }
    }
}
