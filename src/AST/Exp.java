package AST;

public abstract class Exp extends Node {
    String type = null;
    int value;
    float valueF;
    public boolean calculated = false;

    public int getValue() {
        if (calculated) {
            return value;
        }
        calculate();
        calculated = true;
        return value;
    }

    public float getValueF() {
        if (calculated) {
            return valueF;
        }
        calculate();
        calculated = true;
        return valueF;
    }

    StringBuilder codesPre = new StringBuilder();

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public StringBuilder getCodes() {
        return codesPre;
    }

    @Override
    public void addCode(String s) {
        codesPre.append(s);
    }

    public void generate() {
        super.addCode(codesPre.toString());
    }

    public abstract String addCodePre();

    public abstract String getTemp();

    public abstract void calculate();
}

