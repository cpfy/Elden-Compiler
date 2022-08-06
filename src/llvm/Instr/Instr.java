package llvm.Instr;

import backend.Variable;
import llvm.Ident;
import llvm.Value;

import java.util.ArrayList;

public abstract class Instr {

    private String instrname;   //N种中间代码种类

    /* instr所有种类如下（仅目前，可能还会增加）：

    [基本运算]: add fadd sub fsub mul fmul sdiv fdiv（均归属于BinaryInst类）
    [运算扩展]: zext
    [空间分配]: alloca
    [元素赋值]: assign  (左为元素对象，右为另一个独立Instr，例如：%123 = load i32, i32* %3)
    [存储加载]: load store
    [函数调用]: call
    [数组指针]: getelementptr
    [比较跳转]: icmp
    [跳转相关]: br condbr ret

    （新增，仅全局使用）
    [全局定义]: globaldef

    */

    public boolean global;   //是否全局

    /** add by sujunzhe start**/
    private boolean canDelete = false;  //优化中使用，如果该指令待删除，则该变量为true

    public boolean isCanDelete() {
        return canDelete;
    }

    public void setCanDelete(boolean canDelete) {
        this.canDelete = canDelete;
    }

    public abstract void renameUses(Value newValue, Value oldValue);     //常数传播优化中使用

    public abstract Value mergeConst();                                  //常量折叠使用

    public abstract ArrayList<String> getUses();                        //死代码删除使用

    public abstract String getDef();                            //死代码删除使用

    public abstract ArrayList<String> getRoots();               //死代码删除使用
    /** add by sujunzhe end**/


    private String rawstr;  //输出的ircode字符串格式
    private String IRstring;


    private String name;
    private int num;


    public boolean init = false;    //int,array是否有初始化值
    private ArrayList<Integer> initList = new ArrayList<>(); //数组的初始化值List

//    private String operator;
//    private Variable dest;      //二元运算或一元运算中的目标变量
//    private Variable oper1;     //二元运算中的第1个操作数，或一元运算的右操作数
//    private Variable oper2;     //二元运算第2个操作数

    public Instr(String instrname) {
        this.instrname = instrname;
    }

    public String getInstrname() {
        return instrname;
    }

    // 获取localident
//    public abstract Ident getLi();


//    private Type type;      // 默认type1
//    private Value value1;
//    private Value value2;
//    private Type type2;
//    private boolean isassign = false;   // 是否赋值语句
//    private Ident left;     // 赋值语句assign的左边
//    private Instr right;    // 右边的Instr


    // to arm 指令
//    public abstract ArrayList<String> toArm();


    //    private String str;     //大部分string类型串
//    private boolean addroffset = false;     //是否需要地址offset处理
//    private int offset;
//    private String prestr;
//    private String aftstr;
//
//    public boolean pushoffset = false;
//    public boolean activeRegoffset = false;
//
//    public boolean hasRetReg = false;       //有欠着的寄存器需要还掉
//    private int freeRegNumber;              //寄存器标号int no

//
//    Instr(String prestr, int offset, String aftstr, String type) {  //push 或 actreg 两种状态
//        this.prestr = prestr;
//        this.offset = offset;
//        this.aftstr = aftstr;
//
//        this.addroffset = true;
//
//        if (type.equals("push")) {
//            this.pushoffset = true;
//        } else if (type.equals("actreg")) {
//            this.activeRegoffset = true;
//        }
//    }

//    public int getFreeRegNumber() {
//        return freeRegNumber;
//    }
//
//    public boolean isAddroffset() {
//        return addroffset;
//    }
//
//    public void setFreeRegNumber(int freeRegNumber) {
//        this.freeRegNumber = freeRegNumber;
//    }


    public boolean isGlobal() {
        return global;
    }
}
