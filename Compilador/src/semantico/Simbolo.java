package semantico;

public final class Simbolo {
    public final int id;
    public final String nombre;
    public final String tipo;
    public final String clase;
    public final String valor;
    public final int longitud;
    public final int ambito;

    Simbolo(
            int id,
            String nombre,
            String tipo,
            String clase,
            String valor,
            int longitud,
            int ambito) {
        this.id = id;
        this.nombre = nombre;
        this.tipo = tipo;
        this.clase = clase;
        this.valor = valor;
        this.longitud = longitud;
        this.ambito = ambito;
    }
}
