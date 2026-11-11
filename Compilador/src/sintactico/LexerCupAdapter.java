package sintactico;

import java_cup.runtime.Scanner;
import java_cup.runtime.Symbol;
import lexico.AnalizadorLexico;
import lexico.ContextoLexico;

public final class LexerCupAdapter implements Scanner {
    private final AnalizadorLexico lexer;

    public LexerCupAdapter(AnalizadorLexico lexer) {
        if (lexer == null) {
            throw new IllegalArgumentException("El analizador léxico no puede ser null.");
        }
        this.lexer = lexer;
    }

    @Override
    public Symbol next_token() throws Exception {
        ContextoLexico contexto = lexer.getContexto();

        while (true) {
            int erroresPrevios = contexto.errores.size();
            int codigo = lexer.yylex();
            int linea = lexer.getLineaToken() >= 0 ? lexer.getLineaToken() : lexer.getLinea();
            int columna = lexer.getColumnaToken() >= 0 ? lexer.getColumnaToken() : lexer.getColumna();
            String lexema = contexto.lexemaActual;

            if (codigo == -1) {
                if (contexto.errores.size() > erroresPrevios) {
                    continue;
                }
                throw new IllegalStateException("El analizador léxico devolvió un token inválido sin registrar un error.");
            }

            switch (codigo) {
                case 256: return simbolo(sym.ID, linea, columna, lexema);
                case 257: return simbolo(sym.CTE, linea, columna, lexema);
                case 258: return simbolo(sym.LITERAL_TXT, linea, columna, lexema);
                case 259: return simbolo(sym.PRINCIPAL, linea, columna, null);
                case 260: return simbolo(sym.ENTERO, linea, columna, null);
                case 261: return simbolo(sym.SI, linea, columna, null);
                case 262: return simbolo(sym.BUCLE, linea, columna, null);
                case 263: return simbolo(sym.HASTA, linea, columna, null);
                case 264: return simbolo(sym.MOSTRAR, linea, columna, null);
                case 265: return simbolo(sym.MOSTRAR_TXT, linea, columna, null);
                case 266: return simbolo(sym.Y, linea, columna, null);
                case 267: return simbolo(sym.O, linea, columna, null);
                case 268: return simbolo(sym.RETORNAR, linea, columna, null);
                case 269: return simbolo(sym.ASIG, linea, columna, null);
                case 270: return simbolo(sym.IGUAL, linea, columna, null);
                case 271: return simbolo(sym.DISTINTO, linea, columna, null);
                case 272: return simbolo(sym.MENOR, linea, columna, null);
                case 273: return simbolo(sym.MENOR_O_IGUAL, linea, columna, null);
                case 274: return simbolo(sym.MAYOR, linea, columna, null);
                case 275: return simbolo(sym.MAYOR_O_IGUAL, linea, columna, null);
                case 276: return simbolo(sym.EOF, linea, columna, null);
                case 277: return simbolo(sym.SUMA, linea, columna, null);
                case 278: return simbolo(sym.RESTA, linea, columna, null);
                case 279: return simbolo(sym.MULTIPLICACION, linea, columna, null);
                case 280: return simbolo(sym.DIVISION, linea, columna, null);
                case 281: return simbolo(sym.PAREN_IZQ, linea, columna, null);
                case 282: return simbolo(sym.PAREN_DER, linea, columna, null);
                case 283: return simbolo(sym.LLAVE_IZQ, linea, columna, null);
                case 284: return simbolo(sym.LLAVE_DER, linea, columna, null);
                case 285: return simbolo(sym.COMA, linea, columna, null);
                case 286: return simbolo(sym.FIN_DE_LINEA, linea, columna, null);
                default:
                    throw new IllegalStateException("Código léxico sin correspondencia CUP: " + codigo);
            }
        }
    }

    private Symbol simbolo(int id, int linea, int columna, Object valor) {
        return new Symbol(id, linea, columna, valor);
    }
}
