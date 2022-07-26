package llvm;

import llvm.Instr.Instr;

public class CondBrTerm extends Instr {
    private Value v;
    private Ident l1;
    private Ident l2;

    // "condbr"
    public CondBrTerm(String instrname, Value v, Ident l1, Ident l2) {
        super(instrname);
        this.v = v;
        this.l1 = l1;
        this.l2 = l2;
    }
}
