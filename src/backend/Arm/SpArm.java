package backend.Arm;

import java.util.ArrayList;

public class SpArm extends Arm {

    private String op1;
    private String op2;
    private boolean hasOff = false;
    private int off;

    // 共4类：
    // ldr, str, vldr, vstr
    public SpArm(String instrname, String op1, String op2) {
        super(instrname);
        this.op1 = op1;
        this.op2 = op2; // 可能有".L " + lcount + " + " + lpicusecount * 4或者等于号+...情况（算TwoArm了）
    }

    public SpArm(String instrname, String op1, String op2, int off) {
        super(instrname);
        this.op1 = op1;
        this.op2 = op2;
        this.off = off;
        this.hasOff = true;
    }

    @Override
    public String toString() {
        String pre = super.isWithtab() ? "\t" : "";
        if (hasOff) {
            return pre + super.getInstrname() + " " + op1 + ", [" + op2 + ", #" + off + "]";
        }
        return pre + super.getInstrname() + " " + op1 + ", [" + op2 + "]";
    }

    @Override
    public ArrayList<String> getDstRegs() {
        return null;
    }

    @Override
    public ArrayList<String> getSrcRegs() {
        return null;
    }

    @Override
    public ArrayList<String> renameDstRegs(String newReg, String oldReg) {
        return null;
    }

    @Override
    public ArrayList<String> renameSrcRegs(String newReg, String oldReg) {
        return null;
    }
}
