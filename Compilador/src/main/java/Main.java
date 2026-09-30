import compilador.lexico.AnalizadorLexico;
import compilador.lexico.modelo.TipoToken;
import compilador.lexico.modelo.Token;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {
    public static void main(String[] args) {
        Path archivo = args.length > 0 ? Path.of(args[0]) : Path.of("src/main/resources/prueba.gau");

        System.out.println("=== Analizador Lexico GAUCHO ===");
        System.out.println("Archivo: " + archivo.toAbsolutePath());

        try (var reader = Files.newBufferedReader(archivo, StandardCharsets.UTF_8)) {
            AnalizadorLexico lexico = new AnalizadorLexico(reader);
            Token token;
            do {
                token = lexico.proximoToken();
                System.out.println(token);
            } while (token.getCodigo() != TipoToken.EOF);

            System.out.println();
            System.out.println("=== Tabla de simbolos ===");
            lexico.getTablaSimbolos().todos().forEach((lexema, codigo) ->
                    System.out.println(lexema + " -> " + codigo));
        } catch (IOException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }
}
