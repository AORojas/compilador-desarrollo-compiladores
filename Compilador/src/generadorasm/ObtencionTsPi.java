package generadorasm;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ObtencionTsPi {
    private static final Pattern ELEMENTO_POLACA = Pattern.compile("\\[[^\\]]+\\]|->\\d+");

    private ObtencionTsPi() {
    }

    public static Programa leer(String archivo) throws IOException {
        String contenido = new String(
            Files.readAllBytes(Paths.get(archivo)), StandardCharsets.UTF_8);
        String[] lineas = contenido.split("\\R");
        List<Simbolo> simbolos = new ArrayList<Simbolo>();
        List<String> polaca = new ArrayList<String>();
        Map<Integer, Integer> entradasFunciones = new LinkedHashMap<Integer, Integer>();
        Map<Integer, Integer> parametrosFunciones = new LinkedHashMap<Integer, Integer>();
        boolean tabla = false;
        boolean codigo = false;
        boolean funciones = false;
        boolean encontroTabla = false;
        boolean encontroPolaca = false;

        for (String linea : lineas) {
            if (linea.length() > 0 && linea.charAt(0) == '\uFEFF') {
                linea = linea.substring(1);
            }
            String texto = linea.trim();
            if (texto.startsWith("Tabla de símbolos:") || texto.startsWith("Tabla de simbolos:")) {
                tabla = true;
                codigo = false;
                funciones = false;
                encontroTabla = true;
                continue;
            }
            if (texto.startsWith("Polaca Inversa:")) {
                tabla = false;
                codigo = true;
                funciones = false;
                encontroPolaca = true;
                agregarPolaca(texto.substring("Polaca Inversa:".length()), polaca);
                continue;
            }
            if (texto.startsWith("Entradas de funciones:")) {
                tabla = false;
                codigo = false;
                funciones = true;
                continue;
            }
            if (tabla && texto.startsWith("ID |")) {
                continue;
            }
            if (tabla && texto.contains("|")) {
                String[] columnas = texto.split("\\|", -1);
                if (columnas.length < 7) {
                    throw new IOException("Fila incompleta en la tabla de símbolos: " + texto);
                }
                simbolos.add(new Simbolo(
                    entero(columnas[0], "ID"),
                    columnas[1].trim(),
                    columnas[2].trim(),
                    columnas[3].trim(),
                    columnas[4].trim(),
                    entero(columnas[5], "Longitud"),
                    entero(columnas[6], "Ámbito")));
                continue;
            }
            if (funciones && texto.startsWith("ID |")) {
                continue;
            }
            if (funciones && texto.contains("|")) {
                String[] columnas = texto.split("\\|", -1);
                if (columnas.length < 2) {
                    throw new IOException("Fila incompleta en entradas de funciones: " + texto);
                }
                int id = entero(columnas[0], "ID de función");
                int posicion = entero(columnas[1], "Índice de función");
                if (entradasFunciones.put(id, posicion) != null) {
                    throw new IOException("ID de función duplicado en entradas: " + id);
                }
                if (columnas.length > 2 && !columnas[2].trim().isEmpty()) {
                    parametrosFunciones.put(id, entero(columnas[2], "ID del parámetro"));
                }
                continue;
            }
            if (codigo && !texto.isEmpty()) {
                agregarPolaca(texto, polaca);
            }
        }

        if (!encontroTabla || !encontroPolaca) {
            throw new IOException(
                "El archivo debe contener las secciones 'Tabla de símbolos:' y 'Polaca Inversa:'.");
        }
        return new Programa(simbolos, polaca, entradasFunciones, parametrosFunciones);
    }

    private static void agregarPolaca(String texto, List<String> elementos) throws IOException {
        Matcher matcher = ELEMENTO_POLACA.matcher(texto);
        int cursor = 0;
        while (matcher.find()) {
            if (!texto.substring(cursor, matcher.start()).trim().isEmpty()) {
                throw new IOException("Elemento inválido en la Polaca Inversa: " + texto);
            }
            elementos.add(matcher.group());
            cursor = matcher.end();
        }
        if (!texto.substring(cursor).trim().isEmpty()) {
            throw new IOException("Elemento inválido en la Polaca Inversa: " + texto);
        }
    }

    private static int entero(String valor, String nombreCampo) throws IOException {
        try {
            return Integer.parseInt(valor.trim());
        } catch (NumberFormatException error) {
            throw new IOException("Valor inválido para " + nombreCampo + ": " + valor.trim(), error);
        }
    }

    public static final class Programa {
        private final List<Simbolo> simbolos;
        private final List<String> polaca;
        private final Map<Integer, Integer> entradasFunciones;
        private final Map<Integer, Integer> parametrosFunciones;

        private Programa(
                List<Simbolo> simbolos,
                List<String> polaca,
                Map<Integer, Integer> entradasFunciones,
                Map<Integer, Integer> parametrosFunciones) {
            this.simbolos = new ArrayList<Simbolo>(simbolos);
            this.polaca = new ArrayList<String>(polaca);
            this.entradasFunciones = new LinkedHashMap<Integer, Integer>(entradasFunciones);
            this.parametrosFunciones = new LinkedHashMap<Integer, Integer>(parametrosFunciones);
        }

        public List<Simbolo> getSimbolos() {
            return new ArrayList<Simbolo>(simbolos);
        }

        public List<String> getPolaca() {
            return new ArrayList<String>(polaca);
        }

        public Map<Integer, Integer> getEntradasFunciones() {
            return new LinkedHashMap<Integer, Integer>(entradasFunciones);
        }

        public Map<Integer, Integer> getParametrosFunciones() {
            return new LinkedHashMap<Integer, Integer>(parametrosFunciones);
        }
    }

    public static final class Simbolo {
        private final int id;
        private final String nombre;
        private final String tipo;
        private final String clase;
        private final String valor;
        private final int longitud;
        private final int ambito;

        private Simbolo(
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

        public int getId() { return id; }
        public String getNombre() { return nombre; }
        public String getTipo() { return tipo; }
        public String getClase() { return clase; }
        public String getValor() { return valor; }
        public int getLongitud() { return longitud; }
        public int getAmbito() { return ambito; }
    }

    public static void main(String[] args) throws Exception {
        String archivo = args.length > 0 ? args[0] : "entrada.e";
        Programa programa = leer(archivo);
        System.out.println("Símbolos: " + programa.getSimbolos().size());
        System.out.println("Elementos de Polaca Inversa: " + programa.getPolaca().size());
    }
}
