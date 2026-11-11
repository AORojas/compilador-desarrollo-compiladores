package lexico;

import java.io.FileReader;
import java.io.IOException;
import java.io.PushbackReader;

public class Lector implements AutoCloseable {
    private final PushbackReader reader;
    private int linea = 1;
    private int columna = 1;
    private int ultimaLineaLeida = 1;
    private int ultimaColumnaLeida = 1;

    public Lector(String archivo) throws IOException {
        reader = new PushbackReader(new FileReader(archivo), 1);
    }

    public int nextChar() throws IOException {
        int c = reader.read();
        if (c != -1) {
            ultimaLineaLeida = linea;
            ultimaColumnaLeida = columna;
            if (c == '\n') {
                linea++;
                columna = 1;
            } else {
                columna++;
            }
        }
        return c;
    }

    public void unread(int c) throws IOException {
        if (c != -1) {
            reader.unread(c);
            linea = ultimaLineaLeida;
            columna = ultimaColumnaLeida;
        }
    }

    public int getLinea() {
        return linea;
    }

    public int getColumna() {
        return columna;
    }

    public int getUltimaLineaLeida() {
        return ultimaLineaLeida;
    }

    public int getUltimaColumnaLeida() {
        return ultimaColumnaLeida;
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }
}
