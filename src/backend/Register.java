package backend;

import java.util.ArrayList;
import java.util.HashMap;

public class Register {
    private HashMap<Integer, String> regMap;
    private HashMap<String, Integer> regNameMap;

    private ArrayList<Integer> freeRegList;
    private HashMap<Integer, Variable> varAllocMap;
    private ArrayList<Integer> activeRegList;   //当前活跃的变量占用的、已分配出的reg
    private ArrayList<Variable> varRegUsageList;

    public Register() {
        this.regMap = new HashMap<>();
        this.regNameMap = new HashMap<>();

        this.freeRegList = new ArrayList<>();
        this.varAllocMap = new HashMap<>();
        this.activeRegList = new ArrayList<>();

        initRegMap();
        initRegnameMap();
        initFreeRegList();
    }

    private void initRegMap() {
        regMap.put(0, "r0");
        regMap.put(1, "r1");
        regMap.put(2, "r2");
        regMap.put(3, "r3");
        regMap.put(4, "r4");
        regMap.put(5, "r5");
        regMap.put(6, "r6");
        regMap.put(7, "r7");
        regMap.put(8, "r8");
        regMap.put(9, "r9");
        regMap.put(10, "r10");
        regMap.put(11, "r11");
        regMap.put(12, "r12");
        regMap.put(13, "sp");   // Stack pointer
        regMap.put(14, "lr");   // Link Register
        regMap.put(15, "pc");   // pro.. count
    }

    private void initRegnameMap() {
        regNameMap.put("r0", 0);
        regNameMap.put("r1", 1);
        regNameMap.put("r2", 2);
        regNameMap.put("r3", 3);
        regNameMap.put("r4", 4);
        regNameMap.put("r5", 5);
        regNameMap.put("r6", 6);
        regNameMap.put("r7", 7);
        regNameMap.put("r8", 8);
        regNameMap.put("r9", 9);
        regNameMap.put("r10", 10);
        regNameMap.put("r11", 11);
        regNameMap.put("r12", 12);
        regNameMap.put("sp", 13);
        regNameMap.put("lr", 14);
        regNameMap.put("pc", 15);
    }

    private void initFreeRegList() {
        for (int i = 8; i < 32; i++) {
            if (i != 29 && i != 31) {
                freeRegList.add(i);
            }
        }
    }

    //查询 no -> name
    public String getRegisterNameFromNo(int no) {
        return regMap.get(no);
    }

    //查询 name -> no
    public int getRegisterNoFromName(String name) {
        return regNameMap.get(name);
    }

    //临时变量-申请寄存器
    public String applyRegister(Variable v) {
        int no;

        if (!freeRegList.isEmpty()) {
            no = freeRegList.get(0);

            varAllocMap.put(no, v);
            freeRegList.remove(0);
            v.setCurReg(no);
            addActiveListNoRep(no);     //无重复加入activeregList 活跃变量表

        } else {    //todo 无空寄存器
            System.err.println("No free Reg! Alloc $v1.");
            no = 3;
        }

        System.out.println("Alloc Reg $" + regMap.get(no) + " to variable " + v.toString());

        return regMap.get(no);
    }

    //定义变量-申请寄存器
    public String applyRegister(Symbol s) {
        int no;
        if (!freeRegList.isEmpty()) {
            no = freeRegList.get(0);

            freeRegList.remove(0);
            s.setCurReg(no);
            addActiveListNoRep(no);     //无重复加入activeregList 活跃变量表

        } else {    //todo 无空寄存器
            System.err.println("No free Reg! Alloc $v1.");
            no = 3;
        }

        System.out.println("Alloc Reg $" + regMap.get(no) + " to symbol " + s.getName());
        return regMap.get(no);
    }

    //申请临时存器
    public int applyTmpRegister() {
        int regno;
        if (!freeRegList.isEmpty()) {
            regno = freeRegList.get(0);
            freeRegList.remove(0);
            addActiveListNoRep(regno);     //无重复加入activeregList 活跃变量表

        } else {    //todo 无空寄存器,分$v1
            System.err.println("No free Reg! Alloc $v1.");
            regno = 3;
        }

        System.out.println("Alloc Reg $" + regMap.get(regno) + " to Tmp");
        return regno;
    }

    public void freeTmpRegister(int regno) {
//        if (regno < 8 || regno == 29 || regno == 31) {
//            System.err.println("Register freeTmpRegister() : Error free tmp Reg No!! regno = " + regno);
//
//        }
        if(regno > 15){
            System.err.println("Register freeTmpRegister() : Error free tmp Reg No!! regno = " + regno);
        }

        else if (!freeRegList.contains(regno)) {
            removeActiveRegList(regno);     //删除变量in activeregList 活跃变量表
            freeRegList.add(regno);
            System.out.println("Free Reg $" + regMap.get(regno) + " from Tmp");
        }//todo 其它的free寄存器都得检查是否重复！
    }

    public void freeTmpRegisterByName(String regname) {
        int regno = regNameMap.get(regname);
        freeTmpRegister(regno);
    }

    //释放寄存器
    public void freeRegister(Variable v) {
        if (v.isKindofsymbol()) {
            Symbol s = v.getSymbol();
            Assert.check(s);
            int regno = s.getCurReg();
            //freeRegList.add(regno);
            freeTmpRegister(regno);

            s.setCurReg(-1);    //reg使用状态回到未分配的-1

            removeActiveRegList(regno);     //删除变量in activeregList 活跃变量表
            System.out.println("Free Reg $" + regMap.get(regno) + " from " + s.getName());

        } else {
            int regno = v.getCurReg();
            //freeRegList.add(regno);
            freeTmpRegister(regno);
            removeActiveRegList(regno);     //删除变量in activeregList 活跃变量表
            System.out.println("Free Reg $" + regMap.get(regno) + " from " + v.toString());
        }
    }


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

    //reset全部寄存器状态
    public void resetAllReg() {
        freeRegList.clear();
        varAllocMap.clear();
        activeRegList.clear();

        initFreeRegList();
    }
}
