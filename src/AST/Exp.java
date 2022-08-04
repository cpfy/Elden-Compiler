package AST;

import java.util.ArrayList;

public abstract class Exp extends Node {
    String type;
    int value;
    float valueF;
    public boolean calculated = false;

    public boolean isCanCal() {
        return canCal;
    }

    public void setCanCal(boolean canCal) {
        this.canCal = canCal;
    }

    public boolean canCal = true;

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

    ArrayList<String> codesPre = new ArrayList<>();

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public ArrayList<String> getCodes() {
        return codesPre;
    }

    @Override
    public void addCode(String s) {
        codesPre.add(s);
    }

    public void addCode(ArrayList<String> s) {
        codesPre.addAll(s);
    }

    public void generate() {
        super.addCode(codesPre);
    }

    public abstract ArrayList<String> addCodePre();

    public abstract String getTemp();

    public abstract void calculate();
}

