package sintactico;

public final class Simbolo {
    private final int id;
    private final String nombre;
    private final String tipo;
    private final String clase;
    private final String valor;
    private final int longitud;
    private final int ambito;

    public Simbolo(
            int id,
            String nombre,
            String tipo,
            String clase,
            String valor,
            int longitud,
            int ambito) {
        if (nombre == null || tipo == null || clase == null) {
            throw new IllegalArgumentException("Nombre, tipo y clase son obligatorios.");
        }
        this.id = id;
        this.nombre = nombre;
        this.tipo = tipo;
        this.clase = clase;
        this.valor = valor;
        this.longitud = longitud;
        this.ambito = ambito;
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getTipo() {
        return tipo;
    }

    public String getClase() {
        return clase;
    }

    public String getValor() {
        return valor;
    }

    public int getLongitud() {
        return longitud;
    }

    public int getAmbito() {
        return ambito;
    }
}
