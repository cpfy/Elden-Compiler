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

    （新增）
    [全局定义]: globaldef（仅全局使用）
    [浮点]: sitofp, fptosi, fcmp

    */

    public boolean global;  // 是否全局
    public int no;          // 指令编号（reg alloc用）

    /**
     * add by sujunzhe start
     **/
    private int instrNo;       //按照dfs给指令编号，用于寄存器分配

    public int getInstrNo() {
        return instrNo;
    }

    public void setInstrNo(int instrNum) {
        this.instrNo = instrNum;
    }

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

    /**
     * add by sujunzhe end
     **/

    // 应该也无用
    private String name;
    private int num;

//    private String rawstr;  //输出的ircode字符串格式
//    private String IRstring;
//    public boolean init = false;    //int,array是否有初始化值
//    private ArrayList<Integer> initList = new ArrayList<>(); //数组的初始化值List

    public static boolean isTesting;

    public Instr(String instrname) {
        this.instrname = instrname;
    }

    public String getInstrname() {
        return instrname;
    }

    public boolean isGlobal() {
        return global;
    }
}
