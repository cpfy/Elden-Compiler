package backend.Reg;

public class LiveRegMatrix {

    public final static int MAX_REG = 12; // 最大reg数量？

    private int NumAssigned = 0;
    private int NumUnassigned = MAX_REG;

    private VirtRegMap VRM;

    // Matrix: LiveRegMatrix instance, provides on-the-fly interference information and indirect assignment and unassignment of virtual registers.
    public LiveRegMatrix() {

    }

//    public void releaseMemory() {
//        for (int i = 0, e = Matrix.size(); i != e; ++i) {
//            Matrix[i].clear();
//            // No need to clear Queries here, since LiveIntervalUnion::Query doesn't
//            // have anything important to clear and LiveRegMatrix's runOnFunction()
//            // does a std::unique_ptr::reset anyways.
//        }
//    }

    // 分配寄存器映射
    public void assign(LiveInterval VirtReg, Register PhysReg) {
        assert (!VRM.hasPhys(VirtReg.getReg())) : "Duplicate VirtReg assignment";
        VRM.assignVirt2Phys(VirtReg.getReg(), PhysReg);

//        foreachUnit(
//                TRI, VirtReg, PhysReg, [&](unsigned Unit, const LiveRange &Range) {
//
//            Matrix[Unit].unify(VirtReg, Range);
//            return false;
//        });

        ++NumAssigned;
    }

    // 取消分配寄存器
    public void unassign(LiveInterval VirtReg) {
        Register PhysReg = VRM.getPhys(VirtReg.getReg());

        VRM.clearVirt(VirtReg.getReg());

//        foreachUnit(TRI, VirtReg, PhysReg,
//                [&](unsigned Unit, const LiveRange &Range) {
//            Matrix[Unit].extract(VirtReg, Range);
//            return false;
//        });

        ++NumUnassigned;

    }

    public void invalidateVirtRegs() {

    }

//    public boolean isPhysRegUsed(Register PhysReg) {
//
//    }
//
//    // 检查是否干涉
//    public boolean checkInterference(int Start, int End, Register PhysReg) {
//
//
//    }

}
