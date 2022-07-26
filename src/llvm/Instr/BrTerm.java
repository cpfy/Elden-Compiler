package llvm.Instr;

import llvm.Ident;

public class BrTerm extends Instr {
    private Ident li;

    // "br"
    public BrTerm(String instrname, Ident li) {
        super(instrname);
        this.li = li;
    }
}
