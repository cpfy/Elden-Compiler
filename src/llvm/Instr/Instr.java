package llvm.Instr;

import backend.Variable;
import llvm.Ident;
import llvm.Type;
import llvm.Value;

import java.util.ArrayList;

public class Instr {
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
//    Instr(String str) {
//        this.str = str;
//    }
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
//
//    public String getStr() {
//        return str;
//    }
//
//    public int getFreeRegNumber() {
//        return freeRegNumber;
//    }
//
//    public boolean isAddroffset() {
//        return addroffset;
//    }
//
//    //set
//
//    public void setFreeRegNumber(int freeRegNumber) {
//        this.freeRegNumber = freeRegNumber;
//    }
//
//    public void setAddroffset(boolean addroffset) {
//        this.addroffset = addroffset;
//    }
//
//    public void setOffset(int offset) {
//        this.offset = offset;
//    }
//
//    public String toString(int activeregoffset) {       //分为pushoffset与actregoffset两类
//        if (addroffset) {
//            offset += activeregoffset;
//            return prestr + offset + aftstr;
//        }
//        return str;
//    }
//
//    public String toString() {
//        return str;
//    }

    private String instrname;   //N种中间代码种类
    private Type type;      // 默认type1
    private Value value1;
    private Value value2;
    private Type type2;
    private boolean isassign = false;   // 是否赋值语句
    private Ident left;     // 赋值语句assign的左边
    private Instr right;    // 右边的Instr


    private String rawstr;  //输出的ircode字符串格式
    private String IRstring;


    private String name;
    private int num;

    public boolean global;   //是否全局
    public boolean init = false;    //int,array是否有初始化值
    private ArrayList<Integer> initList = new ArrayList<>(); //数组的初始化值List
//    public boolean voidreturn;
//
//    private Variable variable;  //含有表达式等情况时，对应的Variable类型
//
//    private int array1;     //数组形式时第1维的大小
//    private int array2;     //数组形式时第2维的大小

    private String operator;
    private Variable dest;      //二元运算或一元运算中的目标变量
    private Variable oper1;     //二元运算中的第1个操作数，或一元运算的右操作数
    private Variable oper2;     //二元运算第2个操作数

//    private Symbol symbol;  //含有表达式等情况时，对应的symbol类型的符号
//    private SymbolTable.Scope scope;    //todo inblockoffset用到

    public Instr(String instrname){
        this.instrname = instrname;
    }

    public Instr(String instrname, Type t, Value v1, Value v2) {
        this.instrname = instrname;
        this.type = t;
        this.value1 = v1;
        this.value2 = v2;
    }

    public Instr(String instrname, Type t1, Value v1, Type t2, Value v2) {
        this.instrname = instrname;
        this.type = t1;
        this.value1 = v1;
        this.type2 = t2;
        this.value2 = v2;
    }

    public Instr(String instrname, Ident left, Instr right) {
        this.instrname = instrname;
        this.left = left;
        this.right = right;

        this.isassign = true;
    }

    public Instr(String instrname, Type t1, Type t2, Value v) {
        this.instrname = instrname;
        this.type = t1;
        this.type2 = t2;
        this.value1 = v;
    }


    // to arm
    private void convertArm() {

    }

    private void ValueInstruction() {
        String sym = "";
        switch (sym) {
            case "add":
                AddInst();
                break;
            case "fadd":
                FAddInst();
                break;
            case "sub":
                SubInst();
                break;
            case "fsub":
                FSubInst();
                break;
            case "mul":
                MulInst();
                break;
            case "fmul":
                FMulInst();
                break;
            case "sdiv":
                SDivInst();
                break;
            case "fdiv":
                FDivInst();
                break;
            default:
                break;
        }
    }

    private void FAddInst() {
    }

    private void SubInst() {
    }

    private void FSubInst() {
    }

    private void MulInst() {
    }

    private void FMulInst() {
    }

    private void SDivInst() {
    }

    private void FDivInst() {
    }

    private void AddInst() {
    }
}
