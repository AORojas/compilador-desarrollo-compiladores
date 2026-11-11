import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lexico.AnalizadorLexico;
import sintactico.ErrorSemantico;
import sintactico.LexerCupAdapter;
import sintactico.Parser;

public final class FrontendRegressionTest {
    private static final Pattern POLACA_ITEM = Pattern.compile("\\[[^\\]]*\\]|->\\d+|->\\?");
    private static int pruebas;

    private FrontendRegressionTest() {
    }

    public static void main(String[] args) throws Exception {
        pruebaTokensAdyacentesYEOF();
        pruebaPrecedenciaAritmetica();
        pruebaCortocircuitoAND();
        pruebaCortocircuitoOR();
        pruebaCondicionesAgrupadasYMixtas();
        pruebaCortocircuitoHasta();
        pruebaAmbitos();
        pruebaErroresSemanticos();
        pruebaLlamadasAFunciones();
        pruebaAridadDeLlamadas();
        pruebaRecuperacionSintactica();
        System.out.println("OK: " + pruebas + " pruebas del frontend.");
    }

    private static void pruebaTokensAdyacentesYEOF() throws Exception {
        verificarTokenFinal("abc", 256, "abc");
        verificarTokenFinal("123", 257, "123");
        verificarTokenFinal("+", 277, "");
        verificarTokenFinal(")", 282, "");

        withLexer("((\n", new LexerTest() {
            @Override
            public void run(AnalizadorLexico lexer) throws Exception {
                verificar(lexer.yylex() == 281, "Primer paréntesis adyacente");
                verificar(lexer.yylex() == 281, "Segundo paréntesis adyacente");
                verificar(lexer.yylex() == 286, "Salto de línea después de paréntesis");
                verificar(lexer.yylex() == 276, "EOF después de paréntesis");
            }
        });
    }

    private static void verificarTokenFinal(String fuente, int esperado, String lexema) throws Exception {
        withLexer(fuente, new LexerTest() {
            @Override
            public void run(AnalizadorLexico lexer) throws Exception {
                int token = lexer.yylex();
                verificar(token == esperado, "Token final esperado " + esperado + ", obtenido " + token);
                if (!lexema.isEmpty()) {
                    verificar(lexema.equals(lexer.getContexto().lexemaActual), "Lexema final " + lexema);
                }
                verificar(lexer.yylex() == 276, "EOF después del token final " + esperado);
            }
        });
    }

    private static void pruebaPrecedenciaAritmetica() throws Exception {
        Result resultado = analizar(programaPrincipal(
            "entero a, b, c\n"
                + "a = 1\n"
                + "b = 2\n"
                + "c = a + b * 2\n"
                + "mostrar(c)\n"));
        verificarValido(resultado, "Precedencia aritmética");
        List<String> instrucciones = instrucciones(resultado.polaca);
        verificar(indice(instrucciones, "[*]") < indice(instrucciones, "[+]"),
            "La multiplicación debe emitirse antes que la suma");
    }

    private static void pruebaCortocircuitoAND() throws Exception {
        Result resultado = analizar(programaConSi("a > 0 y b > 0"));
        verificarValido(resultado, "AND");
        verificarRamasIf(resultado.polaca, true);
    }

    private static void pruebaCortocircuitoOR() throws Exception {
        Result resultado = analizar(programaConSi("a > 0 o b > 0"));
        verificarValido(resultado, "OR");
        verificarRamasIf(resultado.polaca, false);
    }

    private static void pruebaCondicionesAgrupadasYMixtas() throws Exception {
        String[] condiciones = {
            "(a > 0) y (b > 0)",
            "(a > 0) o (b > 0)",
            "((a > 0) y (b > 0)) o (a == b)",
            "(a > 0) o ((b > 0) y (a == b))"
        };
        for (String condicion : condiciones) {
            Result resultado = analizar(programaConSi(condicion));
            verificarValido(resultado, "Condición agrupada: " + condicion);
            verificarDestinosResueltos(resultado.polaca);
            verificar(cantidadComparadores(instrucciones(resultado.polaca)) >= 2,
                "Comparadores de condición agrupada: " + condicion);
        }
    }

    private static void pruebaCortocircuitoHasta() throws Exception {
        String fuente = "entero principal()\n"
            + "{\n"
            + "entero a, b\n"
            + "bucle\n"
            + "{\n"
            + "mostrar(a)\n"
            + "} hasta ((a > 0) y (b > 0))\n"
            + "}";
        Result resultado = analizar(fuente);
        verificarValido(resultado, "Cortocircuito de bucle hasta");
        List<String> instrucciones = instrucciones(resultado.polaca);
        List<Integer> comparadores = indicesComparadores(instrucciones);
        verificar(comparadores.size() == 2, "Dos comparadores en la condición del bucle");

        int inicioDerecha = comparadores.get(0) + 5;
        int salida = instrucciones.size();
        verificar(destino(instrucciones, comparadores.get(0) + 1) == 0,
            "Rama falsa del primer operando vuelve al cuerpo del bucle");
        verificar(destino(instrucciones, comparadores.get(0) + 3) == inicioDerecha,
            "Rama verdadera del primer operando continúa al segundo");
        verificar(destino(instrucciones, comparadores.get(1) + 1) == 0,
            "Rama falsa del segundo operando vuelve al cuerpo del bucle");
        verificar(destino(instrucciones, comparadores.get(1) + 3) == salida,
            "Condición verdadera del bucle sale del ciclo");
    }

    private static void pruebaAmbitos() throws Exception {
        Result resultado = analizar(programaPrincipal(
            "entero x\n"
                + "x = 1\n"
                + "si (x > 0)\n"
                + "{\n"
                + "entero x\n"
                + "x = 2\n"
                + "}\n"
                + "mostrar(x)\n"));
        verificarValido(resultado, "Ocultamiento local y visibilidad exterior");
        List<ErrorSemantico> errores = resultado.erroresSemanticos;
        verificar(errores.isEmpty(), "Sin errores de ámbito al ocultar una variable");
    }

    private static void pruebaErroresSemanticos() throws Exception {
        Result noDeclarada = analizar(programaPrincipal("x = 1\n"));
        verificarTieneError(noDeclarada, "E4");

        Result duplicada = analizar(programaPrincipal("entero x, x\n"));
        verificarTieneError(duplicada, "R2");

        Result anidamientoTres = analizar(programaPrincipal(
            "entero x\n"
                + "si (x > 0)\n{\n"
                + "si (x > 0)\n{\n"
                + "bucle\n{\n"
                + "mostrar(x)\n"
                + "} hasta (x > 0)\n"
                + "}\n}\n"));
        verificarValido(anidamientoTres, "Tres niveles de control");

        Result anidamientoCuatro = analizar(programaPrincipal(
            "entero x\n"
                + "si (x > 0)\n{\n"
                + "si (x > 0)\n{\n"
                + "bucle\n{\n"
                + "si (x > 0)\n{\n"
                + "mostrar(x)\n"
                + "}\n"
                + "} hasta (x > 0)\n"
                + "}\n}\n"));
        verificarTieneError(anidamientoCuatro, "E5");

        Result parametroDuplicado = analizar(
            "entero f(entero x)\n{\n"
                + "entero x\n"
                + "retornar x\n"
                + "}\n"
                + "entero principal()\n{\n"
                + "retornar 0\n"
                + "}");
        verificarTieneError(parametroDuplicado, "R2");
    }

    private static void pruebaLlamadasAFunciones() throws Exception {
        Result llamadaAdelantada = analizar(
            "entero principal()\n{\n"
                + "entero r\n"
                + "r = duplicar(5)\n"
                + "retornar r\n"
                + "}\n"
                + "entero duplicar(entero n)\n{\n"
                + "retornar n * 2\n"
                + "}");
        verificarValido(llamadaAdelantada, "Llamada adelantada");
        verificar(llamadaAdelantada.polaca.contains("[CALL]"), "Emite CALL");
        verificar(llamadaAdelantada.polaca.contains("[RET]"), "Emite RET");
        verificarDestinosResueltos(llamadaAdelantada.polaca);

        Result recursion = analizar(
            "entero contar(entero n)\n{\n"
                + "retornar contar(n)\n"
                + "}\n"
                + "entero principal()\n{\n"
                + "retornar contar(1)\n"
                + "}");
        verificarValido(recursion, "Llamada recursiva");
        verificar(ocurrencias(recursion.polaca, "[CALL]") == 2, "Dos llamadas, incluyendo recursión");

        Result desconocida = analizar(programaPrincipal("entero r\nr = falta(1)\n"));
        verificarTieneError(desconocida, "E6");
    }

    private static void pruebaAridadDeLlamadas() throws Exception {
        String funcion = "entero f(entero n)\n{\nretornar n\n}\n";
        Result sinArgumento = analizar(
            funcion + "entero principal()\n{\nretornar f()\n}");
        verificarTieneError(sinArgumento, "E6");

        Result conDosArgumentos = analizar(
            funcion + "entero principal()\n{\nretornar f(1, 2)\n}");
        verificarTieneError(conDosArgumentos, "E6");
    }

    private static void pruebaRecuperacionSintactica() throws Exception {
        Result resultado = analizar(programaPrincipal(
            "entero a, b\n"
                + "a =\n"
                + "b =\n"
                + "mostrar(a)\n"));
        verificar(resultado.erroresSintacticos >= 2, "Reporta varios errores sintácticos");
        verificar(resultado.erroresLexicos.isEmpty(), "Sin errores léxicos en la prueba de recuperación");
        verificarDestinosResueltos(resultado.polaca);
    }

    private static void verificarRamasIf(String polaca, boolean and) {
        List<String> instrucciones = instrucciones(polaca);
        List<Integer> comparadores = indicesComparadores(instrucciones);
        verificar(comparadores.size() == 2, "Dos comparadores en condición lógica");

        int izquierda = comparadores.get(0);
        int derecha = comparadores.get(1);
        int inicioDerecha = izquierda + 5;
        int inicioCuerpo = derecha + 5;
        int salida = instrucciones.size();

        if (and) {
            verificar(destino(instrucciones, izquierda + 1) == salida,
                "AND falso omite el operando derecho y el cuerpo");
            verificar(destino(instrucciones, izquierda + 3) == inicioDerecha,
                "AND verdadero continúa con el operando derecho");
        } else {
            verificar(destino(instrucciones, izquierda + 1) == inicioDerecha,
                "OR falso continúa con el operando derecho");
            verificar(destino(instrucciones, izquierda + 3) == inicioCuerpo,
                "OR verdadero omite el operando derecho y entra al cuerpo");
        }
        verificar(destino(instrucciones, derecha + 1) == salida,
            "Rama falsa del último operando sale del si");
        verificar(destino(instrucciones, derecha + 3) == inicioCuerpo,
            "Rama verdadera del último operando entra al cuerpo");
    }

    private static String programaConSi(String condicion) {
        return programaPrincipal(
            "entero a, b\n"
                + "a = 1\n"
                + "b = 2\n"
                + "si (" + condicion + ")\n"
                + "{\n"
                + "mostrar(a)\n"
                + "}\n");
    }

    private static String programaPrincipal(String sentencias) {
        return "entero principal()\n{\n" + sentencias + "}";
    }

    private static Result analizar(String fuente) throws Exception {
        Path archivo = Files.createTempFile("gaucho-frontend-test-", ".txt");
        try {
            Files.write(archivo, fuente.getBytes(StandardCharsets.UTF_8));
            try (AnalizadorLexico lexer = new AnalizadorLexico(archivo.toString())) {
                Parser parser = new Parser(new LexerCupAdapter(lexer));
                try {
                    parser.parse();
                } catch (Parser.ErrorSintacticoNoRecuperable esperado) {
                }
                return new Result(
                    parser.getErroresSintacticos(),
                    new ArrayList<String>(lexer.getContexto().errores),
                    parser.getErroresSemanticos(),
                    parser.getPolaca().print(),
                    parser.getPolaca().tieneSaltosPendientes(),
                    parser.getPolaca().tieneReferenciasPendientes());
            }
        } finally {
            Files.deleteIfExists(archivo);
        }
    }

    private static void withLexer(String fuente, LexerTest prueba) throws Exception {
        Path archivo = Files.createTempFile("gaucho-lexer-test-", ".txt");
        try {
            Files.write(archivo, fuente.getBytes(StandardCharsets.UTF_8));
            try (AnalizadorLexico lexer = new AnalizadorLexico(archivo.toString())) {
                prueba.run(lexer);
            }
        } finally {
            Files.deleteIfExists(archivo);
        }
    }

    private static void verificarValido(Result resultado, String nombre) {
        verificar(resultado.erroresSintacticos == 0, nombre + ": errores sintácticos");
        verificar(resultado.erroresLexicos.isEmpty(), nombre + ": errores léxicos " + resultado.erroresLexicos);
        verificar(resultado.erroresSemanticos.isEmpty(), nombre + ": errores semánticos " + resultado.erroresSemanticos);
        verificar(!resultado.saltosPendientes, nombre + ": quedan saltos pendientes");
        verificar(!resultado.referenciasPendientes, nombre + ": quedan referencias pendientes");
        verificarDestinosResueltos(resultado.polaca);
    }

    private static void verificarTieneError(Result resultado, String codigo) {
        for (ErrorSemantico error : resultado.erroresSemanticos) {
            if (codigo.equals(error.getCodigo())) {
                return;
            }
        }
        throw new AssertionError("Falta el error semántico " + codigo);
    }

    private static void verificarDestinosResueltos(String polaca) {
        List<String> items = instrucciones(polaca);
        for (int i = 0; i < items.size(); i++) {
            String item = items.get(i);
            verificar(!"->?".equals(item), "No deben quedar destinos pendientes");
            verificar(!item.contains("función:") || !item.endsWith("?]"),
                "No deben quedar referencias a funciones pendientes");
            if (item.startsWith("->")) {
                int destino = Integer.parseInt(item.substring(2));
                verificar(destino >= 0 && destino <= items.size(),
                    "Destino de salto dentro del rango de la Polaca");
            }
        }
    }

    private static List<String> instrucciones(String polaca) {
        List<String> items = new ArrayList<String>();
        Matcher matcher = POLACA_ITEM.matcher(polaca);
        while (matcher.find()) {
            items.add(matcher.group());
        }
        return items;
    }

    private static List<Integer> indicesComparadores(List<String> instrucciones) {
        List<Integer> indices = new ArrayList<Integer>();
        for (int i = 0; i < instrucciones.size(); i++) {
            String item = instrucciones.get(i);
            if (Arrays.asList("[==]", "[<>]", "[<]", "[<=]", "[>]", "[>=]").contains(item)) {
                indices.add(i);
            }
        }
        return indices;
    }

    private static int cantidadComparadores(List<String> instrucciones) {
        return indicesComparadores(instrucciones).size();
    }

    private static int destino(List<String> instrucciones, int indiceRama) {
        verificar(indiceRama + 1 < instrucciones.size(), "Destino presente tras rama");
        String destino = instrucciones.get(indiceRama + 1);
        verificar(destino.startsWith("->"), "Rama con destino de salto");
        return Integer.parseInt(destino.substring(2));
    }

    private static int indice(List<String> instrucciones, String valor) {
        int indice = instrucciones.indexOf(valor);
        verificar(indice >= 0, "Operador presente: " + valor);
        return indice;
    }

    private static int ocurrencias(String texto, String buscado) {
        int cantidad = 0;
        int desde = 0;
        while ((desde = texto.indexOf(buscado, desde)) >= 0) {
            cantidad++;
            desde += buscado.length();
        }
        return cantidad;
    }

    private static void verificar(boolean condicion, String mensaje) {
        pruebas++;
        if (!condicion) {
            throw new AssertionError(mensaje);
        }
    }

    private interface LexerTest {
        void run(AnalizadorLexico lexer) throws Exception;
    }

    private static final class Result {
        private final int erroresSintacticos;
        private final List<String> erroresLexicos;
        private final List<ErrorSemantico> erroresSemanticos;
        private final String polaca;
        private final boolean saltosPendientes;
        private final boolean referenciasPendientes;

        private Result(
                int erroresSintacticos,
                List<String> erroresLexicos,
                List<ErrorSemantico> erroresSemanticos,
                String polaca,
                boolean saltosPendientes,
                boolean referenciasPendientes) {
            this.erroresSintacticos = erroresSintacticos;
            this.erroresLexicos = erroresLexicos;
            this.erroresSemanticos = erroresSemanticos;
            this.polaca = polaca;
            this.saltosPendientes = saltosPendientes;
            this.referenciasPendientes = referenciasPendientes;
        }
    }
}
