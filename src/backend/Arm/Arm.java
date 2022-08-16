package backend.Arm;

public abstract class Arm {

    private String instrname;
    private boolean withtab = false;    // 是否有tab前缀，默认false

    // 与Instr类似

    /* Arm所有种类如下（仅目前，可能还会增加）：

    [.extern等头]: head
    [位移]：mov，mov+后缀(如moveq),vmov,vmovw,vmovt
    [比较]: vcmp.f32,cmp
    [状态]: vmrs
    [类型转换]： vcvt.s32.f32
    [运算]：mul,add,sub,sdiv
    [浮点运算]：vadd.f32,vsub.f32,vmul.f32,v?
    [跳转]：bne，beq等
    [标签]：label

    [临时]:tmp。暂时给ldr，str过渡

    */

    public Arm(String instrname) {
        this.instrname = instrname;
    }

    public String getInstrname() {
        return instrname;
    }

    public boolean isWithtab() {
        return withtab;
    }

    public void setWithtab(boolean withtab) {
        this.withtab = withtab;
    }
}
