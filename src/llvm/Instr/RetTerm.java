package llvm.Instr;

import llvm.Type.Type;
import llvm.Value;

public class RetTerm extends Instr{
    private Type retype;
    private Value v;

    // "ret"
    public RetTerm(String instrname, Type retype) {
        super(instrname);
        this.retype = retype;
    }

    public RetTerm(String instrname, Type retype, Value v) {
        super(instrname);
        this.retype = retype;
        this.v = v;
    }
}
