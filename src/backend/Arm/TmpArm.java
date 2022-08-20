package backend.Arm;

import java.util.ArrayList;

public class TmpArm extends Arm {
    private String str;

    // 给ldr，str过渡用
    public TmpArm(String str) {
        super("tmp");
        this.str = str;
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

    @Override
    public String toString() {
        if (super.isWithtab()) {
            return "\t" + str;
        }
        return str;
    }

    public String getStr() {
        return str;
    }
}
