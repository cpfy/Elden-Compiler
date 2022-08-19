package backend.Reg;

public class MCRegister {

    private int Reg;    // represent all physical registers, but not necessarily virtual registers.

    static int NoRegister = 0;
    static int FirstPhysicalReg = 1;
    static int FirstStackSlot = 1 << 30;
    static int VirtualRegFlag = 1 << 31;

    // llvm定义的一个辅助Reg的数据结构
    static boolean isStackSlot(int Reg) {
        return FirstStackSlot <= Reg && Reg < VirtualRegFlag;
    }

    // Return true if the specified register number is in
    // the physical register namespace.
    static boolean isPhysicalRegister(int Reg) {
        return FirstPhysicalReg <= Reg && Reg < FirstStackSlot;
    }

    // Check the provided unsigned value is a valid MCRegister.
//    static MCRegister from(unsigned Val) {
//        assert (Val == NoRegister || isPhysicalRegister(Val));
//        return MCRegister(Val);
//    }

    int id() {
        return Reg;
    }

    boolean isValid() {
        return Reg != NoRegister;
    }

}
