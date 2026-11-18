import ast.ImpresorAST;
import ast.NodoAST;
import gc.GeneradorCodigoIntermedio;
import java_cup.runtime.DefaultSymbolFactory;
import lexico.AnalizadorLexico;
import lexico.ContextoLexico;
import java_cup.runtime.Symbol;
import semantico.AnalizadorSemantico;
import sintactico.LexerCupAdapter;
import sintactico.Parser;

public class Main {
    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("Uso: java -cp \"out;cup.jar\" Main <archivo-fuente>");
            System.exit(2);
            return;
        }

        boolean errores;
        try (AnalizadorLexico analizador = new AnalizadorLexico(args[0])) {
            Parser parser = crearParser(analizador);
            Symbol resultado = null;
            try {
                resultado = parser.parse();
            } catch (Parser.ErrorFatal error) {
                System.err.println("El análisis sintáctico se detuvo sin poder recuperarse.");
            }

            NodoAST raiz = resultado != null && resultado.value instanceof NodoAST
                ? (NodoAST) resultado.value
                : null;
            if (raiz != null) {
                ImpresorAST.imprimir(raiz);
            }

            ContextoLexico contexto = analizador.getContexto();
            for (String error : contexto.errores) {
                System.err.println(error);
            }
            boolean erroresLexicosOSintacticos =
                !contexto.errores.isEmpty() || parser.getErroresSintacticos() > 0;

            if (raiz == null && !erroresLexicosOSintacticos) {
                throw new IllegalStateException("El parser terminó sin devolver la raíz del AST.");
            }

            boolean erroresSemanticos = false;
            if (raiz != null) {
                AnalizadorSemantico semantico = new AnalizadorSemantico();
                semantico.analizar(raiz);
                erroresSemanticos = !semantico.getErrores().isEmpty();
                if (erroresSemanticos) {
                    System.err.println("Errores semánticos:");
                    for (String error : semantico.getErrores()) {
                        System.err.println(error);
                    }
                } else {
                    System.out.println("Sin errores semánticos.");
                }
                System.out.println(semantico.getTablaSimbolos().formatear());

                if (!erroresLexicosOSintacticos && !erroresSemanticos) {
                    GeneradorCodigoIntermedio generador =
                        new GeneradorCodigoIntermedio(semantico.getTablaSimbolos());
                    generador.generar(raiz);
                    System.out.println("Polaca Inversa:");
                    System.out.println(generador.getPolaca().formatear());
                }
            }
            errores = erroresLexicosOSintacticos || erroresSemanticos;
        }

        if (errores) {
            System.err.println("Análisis del compilador finalizado con errores.");
            System.exit(1);
        }

        System.out.println("Análisis del compilador finalizado correctamente.");
    }

    @SuppressWarnings("deprecation")
    private static Parser crearParser(AnalizadorLexico analizador) {
        return new Parser(new LexerCupAdapter(analizador), new DefaultSymbolFactory());
    }
}
