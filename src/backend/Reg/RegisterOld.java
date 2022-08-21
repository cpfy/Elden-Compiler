package backend.Reg;

import llvm.Ident;
import tool.OutputControl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;

import static java.lang.System.exit;

public class RegisterOld {
    private HashMap<Integer, String> regMap;
    private HashMap<String, Integer> regNameMap;
    private HashMap<Integer, String> fregMap;   // 浮点寄存器值
    private HashMap<String, Integer> fregNameMap;   // 浮点寄存器值

    private ArrayList<Integer> freeRegList;
    private ArrayList<Integer> freeFloatRegList;
    private HashMap<String, Integer> identAllocMap;
    //    private ArrayList<Integer> activeRegList;   //当前活跃的变量占用的、已分配出的reg
    private ArrayList<Ident> identRegUsageList;


    // 寄存器分配相关
    private LiveIntervals LIS;
    private HashMap<String, String> allocmap;           // 从ident_name到寄存器名的映射map
    private HashSet<String> spillSet;                   // 溢出的set集合

    private PriorityQueue<LiveInterval> unhandledList;  // 未被分配寄存器的活跃区间，按照开始位置递增的方式进行排序
    private PriorityQueue<LiveInterval> activeList;     // 已经分配寄存器的活跃区间，按照结束位置递增的方式进行排序
    private PriorityQueue<String> regpool;              // 可用寄存器池

    // 浮点Reg Alloc
    private PriorityQueue<LiveInterval> FunhandledList;  // 未被分配寄存器的活跃区间，按照开始位置递增的方式进行排序
    private PriorityQueue<LiveInterval> FactiveList;     // 已经分配寄存器的活跃区间，按照结束位置递增的方式进行排序
    private PriorityQueue<String> Fregpool;              // 可用寄存器池


    private final int REG_MAX = 12 - 4;     // 预留r0-r2,r3；r7不使用；分r4-r12共8个
    public final static String T0 = "r0";   // 标准临时寄存器用
    public final static String T1 = "r1";
    public final static String T2 = "r2";
    public final static String SB = "r3";   // 迫不得已加一个临时寄存器

    private final int FREG_MAX = 32 - 3;    // 预留s0-s2；分s3-s31共29个
    public final static String F0 = "s0";   // 标准临时寄存器用
    public final static String F1 = "s1";
    public final static String F2 = "s2";

    private boolean optimize = false;


    public RegisterOld() {
        this.regMap = new HashMap<>();
        this.regNameMap = new HashMap<>();
        this.fregMap = new HashMap<>();
        this.fregNameMap = new HashMap<>();

        this.freeRegList = new ArrayList<>();
        this.freeFloatRegList = new ArrayList<>();
        this.identAllocMap = new HashMap<>();
//        this.activeRegList = new ArrayList<>();

        // About Reg Alloc
        this.allocmap = new HashMap<>();
        this.spillSet = new HashSet<>();

        this.unhandledList = new PriorityQueue<>();
        this.activeList = new PriorityQueue<LiveInterval>(new Comparator<LiveInterval>() {  // 按end递减排序
            @Override
            public int compare(LiveInterval o1, LiveInterval o2) {
                return o2.getEnd() - o1.getEnd();
            }
        });
        this.FunhandledList = new PriorityQueue<>();
        this.FactiveList = new PriorityQueue<LiveInterval>(new Comparator<LiveInterval>() {  // 按end递减排序
            @Override
            public int compare(LiveInterval o1, LiveInterval o2) {
                return o2.getEnd() - o1.getEnd();
            }
        });

        this.regpool = new PriorityQueue<>();
        this.Fregpool = new PriorityQueue<>();
        initRegPool();
        initFRegPool();


        initRegMap();
        initRegnameMap();
        initFregMap();
        initFregNameMap();
        initFreeRegList();
        initFreeFloatRegList();

    }

    // 初始化操作
    private void initRegMap() {
        // regMap.put(0, "r0");
        for (int i = 0; i <= 12; i++) {
            regMap.put(i, "r" + i);
        }
        regMap.put(13, "sp");   // Stack pointer
        regMap.put(14, "lr");   // Link String
        regMap.put(15, "pc");   // pro.. count
    }

    private void initFregMap() {
        // fregMap.put(0, "s0");
        // 设定float的s系列寄存器number为32-63
        for (int i = 0; i < 32; i++) {
            fregMap.put(i + 32, "s" + i);
        }
    }

    private void initFregNameMap() {
        // fregNameMap.put("s0", 0);
        // 设定float的s系列寄存器number为32-63
        for (int i = 0; i < 32; i++) {
            fregNameMap.put("s" + i, i + 32);
        }
    }

    private void initRegnameMap() {
        // regNameMap.put("r0", 0);
        for (int i = 0; i <= 12; i++) {
            regNameMap.put("r" + i, i);
        }
        regNameMap.put("sp", 13);
        regNameMap.put("lr", 14);
        regNameMap.put("pc", 15);
    }

    // 空闲reg池
    private void initFreeRegList() {
        for (int i = 0; i < 13; i++) {
            if (i != 7 && i != 0) {
                freeRegList.add(i);
            }
        }
    }

    // 空闲 float reg池
    private void initFreeFloatRegList() {
        for (int i = 32; i < 64; i++) {
            freeFloatRegList.add(i);
        }
    }

    //查询 no -> name
    public String getRegnameFromNo(int no) {
        return regMap.get(no);
    }

    //查询 name -> no
    public int getRegnoFromName(String name) {
        return regNameMap.get(name);
    }

    // apply tmp reg.（禁用此写法！）
//    public String applyTmp() {
//        int regno;
//        if (!freeRegList.isEmpty()) {
//            regno = freeRegList.get(0);
//            freeRegList.remove(0);
////            addActiveListNoRep(regno);     //无重复加入activeregList 活跃变量表
//
//        } else {    //todo 无空寄存器
//            System.err.println("No free Reg! Alloc $r0.");
//            regno = 0;
//        }
//
////        OutputControl.printMessage("Alloc Reg $" + regMap.get(regno) + " to Tmp");
//        return this.regMap.get(regno);
//    }
//
//    // free tmp reg.（禁用此写法！）
//    public void freeTmp(String reg) {
//        int no = this.regNameMap.get(reg);
//        if (no > 12) {
//            System.err.println("Register freeTmpRegister() : Error free tmp Reg No!! regno = " + no);
//        } else if (!freeRegList.contains(no)) {
////            removeActiveRegList(no);     //删除变量in activeregList 活跃变量表
//            freeRegList.add(no);
////            OutputControl.printMessage("Free Reg $" + regMap.get(no) + " from Tmp");
//        }
//        //todo 其它的free寄存器都得检查是否重复！
//    }
//
//    // apply tmp float reg.
//    public String applyFTmp() {
//        int regno;
//        if (!freeFloatRegList.isEmpty()) {
//            regno = freeFloatRegList.get(0);
//            freeFloatRegList.remove(0);
//
//            // 此时还没用到active
////            addActiveListNoRep(regno);     //无重复加入activeregList 活跃变量表
//
//        } else {    //todo 无空寄存器
//            System.err.println("No free float Reg! Alloc $s0.");
//            regno = 32;
//        }
//
////        OutputControl.printMessage("Alloc Float Reg $" + fregMap.get(regno) + " to Tmp");
//        return this.fregMap.get(regno);
//    }
//
//    // free float tmp reg.
//    public void freeFTmp(String reg) {
//        int no = this.fregNameMap.get(reg);
//        assert no >= 32 && no < 64;
//
//        if (!freeFloatRegList.contains(no)) {
////            removeActiveRegList(no);     //删除变量in activeregList 活跃变量表
//            freeFloatRegList.add(no);
//            OutputControl.printMessage("Free Float Reg $" + fregMap.get(no) + " from Tmp");
//        }
//        //todo 其它的free寄存器都得检查是否重复
//    }
//
//    // free任意float或int寄存器
//    public void free(String reg) {
//        if (reg.charAt(0) == 's') {    // 必不可能是sp
//            freeFTmp(reg);
//        } else {
////            freeTmp(reg);
//            System.err.println("禁止释放Int寄存器！");
//            exit(1);
//        }
//    }


    //reset全部寄存器状态
    public void resetAllReg() {
        freeRegList.clear();
        identAllocMap.clear();
//        activeRegList.clear();

        initFreeRegList();
    }


    // 此处新加BiSh用
    public int searchIdentRegNo(Ident i) {
        String mapname = i.getMapname();
        if (identAllocMap.containsKey(mapname)) {
            return identAllocMap.get(mapname);
        }
        return -1;
        // 表示error
    }

    //
    //

    /***** 每个函数寄存器分配用 *****/
    //
    //

    // 初始化设置LIS
    public void setLIS(LiveIntervals LIS) {
        this.LIS = LIS;
    }

    private void clear() {
        this.allocmap.clear();
        this.spillSet.clear();

        this.unhandledList.clear();
        this.activeList.clear();
        this.regpool.clear();
        initRegPool();

        this.FunhandledList.clear();
        this.FactiveList.clear();
        this.Fregpool.clear();
        initFRegPool();
    }

    // 初始化寄存器池
    private void initRegPool() {
        for (int i = 4; i <= 12; ++i) {
            if (i != 7) {
                regpool.add("r" + i);
            }
        }
    }

    // 初始化F寄存器池
    private void initFRegPool() {
        for (int i = 3; i < 32; ++i) {
            Fregpool.add("s" + i);
        }
    }

    // 启动线性扫描
    public void RegAllocScan() {
        clear();

        // 应为已经scan后的状态
        for (LiveInterval LI : LIS.getLImap().values()) {
            if (LI.isGlobal()) {
//                spillSet.add(LI.getVname());    // 可不加spillSet，不用这个判断了
                continue;
            }

            if (LI.isVarf()) {
//                System.out.println("NB! "+LI.toString());
                FunhandledList.add(LI);
            } else {
                unhandledList.add(LI);
            }
        }

        while (!unhandledList.isEmpty()) {
            LiveInterval LI = unhandledList.poll();
            expireOldIntervals(LI);
            if (activeList.size() == REG_MAX) {
                spillAtInterval(LI);

            } else {
                String physReg = regpool.poll();
                LI.setReg(physReg);
                allocmap.put(LI.getVname(), physReg);
                activeList.add(LI);
            }
        }

        // Float Reg
        while (!FunhandledList.isEmpty()) {
            LiveInterval FLI = FunhandledList.poll();
            expireOldFIntervals(FLI);
            if (FactiveList.size() == FREG_MAX) {
                spillAtFInterval(FLI);

            } else {
                String physReg = Fregpool.poll();
                FLI.setReg(physReg);
                System.out.println("Assign (" + FLI.toString() + ", " + physReg + ")");
                allocmap.put(FLI.getVname(), physReg);
                FactiveList.add(FLI);
            }
        }

        printtest();
    }

    // 参考算法：https://www.zhihu.com/question/29355187/answer/99413526
    // Reg Alloc专用：释放old区间
    private void expireOldIntervals(LiveInterval LI) {
        while (!activeList.isEmpty()) {
            LiveInterval j = activeList.poll();
            if (j.getEnd() >= LI.getStart()) {
                activeList.add(j);
                return;
            }
//            activeList.remove(j);   // 此处边遍历边修改了，不可
            String physReg = j.getReg();
            regpool.add(physReg);
        }
    }

    // Reg Alloc专用：溢出
    private void spillAtInterval(LiveInterval LI) {
//        spillSet.add(LI.getVname());
        LiveInterval spill = activeList.peek();
        if (spill.getEnd() > LI.getEnd()) {
            String physReg = spill.getReg();
            allocmap.remove(spill.getVname());
            allocmap.put(LI.getVname(), physReg);
            spill.setReg(null);
            LI.setReg(physReg);
            spillSet.add(spill.getVname());
            activeList.poll();
            activeList.add(LI);

        } else {
            spillSet.add((LI.getVname()));
        }
    }


    /***** F寄存器分配照抄一份Reg的分配 *****/
    private void expireOldFIntervals(LiveInterval FLI) {
        while (!FactiveList.isEmpty()) {
            LiveInterval j = FactiveList.poll();
            if (j.getEnd() >= FLI.getStart()) {
                FactiveList.add(j);
                return;
            }

            String physReg = j.getReg();
            Fregpool.add(physReg);
        }
    }

    // Reg Alloc专用：溢出
    private void spillAtFInterval(LiveInterval FLI) {
        LiveInterval fspill = FactiveList.peek();
        if (fspill.getEnd() > FLI.getEnd()) {
            String physReg = fspill.getReg();
            allocmap.remove(fspill.getVname());
            allocmap.put(FLI.getVname(), physReg);
            fspill.setReg(null);
            FLI.setReg(physReg);
            spillSet.add(fspill.getVname());
            FactiveList.poll();
            FactiveList.add(FLI);

        } else {
            spillSet.add((FLI.getVname()));
        }
    }


    // 外部查询
    public boolean hasPhysReg(String name) {
        if (optimize) {
            return allocmap.containsKey(name);
        }
        return false;
    }

    // 会导致global，para等情况报错
//    public boolean isSpill(String name) {
//        return spillSet.contains(name);
//    }

    public String searchPhysReg(String name) {
        assert (allocmap.containsKey(name));
        return allocmap.get(name);
    }

    // 某个函数全部使用的寄存器
    public ArrayList<String> getFuncRegUsage(String funcname) {
        return null;
    }


    // 输出调试
    private void printtest() {
        // 按start升序输出
//        OutputControl.printMessage("【Print RegAlloc】");
//        while(!unhandledList.isEmpty()){
//            OutputControl.printMessage(unhandledList.poll().toString());
//        }


        // 按end降序输出
//        while(!unhandledList.isEmpty()){
//            activeList.add(unhandledList.poll());
//        }
//        while(!activeList.isEmpty()){
//            OutputControl.printMessage(activeList.poll().toString());
//        }

//                 按start升序输出
//        OutputControl.printMessage("【Print RegAlloc】");
//        while(!FunhandledList.isEmpty()){
//            OutputControl.printMessage(FunhandledList.poll().toString());
//        }
//
//
////         按end降序输出
//        while(!FunhandledList.isEmpty()){
//            FactiveList.add(FunhandledList.poll());
//        }
//        while(!FactiveList.isEmpty()){
//            OutputControl.printMessage(FactiveList.poll().toString());
//        }

        OutputControl.printMessage("【寄存器分配映射】");
        for (Map.Entry e : allocmap.entrySet()) {
            OutputControl.printMessage(e.getKey() + " ----> " + e.getValue());
        }
        for (String spill : spillSet) {
            OutputControl.printMessage(spill + " ----> " + "spill");
        }
        OutputControl.printMessage("【本Function分配完毕】");
    }

}
