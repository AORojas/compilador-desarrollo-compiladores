package gc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class PolacaInversa {
    public static final class Referencia {
        public final int id;

        private Referencia(int id) {
            this.id = id;
        }

        @Override
        public String toString() {
            return Integer.toString(id);
        }
    }

    public static final class DestinoPendiente {
        private DestinoPendiente() {
        }
    }

    public static final class DestinoSalto {
        public final int destino;

        private DestinoSalto(int destino) {
            this.destino = destino;
        }

        @Override
        public String toString() {
            return Integer.toString(destino);
        }
    }

    private final List<Object> instrucciones = new ArrayList<Object>();

    public void addReferencia(int id) {
        if (id < 1) {
            throw new IllegalArgumentException("El ID de una referencia debe ser positivo.");
        }
        instrucciones.add(new Referencia(id));
    }

    public void addOperador(String operador) {
        if (operador == null || operador.isEmpty()) {
            throw new IllegalArgumentException("El operador no puede estar vacío.");
        }
        instrucciones.add(operador);
    }

    public int addSaltoPendiente(String operador) {
        addOperador(operador);
        instrucciones.add(new DestinoPendiente());
        return instrucciones.size() - 1;
    }

    public void patch(int posicionHueco, int destino) {
        if (posicionHueco < 0 || posicionHueco >= instrucciones.size()) {
            throw new IndexOutOfBoundsException("Posición de backpatching inválida: " + posicionHueco);
        }
        if (!(instrucciones.get(posicionHueco) instanceof DestinoPendiente)) {
            throw new IllegalStateException("La posición indicada no contiene un destino pendiente.");
        }
        if (destino < 0 || destino > instrucciones.size()) {
            throw new IllegalArgumentException("Destino de salto inválido: " + destino);
        }
        instrucciones.set(posicionHueco, new DestinoSalto(destino));
    }

    public void addSaltoIncondicional(int destino) {
        if (destino < 0 || destino > instrucciones.size()) {
            throw new IllegalArgumentException("Destino de salto inválido: " + destino);
        }
        addOperador("BI");
        instrucciones.add(new DestinoSalto(destino));
    }

    public int getPos() {
        return instrucciones.size();
    }

    public void resolverDestinos() {
        for (Object instruccion : instrucciones) {
            if (instruccion instanceof DestinoPendiente) {
                throw new IllegalStateException("La Polaca contiene destinos de salto sin resolver.");
            }
        }
    }

    public List<Object> getInstrucciones() {
        return Collections.unmodifiableList(instrucciones);
    }

    public String formatear() {
        resolverDestinos();
        StringBuilder salida = new StringBuilder();
        for (Object instruccion : instrucciones) {
            if (salida.length() > 0) {
                salida.append(' ');
            }
            salida.append('[').append(instruccion).append(']');
        }
        return salida.toString();
    }
}
