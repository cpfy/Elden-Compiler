package AST;

import java.util.ArrayList;

public class ConstFloat extends MyNumber {
    private float constFloat;

    public ConstFloat(float constFloat) {
        setType("float");
        this.constFloat = constFloat;
    }

    @Override
    public ArrayList<String> addCodePre() {
        return new ArrayList<>();
    }

    @Override
    public String getTemp() {
        return String.valueOf(constFloat);
    }

    @Override
    public void calculate() {
        value = (int) constFloat;
        valueF = constFloat;
    }

    @Override
    public void addMidCode() {

    }
}