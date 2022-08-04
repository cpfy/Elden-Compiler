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

    @Override
    public String toString() {
        return localident.toString() + " = " + valueinstr.toString();
    }

    public Ident getIdent() {
        return localident;
    }

    public void setIdent(Ident localident) {
        this.localident = localident;
    }

    public Instr getValueinstr() {
        return valueinstr;
    }
}
