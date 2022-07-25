package AST;

import java.util.ArrayList;

public class ConstInt extends MyNumber {
    private int constInt;

    public ConstInt(int constInt) {
        setType("i32");
        this.constInt = constInt;
    }

    @Override
    public ArrayList<String> addCodePre() {
        return new ArrayList<>();
    }

    @Override
    public String getTemp() {
        return String.valueOf(constInt);
    }

    @Override
    public void calculate() {
        value = constInt;
        valueF = constInt;
    }

    @Override
    public void addMidCode() {

    }
}
