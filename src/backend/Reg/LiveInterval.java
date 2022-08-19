package backend.Reg;

// 在开始执行线性扫描算法前，要先获得 live interval，算法分配寄存器都是给 live interval 分配的
public class LiveInterval implements Comparable<LiveInterval> {

    private int start;
    private int end;
    private boolean active = false; // 是否已激活、有start

    //    private LiveRange LR;   // 表明活跃区间为[start, end)
    private String vname;   // 该LI对应的变量名


    private Register reg;   // 被分配的寄存器？

    public LiveInterval(String vname) {
        this.vname = vname;
    }

    // 输出测试用
    @Override
    public String toString() {
        return "<" + vname + "> active interval: [" + start + ", " + end + ")";
    }

    @Override
    public int compareTo(LiveInterval other) {
        return Integer.compare(start, other.start);
    }
//
//    public int getStart() {
//        return start;
//    }
//
//    public int getEnd() {
//        return end;
//    }

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

    // 增加使用位置
    public void addUsePos(int pos) {
        if (!active) {
            active = true;
            start = end = pos;

        } else {
            // 无语子，得开-ea设置，默认不生效的
//            assert (start < pos) : "[LiveInterval] new pos must > start interval!";

            // 有可能先扫编号大的
            if (pos < start) {
                start = pos;

            } else {
                end = pos;
            }
        }
    }


}