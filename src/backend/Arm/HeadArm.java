package backend.Arm;

import java.util.ArrayList;

public class HeadArm extends Arm {
    private String str;

    // 完整的一个str
    public HeadArm(String str) {
        super("head");
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
