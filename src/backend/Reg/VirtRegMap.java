package backend.Reg;

public class VirtRegMap {

    private MachineRegisterInfo MRI;
    private TargetRegisterInfo TRI;

    // VRM: VirtRegMap instance, maps virtual register to physical registers and also to stack slots.
    // 参见：https://github.com/llvm/llvm-project/blob/main/llvm/lib/CodeGen/VirtRegMap.cpp
    public VirtRegMap() {

    }

    public MachineRegisterInfo getRegInfo() {
        return MRI;
    }

    public TargetRegisterInfo getTargetRegInfo() {
        return TRI;
    }

    public boolean hasPhys(Register reg) {
        return true;    //todo
    }

}
