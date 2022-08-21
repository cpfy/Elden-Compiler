package backend.Arm;

import java.util.ArrayList;

public class TwoArm extends Arm {

    private String op1;
    private String op2;

    // 包含12种：
//    cmp
//    mov, movw, movt
//    vcmp.f32, vmrs
//    vmov, vmov.f32
//    vcvt.s32.f32, vcvt.f32.s32
//    ldr, vldr.f32（加载Global用）


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


    @Override
    public ArrayList<String> getSrcRegs() {
        ArrayList<String> list = new ArrayList<>();
        switch (super.getInstrname()) {
            case "cmp":
            case "vcmp.f32":
                list.add(op1);
                list.add(op2);
                break;

            case "mov":
            case "movw":
            case "movt":
            case "vmov":
            case "vmov.f32":
            case "vcvt.s32.f32":
            case "vcvt.f32.s32":
            case "ldr":
            case "vldr.f32":
//                if (op2.charAt(0) != '#') list.add(op2);
                list.add(op2);
                break;

            case "vmrs":
                break;
            default:
                break;
        }

        return list;
    }

    @Override
    public ArrayList<String> getDstRegs() {
        ArrayList<String> list = new ArrayList<>();
        switch (super.getInstrname()) {
            case "mov":
            case "movw":
            case "movt":
            case "vmov":
            case "vmov.f32":
            case "vcvt.s32.f32":
            case "vcvt.f32.s32":
            case "ldr":
            case "vldr.f32":
                list.add(op1);
                break;

            case "cmp":
            case "vcmp.f32":
            case "vmrs":
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
            case "cmp":
            case "vcmp.f32":
                if (op1.equals(oldReg)) op1 = newReg;
                if (op2.equals(oldReg)) op2 = newReg;
                list.add(op1);
                list.add(op2);
                break;

            case "mov":
            case "movw":
            case "movt":
            case "vmov":
            case "vmov.f32":
            case "vcvt.s32.f32":
            case "vcvt.f32.s32":
            case "ldr":
            case "vldr.f32":
//                if (op2.charAt(0) != '#') list.add(op2);
                if (op2.equals(oldReg)) op2 = newReg;
                list.add(op2);
                break;

            case "vmrs":
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
            case "mov":
            case "movw":
            case "movt":
            case "vmov":
            case "vmov.f32":
            case "vcvt.s32.f32":
            case "vcvt.f32.s32":
            case "ldr":
            case "vldr.f32":
                if (op1.equals(oldReg)) op1 = newReg;
                list.add(op1);
                break;

            case "cmp":
            case "vcmp.f32":
            case "vmrs":
                break;
            default:
                break;
        }

        return list;
    }
}
