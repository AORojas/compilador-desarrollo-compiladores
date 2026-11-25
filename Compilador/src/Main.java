import ast.ImpresorAST;
import ast.NodoAST;
import gc.GeneradorCodigoIntermedio;
import generadorasm.GeneradorAsm;
import generadorasm.ObtencionTsPi;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
                System.out.println("Tabla de símbolos:");
                String tablaSimbolos = semantico.getTablaSimbolos().formatear();
                System.out.println(tablaSimbolos);

                if (!erroresLexicosOSintacticos && !erroresSemanticos) {
                    GeneradorCodigoIntermedio generador =
                        new GeneradorCodigoIntermedio(semantico.getTablaSimbolos());
                    generador.generar(raiz);
                    String polacaInversa = generador.getPolaca().formatear();
                    System.out.println("Polaca Inversa:");
                    System.out.println(polacaInversa);
                    Path reporte = escribirReporte(args[0], tablaSimbolos, polacaInversa);
                    generarAssemblerYEjecutable(reporte);
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

    private static Path escribirReporte(
            String archivoFuente, String tablaSimbolos, String polaca)
            throws IOException {
        Path rutaReporte = cambiarExtension(Paths.get(archivoFuente), ".e");
        String separador = System.lineSeparator();
        String contenido = "Tabla de símbolos:" + separador
            + tablaSimbolos + separador
            + "Polaca Inversa:" + separador
            + polaca + separador;
        Files.write(rutaReporte, contenido.getBytes(StandardCharsets.UTF_8));
        System.out.println("Reporte generado: " + rutaReporte);
        return rutaReporte;
    }

    private static void generarAssemblerYEjecutable(Path reporte) throws Exception {
        Path assembler = cambiarExtension(reporte, ".s");
        Path ejecutable = cambiarExtension(reporte, ".exe");
        ObtencionTsPi.Programa programa = ObtencionTsPi.leer(reporte.toString());
        new GeneradorAsm(programa).escribir(assembler.toString());
        System.out.println("Assembler generado: " + assembler);

        Process proceso;
        try {
            proceso = new ProcessBuilder(
                "gcc",
                "-no-pie",
                "-Wl,--image-base=0x400000",
                assembler.toString(),
                "-o",
                ejecutable.toString())
                .inheritIO()
                .start();
        } catch (IOException error) {
            throw new IOException(
                "No se pudo iniciar GCC. Verificá que GCC esté instalado y disponible en PATH.",
                error);
        }

        int codigoSalida = proceso.waitFor();
        if (codigoSalida != 0) {
            throw new IOException(
                "GCC terminó con código " + codigoSalida
                    + "; no se pudo generar el ejecutable " + ejecutable + ".");
        }
        System.out.println("Ejecutable generado: " + ejecutable);
    }

    private static Path cambiarExtension(Path archivo, String nuevaExtension) {
        Path nombreArchivo = archivo.getFileName();
        String nombre = nombreArchivo.toString();
        int extension = nombre.lastIndexOf('.');
        String base = extension > 0 ? nombre.substring(0, extension) : nombre;
        Path nuevoNombre = Paths.get(base + nuevaExtension);
        Path directorio = archivo.getParent();
        return directorio == null ? nuevoNombre : directorio.resolve(nuevoNombre);
    }
}
