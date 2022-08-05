package llvm.Instr;

import llvm.Ident;
import llvm.Value;

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

    @Override
    public void renameUses(Value newValue, Value oldValue) {
        valueinstr.renameUses(newValue, oldValue);
    }

    @Override
    public Value mergeConst() {
        return valueinstr.mergeConst();
    }
}
