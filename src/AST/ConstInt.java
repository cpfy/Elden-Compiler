package AST;

public class ConstInt extends MyNumber {
    private int constInt;

    public ConstInt(int constInt) {
        setType("i32");
        this.constInt = constInt;
    }

    @Override
    public String addCodePre() {
        return "";
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
