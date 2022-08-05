package llvm.Instr;

import llvm.Ident;
import llvm.Value;

public class BrTerm extends Instr {
    private Ident li;

    // "br"
    public BrTerm(String instrname, Ident li) {
        super(instrname);
        this.li = li;
    }

    @Override
    public String toString() {
        return "br label " + li.toString();
    }

    public Ident getLi() {
        return li;
    }

    public void setLi(Ident ident) {
        this.li = ident;
    }

    // 跳转name
    public String getLabelName() {
        return String.valueOf(li.getId());
    }

    @Override
    public void renameUses(Value newValue, Value oldValue) {

    }

    @Override
    public Value mergeConst() {
        return null;
    }
}
