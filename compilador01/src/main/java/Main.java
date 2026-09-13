import java.io.StringReader;

import compilador.lexico.Lexico;
import compilador.lexico.Token;

public class Main {
    public static void main(String[] args) {
        String fuente = "" +
                "entero principal()\n" +
                "{\n" +
                "    entero numero\n" +
                "    numero = 10\n" +
                "    si (numero > 0)\n" +
                "    {\n" +
                "        mostrar(numero)\n" +
                "    }\n" +
                "}\n";

        try {
            Lexico lexico = new Lexico(new StringReader(fuente));
            System.out.println("=== Tokens GAUCHO ===");

            Token token;
            do {
                token = lexico.yylex();
                System.out.println(token);
            } while (token.getCodigo() != compilador.lexico.TokenType.EOF);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}