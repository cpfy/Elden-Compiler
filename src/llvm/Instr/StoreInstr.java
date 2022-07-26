package llvm.Instr;

import llvm.Type;
import llvm.Value;

public class StoreInstr extends Instr{
    private Type t1;
    private Type t2;
    private Value v1;
    private Value v2;

    public StoreInstr(String instrname, Type t1, Type t2, Value v1, Value v2) {
        super(instrname);
        this.t1 = t1;
        this.t2 = t2;
        this.v1 = v1;
        this.v2 = v2;
    }
}
