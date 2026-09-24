package compilador.lexico.semantica;

public final class GestorFuncionesSemanticas {
    private final StringBuilder bufferLetras = new StringBuilder();
    private final StringBuilder bufferDigitos = new StringBuilder();
    private final StringBuilder bufferTexto = new StringBuilder();

    public void limpiarLetras() { bufferLetras.setLength(0); }
    public void agregarLetra(char c) { bufferLetras.append(c); }
    public String letras() { return bufferLetras.toString(); }

    public void limpiarDigitos() { bufferDigitos.setLength(0); }
    public void agregarDigito(char c) { bufferDigitos.append(c); }
    public String digitos() { return bufferDigitos.toString(); }

    public void limpiarTexto() { bufferTexto.setLength(0); }
    public void agregarTexto(char c) { bufferTexto.append(c); }
    public String texto() { return bufferTexto.toString(); }

    public void ejecutarAccion(int accion, int c, int estado, int evento) {
        switch (accion) {
            case 1 -> iniciarLetras((char) c);
            case 2 -> iniciarDigitos((char) c);
            case 3 -> abrirTexto(estado, (char) c);
            case 4 -> ignorarBlancos();
            case 5 -> acumularLetras((char) c);
            case 6 -> acumularDigitos((char) c);
            case 7 -> { /* la resolución del token se hace en el analizador */ }
            case 8 -> finLinea();
            case 9 -> validarConstante();
            case 10 -> tokenDirecto();
            case 11 -> cerrarTexto();
            case 12 -> manejarComentario();
            case 13 -> errorLexico();
            default -> { }
        }
    }

    public void iniciarLetras(char c) {
        limpiarLetras();
        agregarLetra(c);
    }

    public void iniciarDigitos(char c) {
        limpiarDigitos();
        agregarDigito(c);
    }

    public void abrirTexto(int estado, char c) {
        if (estado == 0) {
            limpiarTexto();
        } else {
            agregarTexto(c);
        }
    }

    public void ignorarBlancos() {
        // No acumula nada; el blanco se descarta.
    }

    public void acumularLetras(char c) {
        agregarLetra(c);
    }

    public void acumularDigitos(char c) {
        agregarDigito(c);
    }

    public void cerrarTexto() {
        // La validación final ocurre en el analizador.
    }

    public void manejarComentario() {
        // El comentario se ignora en el analizador.
    }

    public void finLinea() {
        // Se usa para marcar el cierre de una línea de código.
    }

    public void validarConstante() {
        // La validación real se hace en el analizador al cerrar el número.
    }

    public void tokenDirecto() {
        // El token directo se resuelve en el analizador.
    }

    public void errorLexico() {
        // Marca el error para que el analizador lo reporte.
    }
}
