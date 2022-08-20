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
    public void scanIntervalsAbort(Function function) {

        // 新的func，清理重置！！！
        clear();

        if (function.getFuncheader().getFname().equals("GlobalContainer")) {
            return;
        }

        // 初始化编号，这里的No.每个函数重置
        function.initInstrNo();

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
//                System.err.println();
//                System.err.println(i.toString());

                for (Map.Entry<String, Boolean> e : i.getUsesAndTypes().entrySet()) {
                    String s = e.getKey();
                    if (!paras.contains(s)) {
                        insertLIMapPos(s, no, e.getValue());    // e.getValue()标明是否是float
                    }
                }

                // 只一个，但也遍历一下
                if (i.getDefAndType() != null) {
                    for (Map.Entry<String, Boolean> e : i.getDefAndType().entrySet()) {
                        String s = e.getKey();
                        if (!paras.contains(s)) {
                            insertLIMapPos(s, no, e.getValue());    // e.getValue()标明是否是float
                        }
                    }
                }
            }
        }

        printtest();
    }

    // 初始扫描一遍intervals
    // 算法见：https://www.zhihu.com/question/29355187/answer/99413526
    public void scanIntervals(Function function) {

        // 新的func，清理重置！！！
        clear();

        if (function.getFuncheader().getFname().equals("GlobalContainer")) {
            return;
        }

        function.initInstrNo();     // 初始化编号，这里的No.每个函数重置
        function.initLiveInAndOut();    // 初始化计算In/Out信息

        for (Block block: function.getBlocklist()) {
            System.out.println();
            System.out.println("l" + block.getLabel() + ":");
            System.out.println();

            for (String s: block.getLiveIn().keySet()) {
                System.out.println("livein: " + s);
            }

            for (String s: block.getLiveOut().keySet()) {
                System.out.println("liveout: " + s);
            }


            System.out.println();
            for (Instr instr: block.getInblocklist()) {
                System.out.println(instr.getInstrNo() + ": " + instr);
            }
        }


        // para由于内存传参，一律不分配Reg
        ArrayList<String> paras = function.getParas();

        ArrayList<Block> sortlist = function.getSortedBlocks();
        for (int j = sortlist.size() - 1; j >= 0; j--) {

            Block b = sortlist.get(j);
            int block_from = b.getInblocklist().get(0).getInstrNo();
            int block_to = b.getInblocklist().get(b.getInblocklist().size() - 1).getInstrNo() + 2;

            for (Map.Entry<String, Boolean> e : b.getLiveOut().entrySet()) {
                insertLIMapRange(e.getKey(), block_from, block_to, e.getValue());
            }

            for (int k = b.getInblocklist().size() - 1; k >= 0; k--) {
                Instr i = b.getInblocklist().get(k);
                int no = i.getInstrNo();

                // Def变量的range在此处截断
                if (i.getDef() != null) {
                    String s = i.getDef();
                    truncateLIMapPos(i.getDef(), no);
                }

                // 延长或添加新Range
                for (Map.Entry<String, Boolean> e : i.getUsesAndTypes().entrySet()) {
                    String s = e.getKey();
                    insertLIMapRange(s, block_from, no, e.getValue());    // e.getValue()标明是否是float
                }

            }
        }

        //todo 清理paras
        for (String s : paras) {
            LImap.remove(s);
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

    // 向LImap加入一个Int/Float变量+位置
    private void insertLIMapPos(String s, int pos, boolean f) {
        if (!LImap.containsKey(s)) {
            LiveInterval newli = new LiveInterval(s);
            if (f) {
                newli.setVarf(true);
            }
            LImap.put(s, newli);
        }
        LImap.get(s).addUsePos(pos);
    }

    // 向LImap加入一个Int/Float变量+range，f表明是float
    private void insertLIMapRange(String s, int left, int right, boolean f) {
        if (!LImap.containsKey(s)) {
            LiveInterval newli = new LiveInterval(s);
            if (f) {
                newli.setVarf(true);
            }
            LImap.put(s, newli);
        }
        LImap.get(s).addRange(left, right);
    }

    // Def变量在Range处截断
    private void truncateLIMapPos(String s, int pos/*, boolean f*/) {
        assert (LImap.containsKey(s));
        if (LImap.get(s) != null) {
            LImap.get(s).truncate(pos);
        }
    }

    private void printtest() {
        OutputControl.printMessage("【开始输出：LImap】");
        for (LiveInterval LI : LImap.values()) {
            OutputControl.printMessage(LI.toString());
        }
    }

}
