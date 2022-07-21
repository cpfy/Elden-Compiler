package AST;

public class ConstFloat extends MyNumber {
    private float constFloat;

    public ConstFloat(float constFloat) {
        setType("float");
        this.constFloat = constFloat;
    }

    @Override
    public String addCodePre() {
        return "";
    }

    @Override
    public String getTemp() {
        return String.valueOf(constFloat);
    }

    @Override
    public void calculate() {
        valueF = constFloat;
    }

    @Override
    public void addMidCode() {

    }
}