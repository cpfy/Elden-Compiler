package backend.Reg;

public class LiveInterval {

    private Register reg;

    public LiveInterval() {

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


}
