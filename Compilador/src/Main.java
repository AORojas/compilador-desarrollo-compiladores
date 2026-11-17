import lexico.AnalizadorLexico;
import lexico.ContextoLexico;

public class Main {
    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("Uso: java -cp out Main <archivo-fuente>");
            System.exit(2);
            return;
        }

        boolean erroresLexicos;
        try (AnalizadorLexico analizador = new AnalizadorLexico(args[0])) {
            int token;
            do {
                token = analizador.yylex();
                if (token == -1) {
                    continue;
                }
                if (token == 276) {
                    break;
                }

                ContextoLexico contexto = analizador.getContexto();
                String lexema = contexto.lexemaActual
                    .replace("\\", "\\\\")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");
                System.out.println(
                    "Token: " + token
                        + " | Lexema: " + lexema
                        + " | Línea: " + analizador.getLineaToken()
                        + " | Columna: " + analizador.getColumnaToken());
            } while (true);

            ContextoLexico contexto = analizador.getContexto();
            erroresLexicos = !contexto.errores.isEmpty();
            for (String error : contexto.errores) {
                System.err.println(error);
            }
        }

        if (erroresLexicos) {
            System.err.println("Análisis léxico finalizado con errores.");
            System.exit(1);
        }

        System.out.println("Análisis léxico finalizado correctamente.");
    }
}
