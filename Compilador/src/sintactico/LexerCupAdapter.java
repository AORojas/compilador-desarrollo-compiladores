package sintactico;

import java_cup.runtime.Scanner;
import java_cup.runtime.Symbol;
import lexico.AnalizadorLexico;
import lexico.ContextoLexico;

public class LexerCupAdapter implements Scanner {
    private final AnalizadorLexico analizador;
    private int erroresLexicosObservados;

    public LexerCupAdapter(AnalizadorLexico analizador) {
        this.analizador = analizador;
    }

    @Override
    public Symbol next_token() throws Exception {
        while (true) {
            int codigo = analizador.yylex();
            ContextoLexico contexto = analizador.getContexto();

            if (codigo == -1) {
                if (contexto.errores.size() > erroresLexicosObservados) {
                    erroresLexicosObservados = contexto.errores.size();
                    continue;
                }
                return simbolo(sym.EOF, null, contexto);
            }

            erroresLexicosObservados = contexto.errores.size();
            if (codigo == 276) {
                return simbolo(sym.EOF, null, contexto);
            }

            int terminal = terminalCUP(codigo);
            String lexema = codigo == 256 || codigo == 257 || codigo == 258
                ? contexto.lexemaActual
                : null;
            return simbolo(terminal, lexema, contexto);
        }
    }

    private Symbol simbolo(int terminal, String valor, ContextoLexico contexto) {
        int linea = analizador.getLineaToken();
        int columna = analizador.getColumnaToken();
        if (linea < 1 || columna < 1) {
            linea = contexto.lector.getUltimaLineaLeida();
            columna = contexto.lector.getUltimaColumnaLeida();
        }
        return new Symbol(terminal, linea, columna, valor);
    }

    private int terminalCUP(int codigo) {
        switch (codigo) {
            case 256: return sym.ID;
            case 257: return sym.CTE;
            case 258: return sym.LITERAL_TXT;
            case 259: return sym.PRINCIPAL;
            case 260: return sym.ENTERO;
            case 261: return sym.SI;
            case 262: return sym.BUCLE;
            case 263: return sym.HASTA;
            case 264: return sym.MOSTRAR;
            case 265: return sym.MOSTRAR_TXT;
            case 266: return sym.Y;
            case 267: return sym.O;
            case 268: return sym.RETORNAR;
            case 269: return sym.ASIG;
            case 270: return sym.IGUAL;
            case 271: return sym.DISTINTO;
            case 272: return sym.MENOR;
            case 273: return sym.MENOR_O_IGUAL;
            case 274: return sym.MAYOR;
            case 275: return sym.MAYOR_O_IGUAL;
            case 277: return sym.SUMA;
            case 278: return sym.RESTA;
            case 279: return sym.MULTIPLICACION;
            case 280: return sym.DIVISION;
            case 281: return sym.PAREN_IZQ;
            case 282: return sym.PAREN_DER;
            case 283: return sym.LLAVE_IZQ;
            case 284: return sym.LLAVE_DER;
            case 285: return sym.COMA;
            case 286: return sym.FIN_DE_LINEA;
            default:
                throw new IllegalStateException("Código léxico sin mapeo CUP: " + codigo);
        }
    }
}
