package backend.optimize;

import backend.Arm.Arm;
import backend.Arm.SpArm;
import backend.Arm.TwoArm;

import java.util.ArrayList;

public class Optimizer {
    private ArrayList<Arm> oldArmInstrs;
    private ArrayList<Arm> newArmInstrs = new ArrayList<>();

    public Optimizer(ArrayList<Arm> oldArmInstrs) {
        this.oldArmInstrs = oldArmInstrs;
        System.out.println("【开始冗余删除】");
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
                    if (movInstr.getOp2().charAt(0) == '#' && arm2.getInstrname().equals("vmov")) {
                        continue;
                    }
                    String dst = movInstr.getOp1();
                    String src = movInstr.getOp2();
                    if (isTempReg(dst) && arm2.getSrcRegs().contains(dst)) {
                        arm2.renameSrcRegs(src, dst);
                        arm1.setCanDelete(true);
                    }
                }
                else if (arm2.getInstrname().equals("mov")) {
                    if (arm1.getInstrname().equals("movt")) {
                        continue;
                    }
                    TwoArm movInstr = (TwoArm) arm2;
                    String dst = movInstr.getOp1();
                    String src = movInstr.getOp2();
                    if (isTempReg(src) && arm1.getDstRegs().contains(src)) {
                        arm1.renameDstRegs(dst, src);
                        arm2.setCanDelete(true);
                    }
                }
                else if (arm1.getInstrname().equals("vmov")) {
                    TwoArm movInstr = (TwoArm) arm1;
                    String dst = movInstr.getOp1();
                    String src = movInstr.getOp2();
                    if (src.charAt(0) == 's' && isFTempReg(dst) && arm2.getSrcRegs().contains(dst)) {
                        arm2.renameSrcRegs(src, dst);
                        arm1.setCanDelete(true);
                    }
                }
                else if (arm2.getInstrname().equals("vmov")) {
                    TwoArm movInstr = (TwoArm) arm2;
                    String dst = movInstr.getOp1();
                    String src = movInstr.getOp2();
                    if (dst.charAt(0) == 's' && isFTempReg(src) && arm1.getDstRegs().contains(src)) {
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
//        for (int i = 0; i < oldArmInstrs.size() - 1; i++) {
//            Arm arm1 = oldArmInstrs.get(i);
//            Arm arm2 = oldArmInstrs.get(i + 1);
//            if (arm1.getInstrname().equals("ldr") && arm2.getInstrname().equals("ldr")) {
//                if (!(arm1 instanceof SpArm) || !(arm2 instanceof SpArm)) {
//                    continue;
//                }
//                SpArm spArm1 = (SpArm) arm1;
//                SpArm spArm2 = (SpArm) arm2;
//                if (spArm1.getOp1().equals(spArm2.getOp1()) && spArm1.getOp2().equals(spArm2.getOp2()) && spArm1.getOff() == spArm2.getOff()) {
//                    spArm2.setCanDelete(true);
//                }
//            }
//        }
        newArmInstrs = new ArrayList<>();
        for (Arm arm: oldArmInstrs) {
            if (!arm.isCanDelete()) {
                newArmInstrs.add(arm);
            }
        }
        oldArmInstrs = newArmInstrs;

    }

    private boolean isFTempReg(String s) {
        return s.equals("s0") || s.equals("s1") || s.equals("s2");
    }

    private boolean isTempReg(String s) {
        return s.equals("r0") || s.equals("r1") || s.equals("r2") ||s.equals("r3");
    }


    public ArrayList<Arm> getNewArmInstrs() {
        return newArmInstrs;
    }
}
