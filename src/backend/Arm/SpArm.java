package backend.Arm;

import java.util.ArrayList;

public class SpArm extends Arm {

    private String op1;
    private String op2;
    private boolean hasOff = false;
    private int off = 0;

    // 共4类：
    // ldr, str, vldr.f32, vstr.f32
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

    public String getOp1() {
        return op1;
    }

    public String getOp2() {
        return op2;
    }

    public int getOff() {
        return off;
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
        ArrayList<String> list = new ArrayList<>();
        switch (super.getInstrname()) {
            case "ldr":
            case "vldr.f32":
                list.add(op1);
                break;
            case "str":
            case "vstr.f32":
                break;
            default:
                break;
        }
        return list;
    }

    @Override
    public ArrayList<String> getSrcRegs() {
        ArrayList<String> list = new ArrayList<>();
        switch (super.getInstrname()) {
            case "ldr":
            case "vldr.f32":
                list.add(op2);
                break;
            case "str":
            case "vstr.f32":    // str两个均为src，无dst
                list.add(op1);
                list.add(op2);
                break;
            default:
                break;
        }
        return list;
    }

    @Override
    public ArrayList<String> renameDstRegs(String newReg, String oldReg) {
        ArrayList<String> list = new ArrayList<>();
        switch (super.getInstrname()) {
            case "ldr":
            case "vldr.f32":
                if (op1.equals(oldReg)) op1 = newReg;
                list.add(op1);
                break;
            case "str":
            case "vstr.f32":
                break;
            default:
                break;
        }
        return list;
    }

    @Override
    public ArrayList<String> renameSrcRegs(String newReg, String oldReg) {
        ArrayList<String> list = new ArrayList<>();
        switch (super.getInstrname()) {
            case "ldr":
            case "vldr.f32":
                if (op2.equals(oldReg)) op2 = newReg;
                list.add(op2);
                break;
            case "str":
            case "vstr.f32":    // str两个均为src，无dst
                if (op1.equals(oldReg)) op1 = newReg;
                if (op2.equals(oldReg)) op2 = newReg;
                list.add(op1);
                list.add(op2);
                break;
            default:
                break;
        }
        return list;
    }
}
