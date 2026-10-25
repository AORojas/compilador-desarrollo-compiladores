package lexico;

import java.io.FileReader;
import java.io.IOException;
import java.io.PushbackReader;

public class Lector implements AutoCloseable {
    private final PushbackReader reader;
    private int linea = 1;

    public Lector(String archivo) throws IOException {
        reader = new PushbackReader(new FileReader(archivo), 1);
    }

    public int nextChar() throws IOException {
        int c = reader.read();
        if (c == '\n') {
            linea++;
        }
        return c;
    }

    public void unread(int c) throws IOException {
        if (c != -1) {
            if (c == '\n') {
                linea--;
            }
            reader.unread(c);
        }
    }

    public int getLinea() {
        return linea;
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }
}
