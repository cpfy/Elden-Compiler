package backend.Reg;


import java.util.HashMap;
import java.util.Map;

public class VirtRegMap {

    private int NumSpillSlots;  // "Number of spill slots allocated"
    private int NumIdCopies;   // "Number of identity moves eliminated after rewriting"

    private MachineRegisterInfo MRI;
    private TargetRegisterInfo TRI;

    private HashMap<Register, Register> Virt2PhysMap;
    private HashMap<Register, Register> Virt2StackSlotMap;
    private HashMap<Register, Register> Virt2SplitMap;
    private HashMap<Register, Register> Virt2ShapeMap;


    // VRM: VirtRegMap instance, maps virtual register to physical registers and also to stack slots.
    // 参见：https://github.com/llvm/llvm-project/blob/main/llvm/lib/CodeGen/VirtRegMap.cpp
    public VirtRegMap() {

        this.Virt2PhysMap.clear();
        this.Virt2StackSlotMap.clear();
        this.Virt2SplitMap.clear();
        this.Virt2ShapeMap.clear();

    }

    public MachineRegisterInfo getRegInfo() {
        return MRI;
    }

    public TargetRegisterInfo getTargetRegInfo() {
        return TRI;
    }

    // 为Virt分配Phys
    public void assignVirt2Phys(Register virtReg, Register physReg) {
        assert (virtReg.isVirtual() && isPhysicalRegister(physReg));
        assert (!Virt2PhysMap.containsKey(virtReg)) :
                "attempt to assign physical register to already mapped virtual register";
        assert (!getRegInfo().isReserved(physReg)) : "Attempt to map virtReg to a reserved physReg";
        Virt2PhysMap.put(virtReg, physReg);
    }

    // 是否有推荐Reg？
    public boolean hasPreferredPhys(Register VirtReg) {
        Register Hint = MRI.getSimpleHint(VirtReg);
        if (!Hint.isValid())
            return false;
        if (Hint.isVirtual())
            Hint = getPhys(Hint);
        return getPhys(VirtReg) == Hint;
    }

    public boolean hasKnownPreference(Register VirtReg) {
        Map.Entry<Register, Register> Hint = MRI.getRegAllocationHint(VirtReg);
        if (isPhysicalRegister(Hint.getValue())) {
            return true;
        }

        if (isVirtualRegister(Hint.getValue())) {
            return hasPhys(Hint.getValue());
        }

        return false;
    }

    // 分配StackSlot
    public int assignVirt2StackSlot(Register virtReg) {
        assert (virtReg.isVirtual());
        assert (!Virt2StackSlotMap.containsKey(virtReg)) : "attempt to assign stack slot to already spilled register";
//  const TargetRegisterClass* RC = MF.getRegInfo().getRegClass(virtReg);
//        return Virt2StackSlotMap[virtReg.id()] = createSpillSlot(RC);

        return 0;//todo
    }

    public boolean hasPhys(Register reg) {
        return true;    //todo
    }

    public Register getPhys(Register v) {
        return null;//todo
    }

    public void clearVirt(Register v) {

    }

    public boolean isPhysicalRegister(Register r) {
        return true;//todo
    }

    public boolean isVirtualRegister(Register r) {
        return true;//todo
    }

}
