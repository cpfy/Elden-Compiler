package backend.Arm;

public class TwoArm extends Arm {

    private String op1;
    private String op2;

    public TwoArm(String instrname, String op1, String op2) {
        super(instrname);
        this.op1 = op1;
        this.op2 = op2;
    }

    @Override
    public String toString() {
        return super.getInstrname() + " " + op1 + ", " + op2;
    }

    public String getOp1() {
        return op1;
    }

    public String getOp2() {
        return op2;
    }
}
