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

    // 寄存器分配相关
    private LiveIntervals LIS;
    private HashMap<String, HashMap<String, String>> allAlloc;  // 全部的按func名索引的分配信息
    //    private HashMap<String, HashSet<String>> allSpill;
    private HashMap<String, String> allocmap;           // 从ident_name到寄存器名的映射map
    private HashSet<String> spillSet;                   // 溢出的set集合

    private PriorityQueue<LiveInterval> unhandledList;  // 未被分配寄存器的活跃区间，按照开始位置递增的方式进行排序
    private PriorityQueue<LiveInterval> activeList;     // 已经分配寄存器的活跃区间，按照结束位置递增的方式进行排序
    private PriorityQueue<String> regpool;              // 可用寄存器池

    // 浮点Reg Alloc
    private PriorityQueue<LiveInterval> FunhandledList;  // 未被分配寄存器的活跃区间，按照开始位置递增的方式进行排序
    private PriorityQueue<LiveInterval> FactiveList;     // 已经分配寄存器的活跃区间，按照结束位置递增的方式进行排序
    private PriorityQueue<String> Fregpool;              // 可用寄存器池

    // Usage
    private HashSet<String> usageReg;
    private HashSet<String> usageFReg;
    private HashMap<String, String> funcRegUsage;   // 每个函数的UseReg
    private HashMap<String, String> funcFRegUsage1;   // 每个函数的UseFReg第一部分
    private HashMap<String, String> funcFRegUsage2;   // 每个函数的UseFReg第二部分

    // 常量
    private final int REG_MAX = 12 - 4;     // 预留r0-r2,r3；r7不使用；分r4-r12共8个
    public final static String T0 = "r0";   // 标准临时寄存器用
    public final static String T1 = "r1";
    public final static String T2 = "r2";
    public final static String SB = "r3";   // 迫不得已加一个临时寄存器

    private final int FREG_MAX = 32 - 3;    // 预留s0-s2；分s3-s31共29个
    public final static String F0 = "s0";   // 标准临时寄存器用
    public final static String F1 = "s1";
    public final static String F2 = "s2";

    public static boolean optimize = true;


    public RegisterOld() {
        this.regMap = new HashMap<>();
        this.regNameMap = new HashMap<>();
        this.fregMap = new HashMap<>();
        this.fregNameMap = new HashMap<>();

        // About Reg Alloc
        this.allAlloc = new HashMap<>();
//        this.allSpill = new HashMap<>();

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

        // Usage
        this.usageReg = new HashSet<>();
        this.usageFReg = new HashSet<>();
        this.funcRegUsage = new HashMap<>();
        this.funcFRegUsage1 = new HashMap<>();
        this.funcFRegUsage2 = new HashMap<>();


        initRegMap();
        initRegnameMap();
        initFregMap();
        initFregNameMap();
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

    //查询 no -> name
    public String getRegnameFromNo(int no) {
        return regMap.get(no);
    }

    //查询 name -> no
    public int getRegnoFromName(String name) {
        return regNameMap.get(name);
    }

    // apply tmp reg.（禁用如下写法！）
//    public String applyTmp()
//    public void freeTmp(String reg)
//    public String applyFTmp()
//    public void freeFTmp(String reg)
//    public void free(String reg)

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
        // 这里由于进入HashMap，必须new新的
        this.allocmap = new HashMap<>();
        this.spillSet = new HashSet<>();

        // 这里均复用
        this.unhandledList.clear();
        this.activeList.clear();
        this.regpool.clear();
        initRegPool();

        this.FunhandledList.clear();
        this.FactiveList.clear();
        this.Fregpool.clear();
        initFRegPool();

        this.usageReg.clear();
        this.usageFReg.clear();
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

    //////////
    //////////
    // 启动全部分配
    public void RegAll() {
        for (Map.Entry<String, HashMap<String, LiveInterval>> e : LIS.getFullmap().entrySet()) {
            String fname = e.getKey();
            RegAllocScan(e.getValue());
            allAlloc.put(fname, allocmap);
            processUsageRegs(fname); // 处理usage形成的字符串
        }
    }

    private void processUsageRegs(String fname) {
        // empty直接不管，最后返回null（不可！需要手动置{r7, lr}）
        // 因为push/pop个数需要偶数？所以奇数则补充r1、s1
        int size = usageReg.size();
        if (size == 0) {
            funcRegUsage.put(fname, "{r7, lr}");

        } else {
            String pushpopstr = usageReg.toString();
            pushpopstr = pushpopstr.substring(1, pushpopstr.length() - 1) + ", r7, lr";
            if (size % 2 == 1) {
                pushpopstr += ", r1";
            }
            pushpopstr = "{" + pushpopstr + "}";
            OutputControl.printMessage("PUSH+" + pushpopstr);
            funcRegUsage.put(fname, pushpopstr);
        }

        // ？？居然要寄存器升序
        int fsize = usageFReg.size();
        if (fsize == 0) {
            return;
        }
        if (fsize <= 16) {
            String pushpopstr = usageFReg.toString();
            pushpopstr = pushpopstr.substring(1, pushpopstr.length() - 1);
            if (fsize % 2 == 1) {
                pushpopstr += ", s1";
            }
            pushpopstr = reorderS(pushpopstr);
            OutputControl.printMessage("PUSH+" + pushpopstr);
            funcFRegUsage1.put(fname, pushpopstr);

        }
        // 拆分为两部分
        else {
            HashSet<String> newset1 = new HashSet<>();
            HashSet<String> newset2 = new HashSet<>();
            int cnt = 0;
            for (String s : usageFReg) {
                if (cnt < 16) {
                    newset1.add(s);
                } else {
                    newset2.add(s);
                }
                cnt += 1;
            }

            String pushpopstr1 = newset1.toString();
            pushpopstr1 = pushpopstr1.substring(1, pushpopstr1.length() - 1);
            pushpopstr1 = reorderS(pushpopstr1);
            OutputControl.printMessage("PUSH+" + pushpopstr1);
            funcFRegUsage1.put(fname, pushpopstr1);

            String pushpopstr2 = newset2.toString();
            pushpopstr2 = pushpopstr2.substring(1, pushpopstr2.length() - 1);
            if (fsize % 2 == 1) {
                pushpopstr2 += ", s1";
            }
            pushpopstr2 = reorderS(pushpopstr2);
//            pushpopstr2 = "{" + pushpopstr2 + "}";
            OutputControl.printMessage("PUSH+" + pushpopstr2);
            funcFRegUsage2.put(fname, pushpopstr2);
        }
    }

    private String reorderS(String unorderS) {
        String[] order = unorderS.split(",");
        ArrayList<Integer> list = new ArrayList<>();
        for (String s : order) {
            list.add(Integer.parseInt(s.trim().substring(1)));
        }
        list.sort(Comparator.naturalOrder());
        String ppstr = "";
        for (int i : list) {
            ppstr += "s" + i + ", ";
        }
        ppstr = "{" + ppstr.substring(0, ppstr.length() - 2) + "}";
//        ppstr = "{" + ppstr.substring(1, ppstr.length() - 1) + "}";
        return ppstr;
    }

//    private String getPushPopStr(HashSet<String> hset){
//        int size = hset.size();
//    }

    // 启动线性扫描
    public void RegAllocScan(HashMap<String, LiveInterval> curLI) {
        clear();

        // 应为已经scan后的状态
        for (LiveInterval LI : curLI.values()) {
            if (LI.isGlobal()) {
                spillSet.add(LI.getVname());    // 可不加spillSet，不用这个判断了
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

                usageReg.add(physReg);  // 增加到usage记录
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
//                System.out.println("Assign (" + FLI.toString() + ", " + physReg + ")");
                allocmap.put(FLI.getVname(), physReg);
                FactiveList.add(FLI);

                usageFReg.add(physReg);  // 增加到usage记录
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
        ArrayList<String> list = new ArrayList<>();

        return list;
    }

    // 某个函数全部使用的寄存器
    public ArrayList<String> getFuncFRegUsage(String funcname) {
        ArrayList<String> list = new ArrayList<>();

        return list;
    }

    public void refreshAllocMap(String fname) {
        this.allocmap = this.allAlloc.get(fname);
        System.out.println("【切换到新函数：" + fname + "】");
    }

    // 查询usage接口
    public String getRegUsage(String fname) {
        return funcRegUsage.get(fname);
    }

    public String getFRegUsage1(String fname) {
        return funcFRegUsage1.get(fname);
    }

    public String getFRegUsage2(String fname) {
        return funcFRegUsage2.get(fname);
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
