package AST;

public class ConstFloat extends MyNumber {
    private float constFloat;

    public ConstFloat(float constFloat) {
        this.constFloat = constFloat;
    }

    @Override
    public String getTemp() {
        return String.valueOf(constFloat);
    }

    @Override
    public void calculate() {
        //todo!!!!!
        value = (int) constFloat;
    }

    @Override
    public void addMidCode() {

    }
}