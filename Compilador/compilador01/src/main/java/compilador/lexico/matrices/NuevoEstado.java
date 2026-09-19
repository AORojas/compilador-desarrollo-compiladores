package compilador.lexico.matrices;

import compilador.lexico.Estado;

public final class NuevoEstado {
    private NuevoEstado() {
    }

    public static final Estado[][] MATRIZ = new Estado[][] {
        { Estado.E1, Estado.E2, Estado.E3, Estado.E5, Estado.E8, Estado.E10, Estado.E16, Estado.E13, Estado.E14, Estado.E15, Estado.E17, Estado.E18, Estado.E19, Estado.E20, Estado.E21, Estado.E22, Estado.E0, Estado.EF },
        { Estado.E1, Estado.E1, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF },
        { Estado.EF, Estado.E2, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF },
        { Estado.EF, Estado.EF, Estado.E4, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF },
        { Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF },
        { Estado.EF, Estado.EF, Estado.E7, Estado.EF, Estado.E6, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF },
        { Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF },
        { Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF },
        { Estado.EF, Estado.EF, Estado.E9, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF },
        { Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF },
        { Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.E11, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF },
        { Estado.E11, Estado.E11, Estado.E11, Estado.E11, Estado.E11, Estado.E11, Estado.E12, Estado.E11, Estado.E11, Estado.E11, Estado.E11, Estado.E11, Estado.E11, Estado.E11, Estado.E11, Estado.E11, Estado.E11, Estado.E11 },
        { Estado.E11, Estado.E11, Estado.E11, Estado.E11, Estado.E11, Estado.E0, Estado.E11, Estado.E11, Estado.E11, Estado.E11, Estado.E11, Estado.E11, Estado.E11, Estado.E11, Estado.E11, Estado.E11, Estado.E11, Estado.E11 },
        { Estado.E13, Estado.E13, Estado.E13, Estado.E13, Estado.E13, Estado.E13, Estado.E13, Estado.EF, Estado.E13, Estado.E13, Estado.E13, Estado.E13, Estado.E13, Estado.E13, Estado.E13, Estado.E13, Estado.E13, Estado.E13 },
        { Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF },
        { Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF },
        { Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF },
        { Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF },
        { Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF },
        { Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF },
        { Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF },
        { Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF },
        { Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF },
        { Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF },
        { Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF },
        { Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF, Estado.EF }
    };
}
