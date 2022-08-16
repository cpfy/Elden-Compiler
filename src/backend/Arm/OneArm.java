package backend.Arm;

public class OneArm extends Arm {

    private String onestr;

    // 例如push {...}, beq ...
    public OneArm(String instrname, String onestr) {
        super(instrname);
        this.onestr = onestr;
    }

    @Override
    public String toString() {
        return super.getInstrname() + " " + onestr;
    }

    public String getOnestr() {
        return onestr;
    }
}
