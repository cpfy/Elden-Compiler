package backend.Reg;

import llvm.Block;
import llvm.Function;
import llvm.Instr.Instr;

import java.util.ArrayList;
import java.util.HashMap;

public class LiveIntervals {

    private HashMap<Register, LiveInterval> LImap;

    // 每个变量活跃区间的类
    // LIS corresponds to the Liveness Analysis pass
    // 很棒的教程：https://github.com/nael8r/How-To-Write-An-LLVM-Register-Allocator/blob/master/HowToWriteAnLLVMRegisterAllocator.rst
    public LiveIntervals() {
        this.LImap = new HashMap<>();
    }

    // 初始扫描一遍intervals
    public void scanIntervals(ArrayList<Function> functions) {
        int i = 0;
        for (Function function : functions) {

            if (i == 0) {   // Jump First Function
                i++;
                continue;
            }

            // 初始化编号
            function.initInstrNo();
            for (Block b : function.getBlocklist()) {
                for (Instr inst : b.getInblocklist()) {
                    System.out.println(inst.getInstrNo() + ": " + inst.toString());
                }
            }


        }
    }

    public LiveInterval getInterval(Register Reg) {
        return LImap.get(Reg);
    }

    public void removeInterval(Register Reg) {
        LImap.remove(Reg);
    }

    public void clear() {
        LImap.clear();
    }

}
