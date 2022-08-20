package backend.Reg;

// 在开始执行线性扫描算法前，要先获得 live interval，算法分配寄存器都是给 live interval 分配的
public class LiveInterval implements Comparable<LiveInterval> {

    private int start;
    private int end;
    private boolean active = false; // 是否已激活、有start

    //    private LiveRange LR;   // 表明活跃区间为[start, end)（废弃）

    private String vname;   // 该LI对应的变量名
    private String reg;   // 被分配的phys寄存器

    private boolean varf = false;    // 是否是一个float变量

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

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public String getReg() {
        return reg;
    }

    public boolean isGlobal() {
        return vname.charAt(0) == '@';
    }

    public boolean isVarf() {
        return varf;
    }

    public void setVarf(boolean varf) {
        this.varf = varf;
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

            } else if (pos > end) {
                end = pos;
            }
        }
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

    public String getVname() {
        return vname;
    }

    // 暂时没用
    public void setReg(String reg) {
        this.reg = reg;
    }

    // 区间长度，可作为排序参考权重
    public int getLength() {
        return end - start;
    }
}
