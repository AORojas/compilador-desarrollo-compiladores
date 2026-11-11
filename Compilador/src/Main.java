import java.util.Collections;
import java.util.List;

import lexico.AnalizadorLexico;
import lexico.ContextoLexico;
import sintactico.ErrorSemantico;
import sintactico.LexerCupAdapter;
import sintactico.Parser;

public class Main {
    public static void main(String[] args) throws Exception {
        String archivo = args.length > 0 ? args[0] : "prueba-sintactico.txt";
        int erroresSintacticos = 0;
        List<ErrorSemantico> erroresSemanticos = Collections.emptyList();
        ContextoLexico contexto = null;
        Parser parser = null;

        try (AnalizadorLexico analizador = new AnalizadorLexico(archivo)) {
            contexto = analizador.getContexto();
            parser = new Parser(new LexerCupAdapter(analizador));
            try {
                parser.parse();
            } catch (Parser.ErrorSintacticoNoRecuperable error) {
                System.err.println(error.getMessage());
            }
            erroresSintacticos = parser.getErroresSintacticos();
            erroresSemanticos = parser.getErroresSemanticos();
        }

        if (contexto != null) {
            for (String error : contexto.errores) {
                System.err.println(error);
            }
        }

        for (ErrorSemantico error : erroresSemanticos) {
            System.err.println(error);
        }

        if (parser != null) {
            parser.getTablaSimbolos().imprimir(System.out);
        }

        boolean erroresLexicos = contexto != null && !contexto.errores.isEmpty();
        boolean errores = erroresSintacticos > 0 || erroresLexicos || !erroresSemanticos.isEmpty();
        if (parser != null
                && (parser.getPolaca().tieneSaltosPendientes()
                    || parser.getPolaca().tieneReferenciasPendientes())) {
            if (!errores) {
                System.err.println("Error interno: quedaron destinos o referencias sin resolver.");
            }
            errores = true;
        }

        if (errores) {
            System.err.println("Análisis finalizado con errores.");
            System.exit(1);
        }

        System.out.println("Análisis sintáctico finalizado correctamente.");
        System.out.println("Polaca Inversa:");
        System.out.println(parser.getPolaca().print());
    }
}
