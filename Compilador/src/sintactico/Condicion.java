package sintactico;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Condicion {
    private final List<Integer> listaVerdadera;
    private final List<Integer> listaFalsa;

    public Condicion(List<Integer> listaVerdadera, List<Integer> listaFalsa) {
        this.listaVerdadera = Collections.unmodifiableList(
            new ArrayList<Integer>(listaVerdadera));
        this.listaFalsa = Collections.unmodifiableList(
            new ArrayList<Integer>(listaFalsa));
    }

    public List<Integer> getListaVerdadera() {
        return listaVerdadera;
    }

    public List<Integer> getListaFalsa() {
        return listaFalsa;
    }
}
