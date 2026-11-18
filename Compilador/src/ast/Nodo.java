package ast;

public class Nodo implements NodoAST {
    public String operador;
    public NodoAST izquierdo;
    public NodoAST derecho;

    public Nodo(String operador, NodoAST izquierdo, NodoAST derecho) {
        this.operador = operador;
        this.izquierdo = izquierdo;
        this.derecho = derecho;
    }
}
