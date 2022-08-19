package backend.Reg;

// 在开始执行线性扫描算法前，要先获得 live interval，算法分配寄存器都是给 live interval 分配的
public class LiveInterval {

//    private int start;
//    private int end;

    private LiveRange LR;   // 表明活跃区间为[start, end)
    private String vname;   // 该LI对应的变量名


    private Register reg;   // 被分配的寄存器？

    public LiveInterval(String vname) {
        this.vname = vname;
    }

    public Register getReg() {
        return reg;
    }

    // overlaps - Return true if the live range overlaps an interval specified
    // by [Start, End).
    public boolean overlaps(int Start, int End) {
        assert (Start < End);
//        const_iterator I = lower_bound(*this, End);
//        return I != begin() && (--I)->end > Start;
        return true;
    }

    public boolean covers(LiveRange Other) {
        return true;//todo
    }

    public void clear() {
    }


}
