package backend.Arm;

import java.util.ArrayList;

public class FourArm extends Arm {

    private String op1;
    private String op2;
    private String op3;
    private String op4;

    // 目前仅smull一个极端样例
    public FourArm(String instrname, String op1, String op2, String op3, String op4) {
        super(instrname);
        this.op1 = op1;
        this.op2 = op2;
        this.op3 = op3;
        this.op4 = op4;
    }

    @Override
    public String toString() {
        String str = super.getInstrname() + " " + op1 + ", " + op2 + ", " + op3 + ", " + op4;
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

    public String getOp4() {
        return op4;
    }


    @Override
    public ArrayList<String> getSrcRegs() {
        ArrayList<String> list = new ArrayList<>();
        list.add(op3);
        list.add(op4);
        return list;
    }

    @Override
    public ArrayList<String> getDstRegs() {
        ArrayList<String> list = new ArrayList<>();
        list.add(op1);
        list.add(op2);
        return list;
    }

    @Override
    public ArrayList<String> renameSrcRegs(String newReg, String oldReg) {
        ArrayList<String> list = new ArrayList<>();
        if (op3.equals(oldReg)) op3 = newReg;
        if (op4.equals(oldReg)) op4 = newReg;
        list.add(op3);
        list.add(op4);
        return list;
    }

    @Override
    public ArrayList<String> renameDstRegs(String newReg, String oldReg) {
        ArrayList<String> list = new ArrayList<>();
        if (op1.equals(oldReg)) op1 = newReg;
        if (op2.equals(oldReg)) op2 = newReg;
        list.add(op1);
        list.add(op2);
        return list;
    }
}
