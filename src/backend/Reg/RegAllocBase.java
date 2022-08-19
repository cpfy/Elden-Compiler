package backend.Reg;

import java.util.PriorityQueue;
import java.util.Vector;

public class RegAllocBase {

    private TargetRegisterInfo TRI;
    private MachineRegisterInfo MRI;
    private VirtRegMap VRM;
    private LiveIntervals LIS;
    private LiveRegMatrix Matrix;

    private PriorityQueue<LiveInterval> pQueue = new PriorityQueue<>(); // 需要在LI类里实现compare to


    // 管理RegAlloc的总类
    // 参考：https://github.com/llvm/llvm-project/blob/main/llvm/lib/CodeGen/RegAllocBase.cpp
    public RegAllocBase(VirtRegMap vrm, LiveIntervals lis, LiveRegMatrix mat) {
        this.TRI = vrm.getTargetRegInfo();
        this.MRI = vrm.getRegInfo();
        this.VRM = vrm;
        this.LIS = lis;
        this.Matrix = mat;

//        MRI->freezeReservedRegs(vrm.getMachineFunction());
//        RegClassInfo.runOnMachineFunction(vrm.getMachineFunction());
    }


    // 挑选活跃寄存器
    // Visit all the live registers. If they are already assigned to a physical
    // register, unify them with the corresponding LiveIntervalUnion, otherwise push
    // them on the priority queue for later assignment.
    private void seedLiveRegs() {
        for (int i = 0, e = MRI.getNumVirtRegs(); i != e; ++i) {
            Register Reg = TargetRegisterInfo.index2VirtReg(i);
            if (MRI.reg_nodbg_empty(Reg)) { // if is not a DEBUG register
                continue;
            }
            enqueue(LIS.getInterval(Reg));
        }
    }

    // Top-level driver to manage the queue of unassigned VirtRegs and call the
    // selectOrSplit implementation.
    public void allocatePhysRegs() {
        seedLiveRegs();

        while (!pQueue.isEmpty()) {
            LiveInterval VirtReg = dequeue();
            assert (!VRM.hasPhys(VirtReg.getReg())) : "Register already assigned";

            // 溢出合并片段时？未使用寄存器可出现
            if (MRI.reg_nodbg_empty(VirtReg.getReg())) {
                aboutToRemoveInterval(VirtReg);
                LIS.removeInterval(VirtReg.getReg());
                continue;
            }

            Matrix.invalidateVirtRegs();

            //todo

        }
    }

    // 入队
    public void enqueue(LiveInterval LI) {
        Register Reg = LI.getReg();
        assert (Reg.isVirtual()) : "Can only enqueue virtual registers";

        if (VRM.hasPhys(Reg)) {
            return;
        }

        pQueue.add(LI);
    }

    // 出队
    public LiveInterval dequeue() {
        if (pQueue.isEmpty()) {
            return null;
        }
        LiveInterval LI = pQueue.peek();
        pQueue.poll();
        return LI;
    }

    public boolean LRE_CanEraseVirtReg(Register VirtReg) {
        LiveInterval LI = LIS.getInterval(VirtReg);
        if (VRM.hasPhys(VirtReg)) {
            Matrix.unassign(LI);
            aboutToRemoveInterval(LI);
            return true;
        }
        // Unassigned virtreg is probably in the priority queue.
        // RegAllocBase will erase it after dequeueing.
        // Nonetheless, clear the live-range so that the debug
        // dump will show the right state for that VirtReg.
        LI.clear();
        return false;
    }

    public void LRE_WillShrinkVirtReg(Register VirtReg) {
        if (!VRM.hasPhys(VirtReg))
            return;

        // Register is assigned, put it back on the queue for reassignment.
        LiveInterval LI = LIS.getInterval(VirtReg);
        Matrix.unassign(LI);
        enqueue(LI);
    }

    public MCRegister selectOrSplit(LiveInterval VirtReg, Vector<Register> SplitVRegs) {
        // Populate a list of physical register spill candidates.
//        Vector(MCRegister, 8) PhysRegSpillCands;
        return null;//todo
    }

    public void aboutToRemoveInterval(LiveInterval LI) {

    }

}
