package backend.Reg;

import llvm.Ident;

import java.util.ArrayList;
import java.util.HashMap;

public class RegisterOld {
    private HashMap<Integer, String> regMap;
    private HashMap<String, Integer> regNameMap;
    private HashMap<Integer, String> fregMap;   // 浮点寄存器值
    private HashMap<String, Integer> fregNameMap;   // 浮点寄存器值

    private ArrayList<Integer> freeRegList;
    private ArrayList<Integer> freeFloatRegList;
    private HashMap<String, Integer> identAllocMap;
    private ArrayList<Integer> activeRegList;   //当前活跃的变量占用的、已分配出的reg
    private ArrayList<Ident> identRegUsageList;

    public RegisterOld() {
        this.regMap = new HashMap<>();
        this.regNameMap = new HashMap<>();
        this.fregMap = new HashMap<>();
        this.fregNameMap = new HashMap<>();

        this.freeRegList = new ArrayList<>();
        this.freeFloatRegList = new ArrayList<>();
        this.identAllocMap = new HashMap<>();
        this.activeRegList = new ArrayList<>();

        initRegMap();
        initRegnameMap();
        initFregMap();
        initFregNameMap();
        initFreeRegList();
        initFreeFloatRegList();
    }

    private void initRegMap() {
        // regMap.put(0, "r0");
        for (int i = 0; i <= 12; i++) {
            regMap.put(i, "r" + i);
        }
        regMap.put(13, "sp");   // Stack pointer
        regMap.put(14, "lr");   // Link Register
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

    //临时变量-申请寄存器
    public String applyRegister(Ident i) {
        int no;

        if (!freeRegList.isEmpty()) {
            no = freeRegList.get(0);

            // 映射名称
            String mapname = i.getMapname();
            identAllocMap.put(mapname, no);
            freeRegList.remove(0);

//            System.out.println("Set " + i.toString() + "(" + i.getMapname() + ")" + " regno " + no);
            addActiveListNoRep(no);     //无重复加入activeregList 活跃变量表

        } else {    //todo 无空寄存器
            System.err.println("No free Reg! Alloc $r0.");
            no = 0;
        }

//        System.out.println("Info: Alloc Reg $" + regMap.get(no) + " to Ident " + i.toString());

        return regMap.get(no);
    }

    // apply tmp reg.
    public String applyTmp() {
        int regno;
        if (!freeRegList.isEmpty()) {
            regno = freeRegList.get(0);
            freeRegList.remove(0);
            addActiveListNoRep(regno);     //无重复加入activeregList 活跃变量表

        } else {    //todo 无空寄存器
            System.err.println("No free Reg! Alloc $r0.");
            regno = 0;
        }

//        System.out.println("Alloc Reg $" + regMap.get(regno) + " to Tmp");
        return this.regMap.get(regno);
    }

    // free tmp reg.
    public void freeTmp(String reg) {
        int no = this.regNameMap.get(reg);
        if (no > 12) {
            System.err.println("Register freeTmpRegister() : Error free tmp Reg No!! regno = " + no);
        } else if (!freeRegList.contains(no)) {
            removeActiveRegList(no);     //删除变量in activeregList 活跃变量表
            freeRegList.add(no);
//            System.out.println("Free Reg $" + regMap.get(no) + " from Tmp");
        }
        //todo 其它的free寄存器都得检查是否重复！
    }

    // apply tmp float reg.
    public String applyFTmp() {
        int regno;
        if (!freeFloatRegList.isEmpty()) {
            regno = freeFloatRegList.get(0);
            freeFloatRegList.remove(0);

            // 此时还没用到active
            addActiveListNoRep(regno);     //无重复加入activeregList 活跃变量表

        } else {    //todo 无空寄存器
            System.err.println("No free float Reg! Alloc $s0.");
            regno = 32;
        }

//        System.out.println("Alloc Float Reg $" + fregMap.get(regno) + " to Tmp");
        return this.fregMap.get(regno);
    }

    // free float tmp reg.
    public void freeFTmp(String reg) {
        int no = this.fregNameMap.get(reg);
        assert no >= 32 && no < 64;

        if (!freeFloatRegList.contains(no)) {
            removeActiveRegList(no);     //删除变量in activeregList 活跃变量表
            freeFloatRegList.add(no);
            System.out.println("Free Float Reg $" + fregMap.get(no) + " from Tmp");
        }
        //todo 其它的free寄存器都得检查是否重复
    }

    // free任意float或int寄存器
    public void free(String reg) {
        if (reg.charAt(0) == 's') {    // 必不可能是sp
            freeFTmp(reg);
        } else {
            freeTmp(reg);
        }
    }


    //reset全部寄存器状态
    public void resetAllReg() {
        freeRegList.clear();
        identAllocMap.clear();
        activeRegList.clear();

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


    /***** 以下暂未用到 *****/
    //查询是否有空闲寄存器
    public boolean hasSpareRegister() {
        return !freeRegList.isEmpty();
    }

    //查询是否需保存现场，active内有内容？
    public ArrayList<Integer> getActiveRegList() {
        return activeRegList;
    }

    private void addActiveListNoRep(int no) {
        if (!activeRegList.contains(no)) {
            activeRegList.add(no);
        }
    }

    //删除变量in activeregList 活跃变量表
    private void removeActiveRegList(int no) {
        activeRegList.removeIf(i -> i == no);
    }

    // name是否已经分配reg
    public boolean allocated(String name) {
        return identAllocMap.containsKey(name);
    }

}