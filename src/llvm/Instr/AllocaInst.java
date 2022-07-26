package llvm.Instr;

import llvm.Type;
import llvm.Value;

public class AllocaInst extends Instr {
    private Type t;

    public AllocaInst(String instrname, Type t) {
        super(instrname);
        this.t = t;
    }
}
