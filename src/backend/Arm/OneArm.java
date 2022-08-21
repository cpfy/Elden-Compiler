package backend.Arm;

import java.util.ArrayList;

public class OneArm extends Arm {

    private String onestr;

    // 包含7种：
    // push {...}, pop, b, beq, bne, bl, bx
    public OneArm(String instrname, String onestr) {
        super(instrname);
        this.onestr = onestr;
    }

    @Override
    public String toString() {
        String str = super.getInstrname() + " " + onestr;
        if (super.isWithtab()) {
            return "\t" + str;
        }
        return str;
    }

    public String getOnestr() {
        return onestr;
    }

    @Override
    public ArrayList<String> getSrcRegs() {
        return new ArrayList<>();
    }

    @Override
    public ArrayList<String> getDstRegs() {
        return new ArrayList<>();
    }

    @Override
    public ArrayList<String> renameSrcRegs(String newReg, String oldReg) {
        return new ArrayList<>();
    }

    @Override
    public ArrayList<String> renameDstRegs(String newReg, String oldReg) {
        return new ArrayList<>();
    }
}
