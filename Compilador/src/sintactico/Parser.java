package sintactico;

import java.util.ArrayList;
import java.util.List;

import lexico.AnalizadorLexico;
import lexico.ContextoLexico;

public class Parser {
    private final AnalizadorLexico lexer;
    private final ContextoLexico contexto;
    private final TablaSimbolos tablaSimbolos = new TablaSimbolos();
    private final PolacaInversa polaca = new PolacaInversa();
    private String ultimoNombreDeclarado;

    private int tokenActual;
    private String lexemaActual;

    public Parser(AnalizadorLexico lexer) throws Exception {
        this.lexer = lexer;
        this.contexto = lexer.getContexto();
        avanzar();
    }

    public Parser(String archivo) throws Exception {
        this(new AnalizadorLexico(archivo));
    }

    public void parse() throws Exception {
        programa();
        if (tokenActual != 276) {
            throw new IllegalStateException("Se esperaba fin de archivo. Se encontró: " + tokenActual + " / " + lexemaActual);
        }
    }

    public TablaSimbolos getTablaSimbolos() {
        return tablaSimbolos;
    }

    public PolacaInversa getPolaca() {
        return polaca;
    }

    private void avanzar() throws Exception {
        tokenActual = lexer.yylex();
        lexemaActual = contexto.lexemaActual;
        while (tokenActual == 286) {
            tokenActual = lexer.yylex();
            lexemaActual = contexto.lexemaActual;
        }
    }

    private void consumir(int tokenEsperado) throws Exception {
        if (tokenActual != tokenEsperado) {
            throw new IllegalStateException(
                "Token inesperado. Esperado " + tokenEsperado + " pero se encontró " + tokenActual + " (" + lexemaActual + ")");
        }
        avanzar();
    }

    private void programa() throws Exception {
        if (tokenActual == 260) {
            consumir(260); // entero
            consumir(259); // principal
            consumir(281); // (
            consumir(282); // )
            bloque();
            return;
        }

        consumir(259); // principal
        consumir(281); // (
        consumir(282); // )
        bloque();
    }

    private void bloque() throws Exception {
        consumir(283); // {
        while (tokenActual != 284 && tokenActual != 276) {
            sentencia();
        }
        consumir(284); // }
    }

    private void sentencia() throws Exception {
        switch (tokenActual) {
            case 260: // entero
                declaracion();
                return;
            case 261: // si
                condicionSi();
                return;
            case 262: // bucle
                bucle();
                return;
            case 264: // mostrar
                mostrar();
                return;
            case 268: // retornar
                retornar();
                return;
            case 256: // ID
                asignacion();
                return;
            case 269: // =
                asignacionImplicita();
                return;
            default:
                if (tokenActual == 286) {
                    avanzar();
                    return;
                }
                throw new IllegalStateException("Sentencia no soportada: " + tokenActual + " / " + lexemaActual);
        }
    }

    private void declaracion() throws Exception {
        consumir(260);
        List<String> nombres = new ArrayList<String>();
        while (tokenActual == 256) {
            String nombre = lexemaActual;
            consumir(256);
            nombres.add(nombre);
            tablaSimbolos.registrar(nombre, "entero");
            ultimoNombreDeclarado = nombre;
            if (tokenActual == 285) {
                consumir(285);
            }
        }
        if (tokenActual == 286) {
            avanzar();
        }
        if (tokenActual == 269 && !nombres.isEmpty()) {
            asignacionImplicita();
        }
    }

    private void asignacion() throws Exception {
        String nombre = lexemaActual;
        consumir(256);
        consumir(269); // =
        expresion();
        polaca.add(":=");
        polaca.add(nombre);
    }

    private void asignacionImplicita() throws Exception {
        if (ultimoNombreDeclarado == null || ultimoNombreDeclarado.isEmpty()) {
            throw new IllegalStateException("Asignación sin identificador válido: " + lexemaActual);
        }
        String nombre = ultimoNombreDeclarado;
        consumir(269); // =
        expresion();
        polaca.add(":=");
        polaca.add(nombre);
    }

    private void condicionSi() throws Exception {
        consumir(261); // si
        consumir(281); // (
        condicion();
        consumir(282); // )
        bloque();
    }

    private void bucle() throws Exception {
        consumir(262); // bucle
        bloque();
        consumir(263); // hasta
        consumir(281); // (
        condicion();
        consumir(282); // )
    }

    private void mostrar() throws Exception {
        consumir(264);
        consumir(281);
        expresion();
        consumir(282);
        polaca.add("PRINT");
    }

    private void retornar() throws Exception {
        consumir(268);
        expresion();
        polaca.add("RET");
    }

    private void condicion() throws Exception {
        expresion();
        if (esOperadorRelacional(tokenActual)) {
            int op = tokenActual;
            avanzar();
            expresion();
            polaca.addOperador(op);
        }
        while (tokenActual == 266 || tokenActual == 267) {
            int op = tokenActual;
            avanzar();
            expresion();
            if (esOperadorRelacional(tokenActual)) {
                int rel = tokenActual;
                avanzar();
                expresion();
                polaca.addOperador(rel);
            }
            polaca.addOperador(op);
        }
    }

    private void expresion() throws Exception {
        termino();
        while (tokenActual == 277 || tokenActual == 278) {
            int op = tokenActual;
            avanzar();
            termino();
            polaca.addOperador(op);
        }
    }

    private void termino() throws Exception {
        factor();
        while (tokenActual == 279 || tokenActual == 280) {
            int op = tokenActual;
            avanzar();
            factor();
            polaca.addOperador(op);
        }
    }

    private void factor() throws Exception {
        if (tokenActual == 256 || tokenActual == 257 || tokenActual == 258) {
            polaca.add(lexemaActual);
            avanzar();
            return;
        }

        if (tokenActual == 281) {
            avanzar();
            expresion();
            consumir(282);
            return;
        }

        throw new IllegalStateException("Factor inválido: " + tokenActual + " / " + lexemaActual);
    }

    private boolean esOperadorRelacional(int token) {
        return token == 270 || token == 271 || token == 272 || token == 273 || token == 274 || token == 275;
    }
}
