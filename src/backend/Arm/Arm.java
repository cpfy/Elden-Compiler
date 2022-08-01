package backend.Arm;

public abstract class Arm {

    private String instrname;

    // 与Instr类似

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

    */

    public Arm(String instrname) {
        this.instrname = instrname;
    }

    public String getInstrname() {
        return instrname;
    }
}
