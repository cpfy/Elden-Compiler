package backend.Arm;

import java.util.ArrayList;

public class ThreeArm extends Arm {

    private String op1;
    private String op2;
    private String op3;

    // 包含9种：
//    add, sub, mul, sdiv, srem
//    vadd.f32, vsub.f32, vmul.f32, vdiv.f32
    public ThreeArm(String instrname, String op1, String op2, String op3) {
        super(instrname);
        this.op1 = op1;
        this.op2 = op2;
        this.op3 = op3;
    }

    @Override
    public String toString() {
        String str = super.getInstrname() + " " + op1 + ", " + op2 + ", " + op3;
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

    public String getOp3() {
        return op3;
    }

    @Override
    public ArrayList<String> getSrcRegs() {
        return null;
    }

    @Override
    public ArrayList<String> getDstRegs() {
        return null;
    }

    @Override
    public ArrayList<String> renameSrcRegs(String newReg, String oldReg) {
        return null;
    }

    @Override
    public ArrayList<String> renameDstRegs(String newReg, String oldReg) {
        return null;
    }
}
