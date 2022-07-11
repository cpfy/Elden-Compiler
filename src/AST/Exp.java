package AST;

public abstract class Exp extends Node {
    static int tempIndex = 0;

    int value;

    public boolean calculated = false;

    public String newTemp() {
        return "~t" + ++tempIndex;
    }

    public int getValue() {
        if (calculated) {
            return value;
        }
        calculate();
        calculated = true;
        return value;
    }

    public abstract String getTemp();

    public abstract void calculate();
}

