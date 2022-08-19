package backend.Reg;

public class LiveRange {

    // 表示LiveInterval范围的数据结构
    private int start;
    private int end;

    public LiveRange(int start, int end) {
        this.start = start;
        this.end = end;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }
}
