package backend.Reg;

public class RegAllocBase {

    private TargetRegisterInfo TRI;
    private MachineRegisterInfo MRI;
    private VirtRegMap VRM;
    private LiveIntervals LIS;


    // 管理RegAlloc的总类
    // 参考：https://github.com/llvm/llvm-project/blob/main/llvm/lib/CodeGen/RegAllocBase.cpp
    public RegAllocBase(VirtRegMap vrm, LiveIntervals lis, LiveRegMatrix mat) {
//        this.TRI = vrm.getTargetRegInfo();
//        this.MRI = vrm.getRegInfo();
        this.VRM = vrm;
        this.LIS = lis;
//        Matrix = &mat;
//        MRI->freezeReservedRegs(vrm.getMachineFunction());
//        RegClassInfo.runOnMachineFunction(vrm.getMachineFunction());
    }
}
