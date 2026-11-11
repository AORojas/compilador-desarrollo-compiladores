package sintactico;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.StringJoiner;

public final class PolacaInversa {
    private final List<Object> elementos = new ArrayList<Object>();

    public void addReferencia(int idSimbolo) {
        elementos.add(new Referencia(idSimbolo));
    }

    public int addReferenciaPendiente(String nombreFuncion) {
        elementos.add(new ReferenciaPendiente(nombreFuncion));
        return elementos.size() - 1;
    }

    public void resolverReferencia(int posicion, int idSimbolo) {
        if (posicion < 0 || posicion >= elementos.size()) {
            throw new IndexOutOfBoundsException("Posición de referencia inválida: " + posicion);
        }
        if (!(elementos.get(posicion) instanceof ReferenciaPendiente)) {
            throw new IllegalStateException("La posición " + posicion + " no contiene una referencia pendiente.");
        }
        elementos.set(posicion, new Referencia(idSimbolo));
    }

    public void addOperador(String operador) {
        if (operador == null || operador.isEmpty()) {
            throw new IllegalArgumentException("El operador no puede estar vacío.");
        }
        elementos.add(operador);
    }

    public int getPos() {
        return elementos.size();
    }

    public int addSaltoPendiente(String operador) {
        addOperador(operador);
        elementos.add(new DestinoPendiente());
        return elementos.size() - 1;
    }

    public void patch(int posicion, int destino) {
        if (posicion < 0 || posicion >= elementos.size()) {
            throw new IndexOutOfBoundsException("Posición de salto inválida: " + posicion);
        }
        if (!(elementos.get(posicion) instanceof DestinoPendiente)) {
            throw new IllegalStateException("La posición " + posicion + " no contiene un salto pendiente.");
        }
        if (destino < 0 || destino > elementos.size()) {
            throw new IllegalArgumentException("Destino de salto inválido: " + destino);
        }
        elementos.set(posicion, new DestinoSalto(destino));
    }

    public List<Integer> makelist(int posicion) {
        return new ArrayList<Integer>(java.util.Collections.singletonList(posicion));
    }

    public List<Integer> merge(List<Integer> primera, List<Integer> segunda) {
        LinkedHashSet<Integer> combinada = new LinkedHashSet<Integer>(primera);
        combinada.addAll(segunda);
        return new ArrayList<Integer>(combinada);
    }

    public void backpatch(List<Integer> destinosPendientes, int destino) {
        for (Integer posicion : destinosPendientes) {
            patch(posicion, destino);
        }
    }

    public boolean tieneSaltosPendientes() {
        for (Object elemento : elementos) {
            if (elemento instanceof DestinoPendiente) {
                return true;
            }
        }
        return false;
    }

    public boolean tieneReferenciasPendientes() {
        for (Object elemento : elementos) {
            if (elemento instanceof ReferenciaPendiente) {
                return true;
            }
        }
        return false;
    }

    public List<Object> getElementos() {
        return new ArrayList<Object>(elementos);
    }

    public String print() {
        StringJoiner salida = new StringJoiner(" ");
        for (Object elemento : elementos) {
            if (elemento instanceof Referencia) {
                salida.add("[" + ((Referencia) elemento).id + "]");
            } else if (elemento instanceof ReferenciaPendiente) {
                salida.add("[función:" + ((ReferenciaPendiente) elemento).nombre + "?]");
            } else if (elemento instanceof DestinoSalto) {
                salida.add("->" + ((DestinoSalto) elemento).posicion);
            } else if (elemento instanceof DestinoPendiente) {
                salida.add("->?");
            } else {
                salida.add("[" + elemento + "]");
            }
        }
        return salida.toString();
    }

    private static final class Referencia {
        private final int id;

        private Referencia(int id) {
            this.id = id;
        }
    }

    private static final class ReferenciaPendiente {
        private final String nombre;

        private ReferenciaPendiente(String nombre) {
            this.nombre = nombre;
        }
    }

    private static final class DestinoPendiente {
    }

    private static final class DestinoSalto {
        private final int posicion;

        private DestinoSalto(int posicion) {
            this.posicion = posicion;
        }
    }
}
