import compilador.lexico.AnalizadorLexico;
import compilador.lexico.modelo.TipoToken;
import org.junit.jupiter.api.Test;

import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AnalizadorLexicoTest {
    @Test
    void debeReconocerPalabrasReservadasYIdentificadoresSinDuplicarLetras() throws Exception {
        AnalizadorLexico lexico = new AnalizadorLexico(new StringReader("entero principal()\n"));

        assertEquals(TipoToken.ENTERO, lexico.proximoToken().getCodigo());
        assertEquals(TipoToken.PRINCIPAL, lexico.proximoToken().getCodigo());
        assertEquals(TipoToken.PAREN_IZQ, lexico.proximoToken().getCodigo());
        assertEquals(TipoToken.PAREN_DER, lexico.proximoToken().getCodigo());
        assertEquals(TipoToken.FIN_DE_LINEA, lexico.proximoToken().getCodigo());
    }
}
