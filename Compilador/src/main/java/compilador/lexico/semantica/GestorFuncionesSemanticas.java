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
}
