import java.io.File;
import java.util.Map;

import lexico.AnalizadorLexico;
import lexico.ContextoLexico;
import sintactico.Parser;

public class Main {
    public static void main(String[] args) throws Exception {
        String archivo = seleccionarArchivo(args);

        try (AnalizadorLexico analizador = new AnalizadorLexico(archivo)) {
            Parser parser = new Parser(analizador);
            parser.parse();

            System.out.println("Análisis sintáctico finalizado correctamente.");
            System.out.println("Polaca Inversa:");
            System.out.println(parser.getPolaca().print());

            ContextoLexico ctx = analizador.getContexto();
            if (!ctx.errores.isEmpty()) {
                System.err.println("Errores léxicos:");
                for (String error : ctx.errores) {
                    System.err.println(error);
                }
            }

            if (!ctx.tablaSimbolos.isEmpty()) {
                System.out.println("Tabla de símbolos:");
                for (Map.Entry<String, String> simbolo : ctx.tablaSimbolos.entrySet()) {
                    System.out.println(simbolo.getKey() + " -> " + simbolo.getValue());
                }
            }
        }
    }

    private static String seleccionarArchivo(String[] args) {
        if (args.length > 0) {
            return args[0];
        }

        String[] candidatos = {
            "prueba-sintactico.txt",
            "Compilador/prueba-sintactico.txt",
            "../Compilador/prueba-sintactico.txt"
        };

        for (String candidato : candidatos) {
            File archivo = new File(candidato);
            if (archivo.exists()) {
                return candidato;
            }
        }

        return "prueba-sintactico.txt";
    }
}
