package backend.optimize;

import backend.Arm.Arm;
import backend.Arm.TwoArm;

import java.util.ArrayList;

public class Optimizer {
    private ArrayList<Arm> oldArmInstrs;
    private ArrayList<Arm> newArmInstrs = new ArrayList<>();

    public Optimizer(ArrayList<Arm> oldArmInstrs) {
        this.oldArmInstrs = oldArmInstrs;
        System.out.println("开始荣誉删除");
        execute();
    }

    private void execute() {
        boolean changed = true;
        while (changed) {
            changed = false;
            for (int i = 0; i < oldArmInstrs.size() - 1; i++) {
                Arm arm1 = oldArmInstrs.get(i);
                Arm arm2 = oldArmInstrs.get(i + 1);

                if (arm1.getInstrname().equals("mov")) {
                    TwoArm movInstr = (TwoArm) arm1;
                    String dst = movInstr.getOp1();
                    String src = movInstr.getOp2();
                    if (isTempReg(dst) && arm2.getSrcRegs().contains(dst)) {
                        arm2.renameSrcRegs(src, dst);
                        arm1.setCanDelete(true);
                    }
                }
                else if (arm2.getInstrname().equals("mov")) {
                    TwoArm movInstr = (TwoArm) arm2;
                    String dst = movInstr.getOp1();
                    String src = movInstr.getOp2();
                    if (isTempReg(src) && arm1.getDstRegs().contains(src)) {
                        arm1.renameDstRegs(dst, src);
                        arm2.setCanDelete(true);
                    }
                }
            }
            newArmInstrs = new ArrayList<>();
            for (Arm arm: oldArmInstrs) {
                if (!arm.isCanDelete()) {
                    newArmInstrs.add(arm);
                }
                else {
                    changed = true;
                }
            }
            oldArmInstrs = newArmInstrs;
        }
    }

    private boolean isTempReg(String s) {
        return s.equals("r0") || s.equals("r1") || s.equals("r2") ||s.equals("r3");
    }


    public ArrayList<Arm> getNewArmInstrs() {
        return newArmInstrs;
    }
}
