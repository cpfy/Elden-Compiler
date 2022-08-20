package backend.Reg;

import llvm.Block;
import llvm.Function;
import llvm.Instr.Instr;
import tool.OutputControl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class LiveIntervals {

    private HashMap<String, LiveInterval> LImap;

    // 每个变量活跃区间的类
    // LIS corresponds to the Liveness Analysis pass
    // 很棒的教程：https://github.com/nael8r/How-To-Write-An-LLVM-Register-Allocator/blob/master/HowToWriteAnLLVMRegisterAllocator.rst
    public LiveIntervals() {
        this.LImap = new HashMap<>();
    }

    // 初始扫描一遍intervals
    public void scanIntervals(Function function) {

        if (function.getFuncheader().getFname().equals("GlobalContainer")) {
            return;
        }
        int cnt = 0;

        // 初始化编号，这里的No.每个函数重置
        function.initInstrNo();

        // 函数参数也有def
        //todo 不确定para是否要处理
        ArrayList<String> paras = function.getParas();


        for (Block b : function.getBlocklist()) {
            for (Instr i : b.getInblocklist()) {
                OutputControl.printMessage(i.getInstrNo() + ": " + i.toString());
//                    OutputControl.printMessage("Uses:" + i.getUses());
//                    OutputControl.printMessage("Define:" + i.getDef());

                int no = i.getInstrNo();

//                for (String s : i.getUses()) {
//                    // 目前paras均不分配
//                    if (!paras.contains(s)) {
//                        insertLIMap(s, no);
//                    }
//                }
//                if (i.getDef() != null) {
//                    // 目前paras均不分配
//                    if (!paras.contains(i.getDef())) {
//                        insertLIMap(i.getDef(), no);
//                    }
//                }

                // 第一个=变量名；第二个true=float
//                System.out.println();
//                System.out.println(i.toString());

                for (Map.Entry<String, Boolean> e : i.getUsesAndTypes().entrySet()) {
                    String s = e.getKey();
                    if (!paras.contains(s)) {
                        if (!e.getValue()) {
//                            System.out.println("i32 " + s);
                            insertLIMap(s, no);
                        }
                        else {
//                            System.out.println("float " + s);
                            insertFLIMap(s, no);
                        }
                    }
                }

                // 只一个，但也遍历一下
                if (i.getDefAndType() != null) {
                    for (Map.Entry<String, Boolean> e : i.getDefAndType().entrySet()) {
                        String s = e.getKey();
                        if (!paras.contains(s)) {
                            if (!e.getValue()) {
//                                System.out.println("i32 " + s);
                                insertLIMap(s, no);
                            }
                            else {
//                                System.out.println("float " + s);
                                insertFLIMap(s, no);
                            }
                        }
                    }
                }
            }
        }

        printtest();
    }

    public HashMap<String, LiveInterval> getLImap() {
        return LImap;
    }

    public LiveInterval getInterval(Register Reg) {
        return LImap.get(Reg);
    }

    public void removeInterval(Register Reg) {
        LImap.remove(Reg);
    }

    // 每个function结束清理
    public void clear() {
        LImap.clear();
    }

    // 向LImap加入一个变量+位置
    private void insertLIMap(String s, int pos) {
        if (!LImap.containsKey(s)) {
            LImap.put(s, new LiveInterval(s));
        }
        LImap.get(s).addUsePos(pos);
    }

    // 向LImap加入一个Float变量+位置
    private void insertFLIMap(String s, int pos) {
        if (!LImap.containsKey(s)) {
            LiveInterval newli = new LiveInterval(s);
            newli.setVarf(true);
            LImap.put(s, newli);
        }
        LImap.get(s).addUsePos(pos);
    }

    private void printtest() {
        OutputControl.printMessage("【开始输出：LImap】");
        for (LiveInterval LI : LImap.values()) {
            OutputControl.printMessage(LI.toString());
        }
    }

}
