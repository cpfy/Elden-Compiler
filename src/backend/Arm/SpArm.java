package backend.Arm;

import java.util.ArrayList;

public class SpArm extends Arm {

    public SpArm(String instrname) {
        super(instrname);
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
