package compilador.lexico;

import compilador.lexico.datos.Clasificador;
import compilador.lexico.datos.MatrizTokens;
import compilador.lexico.datos.MatrizTransicion;
import compilador.lexico.modelo.TipoToken;
import compilador.lexico.modelo.Token;
import compilador.lexico.semantica.GestorFuncionesSemanticas;

import java.io.IOException;
import java.io.Reader;
import java.util.Map;

public final class AnalizadorLexico {
    private final Reader lector;
    private final TablaSimbolos tablaSimbolos = new TablaSimbolos();
    private final GestorFuncionesSemanticas gestor = new GestorFuncionesSemanticas();
    private int linea = 1;
    private int pushback = -1;

    private static final Map<String, Integer> RESERVADAS = Map.ofEntries(
            Map.entry("principal", TipoToken.PRINCIPAL),
            Map.entry("entero", TipoToken.ENTERO),
            Map.entry("si", TipoToken.SI),
            Map.entry("bucle", TipoToken.BUCLE),
            Map.entry("hasta", TipoToken.HASTA),
            Map.entry("mostrar", TipoToken.MOSTRAR),
            Map.entry("mostrarTexto", TipoToken.MOSTRAR_TXT),
            Map.entry("y", TipoToken.Y),
            Map.entry("o", TipoToken.O),
            Map.entry("retornar", TipoToken.RETORNAR)
    );

    public AnalizadorLexico(Reader lector) {
        this.lector = lector;
    }

    public Token proximoToken() throws IOException {
        int estado = 0;
        gestor.limpiarLetras();
        gestor.limpiarDigitos();
        gestor.limpiarTexto();

        while (true) {
            int c = leer();
            if (c == -1) {
                return new Token(TipoToken.EOF, "EOF", linea);
            }

            int evento = Clasificador.obtenerColumna(c);

            // Blancos: se ignoran y se sigue buscando el siguiente token.
            if (estado == 0 && evento == Clasificador.BLANCO) {
                continue;
            }

            // Salto de línea: se reconoce únicamente '\n'.
            if (estado == 0 && evento == Clasificador.SALTO_LINEA) {
                Token token = new Token(TipoToken.FIN_DE_LINEA, "\n", linea);
                linea++;
            return token;
            }

            // Error léxico: carácter fuera del alfabeto.
            if (estado == 0 && evento == Clasificador.OTRO) {
                throw new IOException("Error léxico E1 en línea " + linea + ": carácter inválido '" + (char)c + "'");
            }

            // Identificador / palabra reservada.
            if (estado == 0 && evento == Clasificador.LETRA) {
                gestor.limpiarLetras();
                gestor.agregarLetra((char)c);
                estado = 1;
                continue;
            }
            if (estado == 1) {
                if (evento == Clasificador.LETRA || evento == Clasificador.DIGITO) {
                    gestor.agregarLetra((char)c);
                    continue;
                }
                unread(c);
                String lexema = gestor.letras();
                int codigo = RESERVADAS.getOrDefault(lexema, TipoToken.ID);
                if (codigo == TipoToken.ID) tablaSimbolos.registrar(lexema, codigo);
                return new Token(codigo, lexema, linea);
            }

            // Constante entera. El signo se maneja como operador RESTA; la constante
            // léxica es la secuencia de dígitos, respetando el alfabeto especificado.
            if (estado == 0 && evento == Clasificador.DIGITO) {
                gestor.limpiarDigitos();
                gestor.agregarDigito((char)c);
                estado = 2;
                continue;
            }
            if (estado == 2) {
                if (evento == Clasificador.DIGITO) {
                    gestor.agregarDigito((char)c);
                    continue;
                }
                unread(c);
                long valor;
                try {
                    valor = Long.parseLong(gestor.digitos());
                } catch (NumberFormatException ex) {
                    throw new IOException("Error léxico E2 en línea " + linea + ": constante inválida");
                }
                if (valor > Integer.MAX_VALUE) {
                    throw new IOException("Error léxico E2 en línea " + linea + ": constante fuera del rango de entero con signo de 32 bits");
                }
                tablaSimbolos.registrar(gestor.digitos(), TipoToken.CTE);
                return new Token(TipoToken.CTE, gestor.digitos(), linea);
            }

            // Literales de texto.
            if (estado == 0 && evento == Clasificador.COMILLA) {
                gestor.limpiarTexto();
                estado = 13;
                continue;
            }
            if (estado == 13) {
                if (evento == Clasificador.COMILLA) {
                    String literal = gestor.texto();
                    tablaSimbolos.registrar(literal, TipoToken.LITERAL_TXT);
                    return new Token(TipoToken.LITERAL_TXT, literal, linea);
                }
                if (c == '\n') {
                    throw new IOException(
                        "Error léxico E1 en línea " + linea +
                        ": literal de texto sin cerrar"
                );
            }
                gestor.agregarTexto((char)c);
                continue;
            }

            // Comentario o división.
            if (estado == 0 && evento == Clasificador.BARRA) {
                int siguiente = leer();
                if (siguiente == '*') {
                    int anterior = -1;
                    while (true) {
                        int ch = leer();
                        if (ch == -1) throw new IOException("Error léxico E1 en línea " + linea + ": comentario sin cerrar");
                        if (ch == '\n') {
                            linea++;
                            anterior = -1;
                            continue;
                        }
                        if (anterior == '*' && ch == '/') break;
                        anterior = ch;
                    }
                    continue;
                }
                if (siguiente != -1) unread(siguiente);
                return new Token(TipoToken.DIVISION, "/", linea);
            }

            // Operadores relacionales y asignación.
            if (estado == 0 && (evento == Clasificador.IGUAL || evento == Clasificador.MENOR || evento == Clasificador.MAYOR)) {
                int siguiente = leer();
                if (evento == Clasificador.IGUAL) {
                    if (siguiente == '=') return new Token(TipoToken.IGUAL, "==", linea);
                    if (siguiente != -1) unread(siguiente);
                    return new Token(TipoToken.ASIG, "=", linea);
                }
                if (evento == Clasificador.MENOR) {
                    if (siguiente == '=') return new Token(TipoToken.MENOR_IGUAL, "<=", linea);
                    if (siguiente == '>') return new Token(TipoToken.DISTINTO, "<>", linea);
                    if (siguiente != -1) unread(siguiente);
                    return new Token(TipoToken.MENOR, "<", linea);
                }
                if (siguiente == '=') return new Token(TipoToken.MAYOR_IGUAL, ">=", linea);
                if (siguiente != -1) unread(siguiente);
                return new Token(TipoToken.MAYOR, ">", linea);
            }

            // Tokens de un solo carácter.
            int directo = MatrizTokens.tokenDirecto(siguienteEstadoDirecto(evento), evento);
            if (directo != -1) {
                return new Token(directo, String.valueOf((char)c), linea);
            }

            throw new IOException("Error léxico E1 en línea " + linea + ": carácter no reconocido '" + (char)c + "'");
        }
    }

    private int siguienteEstadoDirecto(int evento) {
        return MatrizTransicion.siguiente(0, evento);
    }

    private int leer() throws IOException {
        if (pushback != -1) {
            int c = pushback;
            pushback = -1;
            return c;
        }
        return lector.read();
    }

    private void unread(int c) {
        pushback = c;
    }

    public int getLineaActual() { return linea; }
    public TablaSimbolos getTablaSimbolos() { return tablaSimbolos; }
}
