package llvm.Instr;

import llvm.Type;
import llvm.Value;

public class BinaryInst extends Instr {
    private Type t;
    private Value v1;
    private Value v2;

    public BinaryInst(String instrname, Type t, Value v1, Value v2) {
        super(instrname);
        this.t = t;
        this.v1 = v1;
        this.v2 = v2;
    }

}
