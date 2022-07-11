package AST;

public class ConstInt extends ExpPrimary {
    private int constInt;

    public ConstInt(int constInt) {
        this.constInt = constInt;
    }

    @Override
    public String getTemp() {
        return String.valueOf(constInt);
    }

    @Override
    public void calculate() {
        value = constInt;
    }

    @Override
    public void addMidCode() {

    }
}
