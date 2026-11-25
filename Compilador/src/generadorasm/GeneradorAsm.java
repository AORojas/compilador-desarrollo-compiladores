package generadorasm;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class GeneradorAsm {
    private final ObtencionTsPi.Programa programa;
    private final Map<Integer, ObtencionTsPi.Simbolo> simbolos =
        new LinkedHashMap<Integer, ObtencionTsPi.Simbolo>();
    private final Map<Integer, String> etiquetas = new HashMap<Integer, String>();
    private final Map<Integer, String> etiquetasFunciones = new HashMap<Integer, String>();
    private final Map<Integer, Integer> entradasFunciones = new LinkedHashMap<Integer, Integer>();
    private final Map<Integer, Integer> parametrosFunciones = new LinkedHashMap<Integer, Integer>();
    private final Set<Integer> destinos = new HashSet<Integer>();
    private final List<String> lineas = new ArrayList<String>();
    private final List<String> tiposPila = new ArrayList<String>();
    private String ultimoOperadorRelacional;
    private String funcionActual;
    private int indiceActual;

    public GeneradorAsm(ObtencionTsPi.Programa programa) throws IOException {
        if (programa == null) {
            throw new IllegalArgumentException("El programa de entrada es obligatorio.");
        }
        this.programa = programa;
        prepararSimbolos();
        prepararFunciones();
        prepararDestinos();
    }

    public String generar() throws IOException {
        emitirDatos();
        lineas.add("");
        lineas.add(".text");
        lineas.add("    .global main");
        lineas.add("    .extern printf");
        lineas.add("    .extern strcpy");
        lineas.add("    .extern strcmp");
        lineas.add("    .extern strcat");
        lineas.add("    .extern ExitProcess");
        lineas.add("");

        List<String> polaca = programa.getPolaca();
        Map<Integer, List<Integer>> funcionesPorIndice = funcionesPorIndice();
        for (indiceActual = 0; indiceActual < polaca.size(); indiceActual++) {
            if (destinos.contains(indiceActual)) {
                lineas.add("etq_" + indiceActual + ":");
            }
            iniciarFunciones(funcionesPorIndice.get(indiceActual));
            traducir(polaca, indiceActual);
        }
        if (destinos.contains(polaca.size())) {
            lineas.add("etq_" + polaca.size() + ":");
        }
        iniciarFunciones(funcionesPorIndice.get(polaca.size()));
        finalizarFuncion();
        return unir(lineas);
    }

    public void escribir(String archivo) throws IOException {
        Files.write(Paths.get(archivo), generar().getBytes(StandardCharsets.UTF_8));
    }

    private void prepararSimbolos() throws IOException {
        Map<String, Integer> repeticiones = new HashMap<String, Integer>();
        for (ObtencionTsPi.Simbolo simbolo : programa.getSimbolos()) {
            if (simbolos.put(simbolo.getId(), simbolo) != null) {
                throw new IOException("ID de símbolo duplicado: " + simbolo.getId());
            }
            Integer cantidad = repeticiones.get(simbolo.getNombre());
            repeticiones.put(simbolo.getNombre(), cantidad == null ? 1 : cantidad + 1);
        }
        for (ObtencionTsPi.Simbolo simbolo : simbolos.values()) {
            String tipo = normalizarTipo(simbolo.getTipo());
            String prefijo = "constante".equals(simbolo.getClase())
                ? ("string".equals(tipo) ? "_str" : "_cte")
                : "_";
            String nombre = "constante".equals(simbolo.getClase())
                ? Integer.toString(simbolo.getId())
                : limpiar(simbolo.getNombre());
            if ("variable".equals(simbolo.getClase())
                    && repeticiones.get(simbolo.getNombre()) > 1) {
                nombre = "v" + simbolo.getId() + "_" + nombre;
            }
            etiquetas.put(simbolo.getId(), prefijo + nombre);
        }
    }

    private void prepararFunciones() throws IOException {
        int principalId = -1;
        for (ObtencionTsPi.Simbolo simbolo : simbolos.values()) {
            if (!"funcion".equals(simbolo.getClase())) {
                continue;
            }
            String etiqueta = "principal".equals(simbolo.getNombre())
                ? "main"
                : "_func_" + limpiar(simbolo.getNombre());
            etiquetasFunciones.put(simbolo.getId(), etiqueta);
            if ("principal".equals(simbolo.getNombre())) {
                principalId = simbolo.getId();
            }
        }
        if (principalId < 0) {
            throw new IOException("La tabla de símbolos no contiene la función 'principal'.");
        }

        entradasFunciones.putAll(programa.getEntradasFunciones());
        if (entradasFunciones.isEmpty()) {
            entradasFunciones.put(principalId, 0);
            inferirEntradasAuxiliares(principalId);
        }
        for (Map.Entry<Integer, Integer> entrada : entradasFunciones.entrySet()) {
            if (!etiquetasFunciones.containsKey(entrada.getKey())) {
                throw new IOException("Punto de entrada para función inexistente: " + entrada.getKey());
            }
            if (entrada.getValue() < 0 || entrada.getValue() > programa.getPolaca().size()) {
                throw new IOException("Índice de función fuera de rango: " + entrada.getValue());
            }
        }
        for (Integer idFuncion : etiquetasFunciones.keySet()) {
            if (!entradasFunciones.containsKey(idFuncion)) {
                throw new IOException("Falta el punto de entrada de la función: " + idFuncion);
            }
        }
        prepararParametrosFunciones();
    }

    private void inferirEntradasAuxiliares(int principalId) throws IOException {
        List<Integer> auxiliares = new ArrayList<Integer>();
        for (Integer id : etiquetasFunciones.keySet()) {
            if (id.intValue() != principalId) {
                auxiliares.add(id);
            }
        }
        if (auxiliares.isEmpty()) {
            return;
        }
        if (auxiliares.size() > 1) {
            throw new IOException(
                "El reporte omite las entradas de varias funciones auxiliares; "
                    + "no se pueden inferir sus posiciones sin ambigüedad.");
        }

        List<String> polaca = programa.getPolaca();
        int entradaAuxiliar = -1;
        for (int i = 0; i < polaca.size(); i++) {
            if ("[BI]".equals(polaca.get(i)) && i + 1 < polaca.size()) {
                String destino = polaca.get(i + 1);
                if (destino.startsWith("->")) {
                    int indiceDestino = leerDestino(destino);
                    if (indiceDestino < polaca.size()
                            && (entradaAuxiliar < 0 || indiceDestino > entradaAuxiliar)) {
                        entradaAuxiliar = indiceDestino;
                    }
                }
            }
        }

        if (entradaAuxiliar < 0) {
            throw new IOException(
                "El reporte no indica la posición de las funciones auxiliares "
                    + "y no contiene un [BI] que permita inferir su entrada.");
        }
        entradasFunciones.put(auxiliares.get(0), entradaAuxiliar);
    }

    private void prepararParametrosFunciones() throws IOException {
        parametrosFunciones.putAll(programa.getParametrosFunciones());
        List<String> polaca = programa.getPolaca();
        for (Map.Entry<Integer, String> funcion : etiquetasFunciones.entrySet()) {
            int idFuncion = funcion.getKey();
            if ("main".equals(funcion.getValue())) {
                continue;
            }

            Integer parametro = parametrosFunciones.get(idFuncion);
            if (parametro == null) {
                int inferido = inferirParametro(idFuncion, polaca);
                if (inferido >= 0) {
                    parametro = Integer.valueOf(inferido);
                    parametrosFunciones.put(idFuncion, parametro);
                }
            }
            if (parametro != null) {
                ObtencionTsPi.Simbolo simboloParametro = simbolos.get(parametro);
                if (simboloParametro == null
                        || !"variable".equals(simboloParametro.getClase())
                        || !"entero".equals(normalizarTipo(simboloParametro.getTipo()))) {
                    throw new IOException(
                        "El parámetro de la función " + idFuncion
                            + " debe ser una variable entera existente.");
                }
            }
        }
    }

    private int inferirParametro(int idFuncion, List<String> polaca) throws IOException {
        Integer entrada = entradasFunciones.get(idFuncion);
        if (entrada == null) {
            throw new IOException("No se puede inferir el parámetro sin entrada de función.");
        }
        int inicio = entrada.intValue();
        int fin = polaca.size();
        for (Integer otraEntrada : entradasFunciones.values()) {
            if (otraEntrada.intValue() > inicio && otraEntrada.intValue() < fin) {
                fin = otraEntrada.intValue();
            }
        }

        int primerIdVariable = Integer.MAX_VALUE;
        boolean parametroLeidoAntesDeAsignar = false;
        Set<Integer> vistos = new HashSet<Integer>();
        for (int i = inicio; i < fin; i++) {
            String token = polaca.get(i);
            if (!token.startsWith("[") || !esId(token.substring(1, token.length() - 1))) {
                continue;
            }
            int id = Integer.parseInt(token.substring(1, token.length() - 1));
            ObtencionTsPi.Simbolo simbolo = simbolos.get(id);
            if (simbolo == null || !"variable".equals(simbolo.getClase())
                    || !vistos.add(id) || id >= primerIdVariable) {
                continue;
            }
            boolean destinoAsignacion = i + 1 < fin && "[:=]".equals(polaca.get(i + 1));
            primerIdVariable = id;
            parametroLeidoAntesDeAsignar = !destinoAsignacion;
        }
        return parametroLeidoAntesDeAsignar ? primerIdVariable : -1;
    }

    private void prepararDestinos() throws IOException {
        List<String> polaca = programa.getPolaca();
        for (int i = 0; i < polaca.size(); i++) {
            String token = polaca.get(i);
            if ("[BF]".equals(token) || "[BI]".equals(token)) {
                if (i + 1 >= polaca.size() || !polaca.get(i + 1).startsWith("->")) {
                    throw new IOException("Salto sin destino en la posición " + i + ".");
                }
                int destino = leerDestino(polaca.get(i + 1));
                if (destino > polaca.size()) {
                    throw new IOException("Destino de salto fuera de rango: " + destino);
                }
                destinos.add(destino);
            } else if (token.startsWith("->")) {
                leerDestino(token);
            }
        }
    }

    private void emitirDatos() throws IOException {
        lineas.add(".intel_syntax noprefix");
        lineas.add("");
        lineas.add(".data");
        for (ObtencionTsPi.Simbolo simbolo : simbolos.values()) {
            String tipo = normalizarTipo(simbolo.getTipo());
            if ("variable".equals(simbolo.getClase())) {
                if ("entero".equals(tipo)) {
                    lineas.add("    " + etiquetas.get(simbolo.getId()) + ": .quad 0");
                } else if ("string".equals(tipo)) {
                    lineas.add("    " + etiquetas.get(simbolo.getId()) + ": .space 256");
                } else {
                    throw new IOException("Tipo de variable no soportado: " + simbolo.getTipo());
                }
            } else if ("constante".equals(simbolo.getClase())) {
                if ("entero".equals(tipo)) {
                    lineas.add("    " + etiquetas.get(simbolo.getId()) + ": .quad "
                        + validarEntero(simbolo.getValor(), simbolo.getId()));
                } else if ("string".equals(tipo)) {
                    lineas.add("    " + etiquetas.get(simbolo.getId()) + ": .asciz \""
                        + escapar(descomillar(simbolo.getValor())) + "\"");
                } else {
                    throw new IOException("Tipo de constante no soportado: " + simbolo.getTipo());
                }
            } else if (!"funcion".equals(simbolo.getClase())) {
                throw new IOException("Clase de símbolo no soportada: " + simbolo.getClase());
            }
        }
        lineas.add("    _buf_str: .space 256");
        lineas.add("    fmt_int: .asciz \"%lld\\n\"");
        lineas.add("    fmt_str: .asciz \"%s\\n\"");
    }

    private void traducir(List<String> polaca, int indice) throws IOException {
        String token = polaca.get(indice);
        if (!token.startsWith("[")) {
            if (!token.startsWith("->")) {
                throw new IOException("Token inválido en la posición " + indice + ": " + token);
            }
            return;
        }
        String contenido = token.substring(1, token.length() - 1);
        if (esId(contenido)) {
            emitirId(Integer.parseInt(contenido), polaca, indice);
            return;
        }
        switch (contenido) {
            case "+": emitirSuma(); break;
            case "-": emitirAritmetica("sub rax, rbx"); break;
            case "*": emitirAritmetica("imul rax, rbx"); break;
            case "/": emitirDivision(); break;
            case ":=": emitirAsignacion(polaca, indice); break;
            case ">":
            case "<":
            case ">=":
            case "<=":
            case "==":
            case "<>":
            case "!=":
                emitirComparacion(contenido);
                break;
            case "BF": emitirSaltoFalso(polaca, indice); break;
            case "BI": emitirSaltoIncondicional(polaca, indice); break;
            case "PRINT": emitirPrint(false); break;
            case "PRINT_TEXT":
            case "PRINT_STR": emitirPrint(true); break;
            case "CALL": emitirLlamada(polaca, indice); break;
            case "RET": emitirRetorno(); break;
            default: throw new IOException("Operador de Polaca no soportado: " + token);
        }
    }

    private void emitirId(int id, List<String> polaca, int indice) throws IOException {
        ObtencionTsPi.Simbolo simbolo = simbolos.get(id);
        if (simbolo == null) {
            throw new IOException("La Polaca referencia un ID inexistente: " + id);
        }
        if ("funcion".equals(simbolo.getClase())) {
            if (indice + 1 < polaca.size() && "[CALL]".equals(polaca.get(indice + 1))) {
                return;
            }
            throw new IOException("Referencia a función fuera de una llamada: " + id);
        }
        String tipo = normalizarTipo(simbolo.getTipo());
        if ("string".equals(tipo)) {
            lineas.add("    lea rax, [" + etiquetas.get(id) + "]");
            lineas.add("    push rax");
        } else if ("entero".equals(tipo)) {
            lineas.add("    push qword ptr [" + etiquetas.get(id) + "]");
        } else {
            throw new IOException("Tipo de operando no soportado: " + simbolo.getTipo());
        }
        tiposPila.add(tipo);
    }

    private void emitirSuma() throws IOException {
        String segundo = desapilarTipo("+");
        String primero = desapilarTipo("+");
        if ("string".equals(primero) && "string".equals(segundo)) {
            lineas.add("    pop rbx");
            lineas.add("    pop rax");
            lineas.add("    lea rcx, [_buf_str]");
            lineas.add("    mov rdx, rax");
            lineas.add("    call strcpy");
            lineas.add("    lea rcx, [_buf_str]");
            lineas.add("    mov rdx, rbx");
            lineas.add("    call strcat");
            lineas.add("    lea rax, [_buf_str]");
            lineas.add("    push rax");
            tiposPila.add("string");
        } else if ("entero".equals(primero) && "entero".equals(segundo)) {
            lineas.add("    pop rbx");
            lineas.add("    pop rax");
            lineas.add("    add rax, rbx");
            lineas.add("    push rax");
            tiposPila.add("entero");
        } else {
            throw new IOException("La suma requiere dos operandos del mismo tipo.");
        }
    }

    private void emitirAritmetica(String instruccion) throws IOException {
        exigirEnteros(desapilarTipo(instruccion), desapilarTipo(instruccion), instruccion);
        lineas.add("    pop rbx");
        lineas.add("    pop rax");
        lineas.add("    " + instruccion);
        lineas.add("    push rax");
        tiposPila.add("entero");
    }

    private void emitirDivision() throws IOException {
        exigirEnteros(desapilarTipo("/"), desapilarTipo("/"), "/");
        lineas.add("    pop rbx");
        lineas.add("    pop rax");
        lineas.add("    cqo");
        lineas.add("    idiv rbx");
        lineas.add("    push rax");
        tiposPila.add("entero");
    }

    private void emitirAsignacion(List<String> polaca, int indice) throws IOException {
        if (indice == 0) {
            throw new IOException("La asignación no contiene un ID de destino.");
        }
        String tokenDestino = polaca.get(indice - 1);
        if (!tokenDestino.startsWith("[")
                || !esId(tokenDestino.substring(1, tokenDestino.length() - 1))) {
            throw new IOException("La asignación no contiene un ID de destino.");
        }
        int idDestino = Integer.parseInt(tokenDestino.substring(1, tokenDestino.length() - 1));
        ObtencionTsPi.Simbolo destino = simbolos.get(idDestino);
        if (destino == null || !"variable".equals(destino.getClase())) {
            throw new IOException("El destino de la asignación no es una variable: " + idDestino);
        }
        String tipoDestino = normalizarTipo(destino.getTipo());
        String tipoValor = desapilarTipo(":=");
        desapilarTipo(":=");
        if (!tipoDestino.equals(tipoValor)) {
            throw new IOException("Los tipos del valor y destino de asignación no coinciden.");
        }
        if ("entero".equals(tipoDestino)) {
            lineas.add("    pop rbx");
            lineas.add("    pop rax");
            lineas.add("    mov [" + etiquetas.get(idDestino) + "], rax");
        } else {
            lineas.add("    pop rcx");
            lineas.add("    pop rdx");
            lineas.add("    lea rcx, [" + etiquetas.get(idDestino) + "]");
            lineas.add("    call strcpy");
        }
    }

    private void emitirComparacion(String operador) throws IOException {
        String segundo = desapilarTipo(operador);
        String primero = desapilarTipo(operador);
        if (!primero.equals(segundo)) {
            throw new IOException("Los operandos de la comparación tienen tipos distintos.");
        }
        ultimoOperadorRelacional = operador;
        if ("string".equals(primero)) {
            if (!"==".equals(operador) && !"<>".equals(operador) && !"!=".equals(operador)) {
                throw new IOException("Las cadenas solo admiten comparaciones == y !=.");
            }
            lineas.add("    pop rdx");
            lineas.add("    pop rcx");
            lineas.add("    call strcmp");
            lineas.add("    cmp eax, 0");
        } else {
            lineas.add("    pop rbx");
            lineas.add("    pop rax");
            lineas.add("    cmp rax, rbx");
        }
    }

    private void emitirSaltoFalso(List<String> polaca, int indice) throws IOException {
        if (ultimoOperadorRelacional == null || indice + 1 >= polaca.size()) {
            throw new IOException("BF no está precedido por una comparación válida.");
        }
        int destino = leerDestino(polaca.get(indice + 1));
        String salto;
        switch (ultimoOperadorRelacional) {
            case ">": salto = "jle"; break;
            case "<": salto = "jge"; break;
            case ">=": salto = "jl"; break;
            case "<=": salto = "jg"; break;
            case "==": salto = "jne"; break;
            case "<>":
            case "!=": salto = "je"; break;
            default: throw new IOException("Operador relacional no soportado: " + ultimoOperadorRelacional);
        }
        lineas.add("    " + salto + " etq_" + destino);
    }

    private void emitirSaltoIncondicional(List<String> polaca, int indice) throws IOException {
        if (indice + 1 >= polaca.size()) {
            throw new IOException("BI sin destino.");
        }
        lineas.add("    jmp etq_" + leerDestino(polaca.get(indice + 1)));
    }

    private void emitirPrint(boolean texto) throws IOException {
        String tipo = desapilarTipo(texto ? "PRINT_TEXT" : "PRINT");
        if (texto && !"string".equals(tipo)) {
            throw new IOException("PRINT_TEXT requiere un operando string.");
        }
        lineas.add("    pop rdx");
        lineas.add("    lea rcx, [" + ("string".equals(tipo) ? "fmt_str" : "fmt_int") + "]");
        lineas.add("    call printf");
    }

    private void emitirLlamada(List<String> polaca, int indice) throws IOException {
        if (indice == 0) {
            throw new IOException("CALL no contiene una referencia a función.");
        }
        String token = polaca.get(indice - 1);
        if (!token.startsWith("[") || !esId(token.substring(1, token.length() - 1))) {
            throw new IOException("CALL no contiene un ID de función válido.");
        }
        int id = Integer.parseInt(token.substring(1, token.length() - 1));
        ObtencionTsPi.Simbolo funcion = simbolos.get(id);
        String etiqueta = etiquetasFunciones.get(id);
        if (funcion == null || etiqueta == null || !"funcion".equals(funcion.getClase())) {
            throw new IOException("CALL referencia una función inexistente: " + id);
        }
        if (!entradasFunciones.containsKey(id)) {
            throw new IOException(
                "CALL sin índice de entrada para '" + funcion.getNombre()
                    + "'; el reporte no incluye esa información.");
        }
        if ("main".equals(etiqueta)) {
            throw new IOException("No se puede llamar a la función 'principal'.");
        }
        exigirEnteros(desapilarTipo("CALL"), "entero", "CALL");
        lineas.add("    pop rcx");
        lineas.add("    call " + etiqueta);
        lineas.add("    push rax");
        tiposPila.add("entero");
    }

    private void emitirRetorno() throws IOException {
        desapilarTipo("RET");
        lineas.add("    pop rax");
        if ("main".equals(funcionActual)) {
            lineas.add("    add rsp, 40");
            lineas.add("    ret");
        } else {
            lineas.add("    add rsp, 32");
            lineas.add("    pop rbp");
            lineas.add("    ret");
        }
    }

    private void iniciarFunciones(List<Integer> funciones) throws IOException {
        if (funciones == null) {
            return;
        }
        for (Integer id : funciones) {
            finalizarFuncion();
            String etiqueta = etiquetasFunciones.get(id);
            if (etiqueta == null) {
                throw new IOException("Entrada de función inexistente: " + id);
            }
            funcionActual = etiqueta;
            tiposPila.clear();
            ultimoOperadorRelacional = null;
            lineas.add("");
            lineas.add(etiqueta + ":");
            if ("main".equals(etiqueta)) {
                lineas.add("    sub rsp, 40");
            } else {
                lineas.add("    push rbp");
                lineas.add("    mov rbp, rsp");
                lineas.add("    sub rsp, 32");
                Integer parametro = parametrosFunciones.get(id);
                if (parametro != null) {
                    String etiquetaParametro = etiquetas.get(parametro);
                    if (etiquetaParametro == null) {
                        throw new IOException("ID de parámetro inexistente: " + parametro);
                    }
                    lineas.add("    mov [" + etiquetaParametro + "], rcx");
                }
            }
        }
    }

    private void finalizarFuncion() {
        if (funcionActual == null) {
            return;
        }
        if ("main".equals(funcionActual)) {
            lineas.add("    xor ecx, ecx");
            lineas.add("    call ExitProcess");
        } else {
            lineas.add("    xor eax, eax");
            lineas.add("    add rsp, 32");
            lineas.add("    pop rbp");
            lineas.add("    ret");
        }
        funcionActual = null;
    }

    private Map<Integer, List<Integer>> funcionesPorIndice() {
        Map<Integer, List<Integer>> resultado = new LinkedHashMap<Integer, List<Integer>>();
        for (Map.Entry<Integer, Integer> entrada : entradasFunciones.entrySet()) {
            List<Integer> funciones = resultado.get(entrada.getValue());
            if (funciones == null) {
                funciones = new ArrayList<Integer>();
                resultado.put(entrada.getValue(), funciones);
            }
            funciones.add(entrada.getKey());
        }
        return resultado;
    }

    private String desapilarTipo(String operador) throws IOException {
        if (tiposPila.isEmpty()) {
            throw new IOException("Faltan operandos para " + operador + ".");
        }
        return tiposPila.remove(tiposPila.size() - 1);
    }

    private static void exigirEnteros(String primero, String segundo, String operador)
            throws IOException {
        if (!"entero".equals(primero) || !"entero".equals(segundo)) {
            throw new IOException(operador + " requiere operandos enteros.");
        }
    }

    private static int leerDestino(String token) throws IOException {
        try {
            if (!token.startsWith("->")) {
                throw new NumberFormatException();
            }
            return Integer.parseInt(token.substring(2));
        } catch (NumberFormatException error) {
            throw new IOException("Destino de salto inválido: " + token, error);
        }
    }

    private static boolean esId(String texto) {
        if (texto.isEmpty()) {
            return false;
        }
        for (int i = 0; i < texto.length(); i++) {
            if (!Character.isDigit(texto.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private static String normalizarTipo(String tipo) {
        return "texto".equals(tipo) ? "string" : tipo;
    }

    private static String validarEntero(String valor, int id) throws IOException {
        try {
            return Long.toString(Long.parseLong(valor.trim()));
        } catch (NumberFormatException error) {
            throw new IOException("Literal entero inválido para el símbolo " + id + ": " + valor, error);
        }
    }

    private static String descomillar(String valor) {
        if (valor.length() >= 2 && valor.charAt(0) == '"' && valor.charAt(valor.length() - 1) == '"') {
            return valor.substring(1, valor.length() - 1);
        }
        return valor;
    }

    private static String escapar(String valor) {
        StringBuilder resultado = new StringBuilder();
        for (int i = 0; i < valor.length(); i++) {
            char caracter = valor.charAt(i);
            switch (caracter) {
                case '\\': resultado.append("\\\\"); break;
                case '"': resultado.append("\\\""); break;
                case '\n': resultado.append("\\n"); break;
                case '\r': resultado.append("\\r"); break;
                case '\t': resultado.append("\\t"); break;
                default: resultado.append(caracter);
            }
        }
        return resultado.toString();
    }

    private static String limpiar(String nombre) {
        StringBuilder resultado = new StringBuilder();
        for (int i = 0; i < nombre.length(); i++) {
            char caracter = nombre.charAt(i);
            resultado.append(Character.isLetterOrDigit(caracter) || caracter == '_'
                ? caracter : '_');
        }
        return resultado.length() == 0 ? "simbolo" : resultado.toString();
    }

    private static String unir(List<String> lineas) {
        StringBuilder resultado = new StringBuilder();
        for (String linea : lineas) {
            resultado.append(linea).append('\n');
        }
        return resultado.toString();
    }

    public static void main(String[] args) throws Exception {
        String entrada = args.length > 0 ? args[0] : "entrada.e";
        String salida = args.length > 1 ? args[1] : "salida.s";
        ObtencionTsPi.Programa programa = ObtencionTsPi.leer(entrada);
        new GeneradorAsm(programa).escribir(salida);
        System.out.println("Assembler generado: " + salida);
    }
}
