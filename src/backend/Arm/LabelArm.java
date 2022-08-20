package backend.Arm;

import java.util.ArrayList;

public class LabelArm extends Arm {

    private String label;

    // 一个标签名 + <冒号>
    public LabelArm(String label) {
        super("label");
        this.label = label;
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
        // 必无缩进
        return label + ":";
    }

    public String getLabel() {
        return label;
    }
}
