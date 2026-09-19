package compilador.lexico;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import compilador.lexico.matrices.Transicion;

public class Lexico {
    private final Reader reader;
    private final TablaSimbolos tablaSimbolos;
    private Estado estadoActual;
    private int lineaActual;
    private int ultimoCharLeido;
    private boolean hayCaracterPushback;
    private final StringBuilder bufferLetras = new StringBuilder();
    private final StringBuilder bufferDigitos = new StringBuilder();
    private final StringBuilder bufferLiteral = new StringBuilder();

    private static final Map<String, Integer> PALABRAS_RESERVADAS = new HashMap<>();

    static {
        PALABRAS_RESERVADAS.put("principal", TokenType.PRINCIPAL);
        PALABRAS_RESERVADAS.put("entero", TokenType.ENTERO);
        PALABRAS_RESERVADAS.put("si", TokenType.SI);
        PALABRAS_RESERVADAS.put("bucle", TokenType.BUCLE);
        PALABRAS_RESERVADAS.put("hasta", TokenType.HASTA);
        PALABRAS_RESERVADAS.put("mostrar", TokenType.MOSTRAR);
        PALABRAS_RESERVADAS.put("mostrarTexto", TokenType.MOSTRAR_TXT);
        PALABRAS_RESERVADAS.put("y", TokenType.Y);
        PALABRAS_RESERVADAS.put("o", TokenType.O);
        PALABRAS_RESERVADAS.put("retornar", TokenType.RETORNAR);
    }

    public Lexico(Reader reader) {
        this.reader = reader;
        this.tablaSimbolos = new TablaSimbolos();
        this.estadoActual = Estado.E0;
        this.lineaActual = 1;
        this.ultimoCharLeido = -1;
        this.hayCaracterPushback = false;
    }

    public Token yylex() throws IOException {
        StringBuilder bufferTemporal = new StringBuilder();
        Estado estado = Estado.E0;

        while (true) {
            int c = leerSiguienteChar();
            if (c == -1) {
                return new Token(TokenType.EOF, "EOF", lineaActual);
            }

            Evento evento = getEvento((char) c);
            int accion = Transicion.FUNCIONES[estado.ordinal()][evento.ordinal()];
            estado = getNuevoEstado(estado, evento);

            switch (accion) {
                case 1 -> {
                    bufferLetras.setLength(0);
                    bufferLetras.append((char) c);
                }
                case 2 -> {
                    bufferDigitos.setLength(0);
                    bufferDigitos.append((char) c);
                }
                case 3 -> {
                    bufferLiteral.setLength(0);
                }
                case 4 -> {
                    // ignorar espacio / tab / CR ya manejado en F8
                }
                case 5 -> bufferLetras.append((char) c);
                case 6 -> bufferDigitos.append((char) c);
                case 7 -> {
                    String lexema = bufferLetras.toString();
                    int token = PALABRAS_RESERVADAS.getOrDefault(lexema, TokenType.ID);
                    if (token == TokenType.ID) {
                        tablaSimbolos.registrar(lexema, TokenType.ID);
                    }
                    if (c != -1 && !esParteDeIdentificador((char) c)) {
                        unread((char) c);
                    }
                    return new Token(token, lexema, lineaActual);
                }
                case 8 -> {
                    lineaActual++;
                    return new Token(TokenType.FIN_DE_LINEA, "\n", lineaActual - 1);
                }
                case 9 -> {
                    String lexema = bufferDigitos.toString();
                    int valor = Integer.parseInt(lexema);
                    tablaSimbolos.registrar(lexema, TokenType.CTE);
                    if (c != -1 && !Character.isDigit((char) c)) {
                        unread((char) c);
                    }
                    return new Token(TokenType.CTE, lexema, lineaActual);
                }
                case 10 -> {
                    String texto = String.valueOf((char) c);
                    return new Token(identificarTokenDirecto((char) c), texto, lineaActual);
                }
                case 11 -> {
                    String literal = bufferLiteral.toString();
                    tablaSimbolos.registrar(literal, TokenType.LITERAL_TXT);
                    return new Token(TokenType.LITERAL_TXT, literal, lineaActual);
                }
                case 12 -> {
                    // comentario: descartar y continuar
                    while (true) {
                        int ch = leerSiguienteChar();
                        if (ch == -1) {
                            throw new IOException("Comentario no cerrado");
                        }
                        if (ch == '*') {
                            int sig = leerSiguienteChar();
                            if (sig == '/') {
                                break;
                            }
                            unread((char) sig);
                        }
                    }
                    break;
                }
                default -> throw new IllegalStateException("Accion semantica invaida: " + accion);
            }

            if (estado == Estado.EF) {
                return new Token(TokenType.EOF, "EOF", lineaActual);
            }
        }
    }

    private Estado getNuevoEstado(Estado actual, Evento evento) {
        return switch (actual) {
            case E0 -> switch (evento) {
                case L -> Estado.E1;
                case D -> Estado.E2;
                case IGUAL -> Estado.E3;
                case MENOR -> Estado.E5;
                case MAYOR -> Estado.E8;
                case BARRA -> Estado.E10;
                case ASTERISCO -> Estado.E16;
                case COMILLA -> Estado.E13;
                case MAS -> Estado.E14;
                case MENOS -> Estado.E15;
                case PAREN_IZQ -> Estado.E17;
                case PAREN_DER -> Estado.E18;
                case LLAVE_IZQ -> Estado.E19;
                case LLAVE_DER -> Estado.E20;
                case COMA -> Estado.E21;
                case CR -> Estado.E22;
                case BL -> Estado.E0;
                case OTRO -> Estado.EF;
            };
            case E1 -> Estado.E1;
            case E2 -> Estado.E2;
            case E3 -> Estado.E4;
            case E4 -> Estado.EF;
            case E5 -> Estado.EF;
            case E6 -> Estado.EF;
            case E7 -> Estado.EF;
            case E8 -> Estado.EF;
            case E9 -> Estado.EF;
            case E10 -> Estado.EF;
            case E11 -> Estado.E11;
            case E12 -> Estado.E11;
            case E13 -> Estado.E13;
            default -> Estado.EF;
        };
    }

    private int identificarTokenDirecto(char c) {
        return switch (c) {
            case '=' -> TokenType.ASIG;
            case '<' -> TokenType.MENOR;
            case '>' -> TokenType.MAYOR;
            case '+' -> TokenType.SUMA;
            case '-' -> TokenType.RESTA;
            case '*' -> TokenType.MULTIPLICACION;
            case '/' -> TokenType.DIVISION;
            case '(' -> TokenType.PAREN_IZQ;
            case ')' -> TokenType.PAREN_DER;
            case '{' -> TokenType.LLAVE_IZQ;
            case '}' -> TokenType.LLAVE_DER;
            case ',' -> TokenType.COMA;
            default -> TokenType.EOF;
        };
    }

    private Evento getEvento(char c) {
        if (Character.isLetter(c)) return Evento.L;
        if (Character.isDigit(c)) return Evento.D;
        if (c == '=') return Evento.IGUAL;
        if (c == '<') return Evento.MENOR;
        if (c == '>') return Evento.MAYOR;
        if (c == '/') return Evento.BARRA;
        if (c == '*') return Evento.ASTERISCO;
        if (c == '"') return Evento.COMILLA;
        if (c == '+') return Evento.MAS;
        if (c == '-') return Evento.MENOS;
        if (c == '(') return Evento.PAREN_IZQ;
        if (c == ')') return Evento.PAREN_DER;
        if (c == '{') return Evento.LLAVE_IZQ;
        if (c == '}') return Evento.LLAVE_DER;
        if (c == ',') return Evento.COMA;
        if (c == '\n' || c == '\r') return Evento.CR;
        if (c == ' ' || c == '\t') return Evento.BL;
        return Evento.OTRO;
    }

    private int leerSiguienteChar() throws IOException {
        if (hayCaracterPushback) {
            hayCaracterPushback = false;
            return ultimoCharLeido;
        }

        int valor = reader.read();
        if (valor != -1) {
            ultimoCharLeido = valor;
        }
        return valor;
    }

    private void unread(char c) {
        ultimoCharLeido = c;
        hayCaracterPushback = true;
    }

    private boolean esParteDeIdentificador(char c) {
        return Character.isLetterOrDigit(c) || c == '_';
    }
}
