package backend.Reg;

public class MachineRegisterInfo {

    private int numVirtRegs;

    // 管理machine寄存器信息 contains information about virtual registers
    public MachineRegisterInfo() {

    }

    // 虚拟寄存器数量
    public int getNumVirtRegs() {
        return numVirtRegs;
    }

    //
    public boolean reg_nodbg_empty(Register reg) {
        return true;//todo
    }

}
