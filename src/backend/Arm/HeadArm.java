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
