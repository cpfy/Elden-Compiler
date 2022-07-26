package llvm.Instr;

import llvm.Ident;

public class AssignInstr extends Instr {
    private Ident localident;
    private Instr valueinstr;

    // LocalIdent "=" ValueInstruction
    public AssignInstr(String instrname, Ident localident, Instr valueinstr) {
        super(instrname);
        this.localident = localident;
        this.valueinstr = valueinstr;
    }
}
