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
        String str = super.getInstrname() + " " + op1 + ", " + op2;
        if (super.isWithtab()) {
            return "\t" + str;
        }
        return str;
    }

    public String getOp1() {
        return op1;
    }

    public String getOp2() {
        return op2;
    }
}
