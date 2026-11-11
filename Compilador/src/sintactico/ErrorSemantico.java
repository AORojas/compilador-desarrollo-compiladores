package sintactico;

public final class ErrorSemantico {
    private final String codigo;
    private final String descripcion;
    private final int linea;
    private final int columna;
    private final String identificador;

    public ErrorSemantico(
            String codigo,
            String descripcion,
            int linea,
            int columna,
            String identificador) {
        this.codigo = codigo;
        this.descripcion = descripcion;
        this.linea = linea;
        this.columna = columna;
        this.identificador = identificador;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public int getLinea() {
        return linea;
    }

    public int getColumna() {
        return columna;
    }

    public String getIdentificador() {
        return identificador;
    }

    @Override
    public String toString() {
        return codigo + " - Línea " + linea + ", columna " + columna + ": "
            + descripcion
            + (identificador == null ? "" : " (" + identificador + ")");
    }
}
