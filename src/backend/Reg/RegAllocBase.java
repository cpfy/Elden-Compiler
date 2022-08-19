package backend.Reg;

import java.util.PriorityQueue;

public class RegAllocBase {

    private TargetRegisterInfo TRI;
    private MachineRegisterInfo MRI;
    private VirtRegMap VRM;
    private LiveIntervals LIS;
    private LiveRegMatrix Matrix;

    private PriorityQueue<LiveInterval> pQueue = new PriorityQueue<>();


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

        while (LiveInterval VirtReg = dequeue()){
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
//        enqueue(LI) ?????
    }

}
