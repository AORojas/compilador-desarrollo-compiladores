import java.util.Map;

import lexico.AnalizadorLexico;
import lexico.ContextoLexico;

public class Main {
    public static void main(String[] args) throws Exception {
        String archivo = args.length > 0 ? args[0] : "prueba.txt";

        try (AnalizadorLexico analizador = new AnalizadorLexico(archivo)) {
            int token;
            do {
                token = analizador.yylex();
                ContextoLexico ctx = analizador.getContexto();
                if (token != 276) {
                    System.out.println("Token: " + token + " | Lexema: " + ctx.lexemaActual
                            + " | Línea: " + analizador.getLinea());
                }
            } while (token != 276);

            ContextoLexico ctx = analizador.getContexto();
            for (String error : ctx.errores) {
                System.err.println(error);
            }
            if (!ctx.tablaSimbolos.isEmpty()) {
                System.out.println("Tabla de símbolos:");
                for (Map.Entry<String, String> simbolo : ctx.tablaSimbolos.entrySet()) {
                    System.out.println(simbolo.getKey() + " -> " + simbolo.getValue());
                }
            }
        }
    }
}
