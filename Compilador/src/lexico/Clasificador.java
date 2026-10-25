package lexico;

public final class Clasificador {
    private Clasificador() {
    }

    public static int getEvento(int c) {
        if (Character.isLetter(c)) {
            return 0;
        }
        if (Character.isDigit(c)) {
            return 1;
        }

        switch (c) {
            case '=': return 2;
            case '<': return 3;
            case '>': return 4;
            case '/': return 5;
            case '*': return 6;
            case '"': return 7;
            case '+': return 8;
            case '-': return 9;
            case '(': return 10;
            case ')': return 11;
            case '{': return 12;
            case '}': return 13;
            case ',': return 14;
            case '\r': return 15;
            case '\n': return 16;
            case ' ':
            case '\t': return 17;
            default: return 18;
        }
    }
}
