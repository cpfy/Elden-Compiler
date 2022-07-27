package llvm.Instr;

import llvm.Ident;

public class BrTerm extends Instr {
    private Ident li;

    // "br"
    public BrTerm(String instrname, Ident li) {
        super(instrname);
        this.li = li;
    }

    public Ident getLi() {
        return li;
    }

    // 跳转name
    public String getLabelName(){
        return String.valueOf(li.getId());
    }
}
