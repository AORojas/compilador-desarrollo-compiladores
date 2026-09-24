package compilador.lexico.datos;

import compilador.lexico.AnalizadorLexico;
import compilador.lexico.modelo.TipoToken;
import compilador.lexico.modelo.Token;
import compilador.lexico.semantica.GestorFuncionesSemanticas;

import java.io.IOException;

public final class MatrizTransicion {
    private MatrizTransicion() {}

    public static final int EF = -1;

    public static final int IGNORAR = 1;
    public static final int FIN_LINEA = 2;
    public static final int ERROR = 3;
    public static final int INICIO_IDENTIFICADOR = 4;
    public static final int CONTINUA_IDENTIFICADOR = 5;
    public static final int FINAL_IDENTIFICADOR = 6;
    public static final int INICIO_CONSTANTE = 7;
    public static final int CONTINUA_CONSTANTE = 8;
    public static final int FINAL_CONSTANTE = 9;
    public static final int INICIO_LITERAL = 10;
    public static final int FINAL_LITERAL = 11;
    public static final int LITERAL_SIN_CERRAR = 12;
    public static final int COMENTARIO = 13;
    public static final int OPERADOR_RELACIONAL = 14;
    public static final int DIRECTO = 15;
    public static final int NINGUNA = -1;

    public static final class Resultado {
        private final boolean continuacion;
        private final int estado;
        private final Token token;
        private final IOException error;

        private Resultado(boolean continuacion, int estado, Token token, IOException error) {
            this.continuacion = continuacion;
            this.estado = estado;
            this.token = token;
            this.error = error;
        }

        public static Resultado continuar(int estado) { return new Resultado(true, estado, null, null); }
        public static Resultado token(Token token) { return new Resultado(false, 0, token, null); }
        public static Resultado error(IOException error) { return new Resultado(false, 0, null, error); }

        public boolean esContinuacion() { return continuacion; }
        public int getEstado() { return estado; }
        public Token getToken() { return token; }
        public IOException getError() { return error; }
    }

    // Estados: E0..E22 = 0..22. EF = -1.
    public static final int[][] MATRIZ = {
        {1,2,3,5,8,10,16,13,14,15,17,18,19,20,21,22,0,EF},
        {1,1,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF},
        {EF,2,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF},
        {EF,EF,4,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF},
        {EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF},
        {EF,EF,6,EF,7,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF},
        {EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF},
        {EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF},
        {EF,EF,9,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF},
        {EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF},
        {EF,EF,EF,EF,EF,11,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF},
        {11,11,11,11,11,11,12,11,11,11,11,11,11,11,11,11,11,11},
        {11,11,11,11,11,0,11,11,11,11,11,11,11,11,11,11,11,11},
        {13,13,13,13,13,13,13,EF,13,13,13,13,13,13,13,13,13,13},
        {EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF},
        {EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF},
        {EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF},
        {EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF},
        {EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF},
        {EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF},
        {EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF},
        {EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF},
        {EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF,EF}
    };

    public static int siguiente(int estado, int evento) {
        return MATRIZ[estado][evento];
    }

    public static int siguienteEstadoDirecto(int evento) {
        return siguiente(0, evento);
    }

    public static int tipo(int estado, int evento, int c) {
        if (estado == 0 && evento == Clasificador.BLANCO) return IGNORAR;
        if (estado == 0 && evento == Clasificador.SALTO_LINEA) return FIN_LINEA;
        if (estado == 0 && evento == Clasificador.OTRO) return ERROR;

        if (estado == 0 && evento == Clasificador.LETRA) return INICIO_IDENTIFICADOR;
        if (estado == 1 && (evento == Clasificador.LETRA || evento == Clasificador.DIGITO)) return CONTINUA_IDENTIFICADOR;
        if (estado == 1) return FINAL_IDENTIFICADOR;

        if (estado == 0 && evento == Clasificador.DIGITO) return INICIO_CONSTANTE;
        if (estado == 2 && evento == Clasificador.DIGITO) return CONTINUA_CONSTANTE;
        if (estado == 2) return FINAL_CONSTANTE;

        if (estado == 0 && evento == Clasificador.COMILLA) return INICIO_LITERAL;
        if (estado == 13 && evento == Clasificador.COMILLA) return FINAL_LITERAL;
        if (estado == 13 && c == '\n') return LITERAL_SIN_CERRAR;

        if (estado == 0 && evento == Clasificador.BARRA) return COMENTARIO;
        if (estado == 0 && (evento == Clasificador.IGUAL || evento == Clasificador.MENOR || evento == Clasificador.MAYOR)) return OPERADOR_RELACIONAL;

        if (estado == 0) return DIRECTO;

        return NINGUNA;
    }

    public static Resultado resolver(int estado, int evento, int c, AnalizadorLexico analizador, GestorFuncionesSemanticas gestor) throws IOException {
        int tipo = tipo(estado, evento, c);

        switch (tipo) {
            case IGNORAR -> { return Resultado.continuar(0); }
            case FIN_LINEA -> {
                Token token = analizador.crearToken(TipoToken.FIN_DE_LINEA, "\n");
                analizador.incrementarLinea();
                return Resultado.token(token);
            }
            case ERROR -> {
                throw new IOException("Error léxico E1 en línea " + analizador.getLineaActual() + ": carácter inválido '" + (char)c + "'");
            }
            case INICIO_IDENTIFICADOR -> { return Resultado.continuar(siguiente(estado, evento)); }
            case CONTINUA_IDENTIFICADOR -> { return Resultado.continuar(estado); }
            case FINAL_IDENTIFICADOR -> {
                analizador.unreadDesdeAnalizador(c);
                String lexema = gestor.letras();
                int codigo = AnalizadorLexico.RESERVADAS.getOrDefault(lexema, TipoToken.ID);
                if (codigo == TipoToken.ID) analizador.registrarEnTabla(lexema, codigo);
                return Resultado.token(analizador.crearToken(codigo, lexema));
            }
            case INICIO_CONSTANTE -> { return Resultado.continuar(siguiente(estado, evento)); }
            case CONTINUA_CONSTANTE -> { return Resultado.continuar(estado); }
            case FINAL_CONSTANTE -> {
                analizador.unreadDesdeAnalizador(c);
                String digitos = gestor.digitos();
                long valor;
                try {
                    valor = Long.parseLong(digitos);
                } catch (NumberFormatException ex) {
                    throw new IOException("Error léxico E2 en línea " + analizador.getLineaActual() + ": constante inválida");
                }
                if (valor > Integer.MAX_VALUE) {
                    throw new IOException("Error léxico E2 en línea " + analizador.getLineaActual() + ": constante fuera del rango de entero con signo de 32 bits");
                }
                analizador.registrarEnTabla(digitos, TipoToken.CTE);
                return Resultado.token(analizador.crearToken(TipoToken.CTE, digitos));
            }
            case INICIO_LITERAL -> { return Resultado.continuar(siguiente(estado, evento)); }
            case FINAL_LITERAL -> {
                String literal = gestor.texto();
                analizador.registrarEnTabla(literal, TipoToken.LITERAL_TXT);
                return Resultado.token(analizador.crearToken(TipoToken.LITERAL_TXT, literal));
            }
            case LITERAL_SIN_CERRAR -> {
                throw new IOException("Error léxico E1 en línea " + analizador.getLineaActual() + ": literal de texto sin cerrar");
            }
            case COMENTARIO -> {
                int siguiente = analizador.leerDesdeAnalizador();
                if (siguiente == '*') {
                    int anterior = -1;
                    while (true) {
                        int ch = analizador.leerDesdeAnalizador();
                        if (ch == -1) throw new IOException("Error léxico E1 en línea " + analizador.getLineaActual() + ": comentario sin cerrar");
                        if (ch == '\n') {
                            analizador.incrementarLinea();
                            anterior = -1;
                            continue;
                        }
                        if (anterior == '*' && ch == '/') break;
                        anterior = ch;
                    }
                    return Resultado.continuar(0);
                }
                if (siguiente != -1) analizador.unreadDesdeAnalizador(siguiente);
                return Resultado.token(analizador.crearToken(TipoToken.DIVISION, "/"));
            }
            case OPERADOR_RELACIONAL -> {
                int siguiente = analizador.leerDesdeAnalizador();
                if (evento == Clasificador.IGUAL) {
                    if (siguiente == '=') return Resultado.token(analizador.crearToken(TipoToken.IGUAL, "=="));
                    if (siguiente != -1) analizador.unreadDesdeAnalizador(siguiente);
                    return Resultado.token(analizador.crearToken(TipoToken.ASIG, "="));
                }
                if (evento == Clasificador.MENOR) {
                    if (siguiente == '=') return Resultado.token(analizador.crearToken(TipoToken.MENOR_IGUAL, "<="));
                    if (siguiente == '>') return Resultado.token(analizador.crearToken(TipoToken.DISTINTO, "<>"));
                    if (siguiente != -1) analizador.unreadDesdeAnalizador(siguiente);
                    return Resultado.token(analizador.crearToken(TipoToken.MENOR, "<"));
                }
                if (siguiente == '=') return Resultado.token(analizador.crearToken(TipoToken.MAYOR_IGUAL, ">="));
                if (siguiente != -1) analizador.unreadDesdeAnalizador(siguiente);
                return Resultado.token(analizador.crearToken(TipoToken.MAYOR, ">"));
            }
            case DIRECTO -> {
                int directo = MatrizTokens.tokenDirecto(siguienteEstadoDirecto(evento), evento);
                if (directo != -1) {
                    return Resultado.token(analizador.crearToken(directo, String.valueOf((char)c)));
                }
                throw new IOException("Error léxico E1 en línea " + analizador.getLineaActual() + ": carácter no reconocido '" + (char)c + "'");
            }
            default -> {
                throw new IOException("Error léxico E1 en línea " + analizador.getLineaActual() + ": carácter no reconocido '" + (char)c + "'");
            }
        }
    }
}
