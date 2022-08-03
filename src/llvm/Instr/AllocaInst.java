package llvm.Instr;

import llvm.Type.Type;

public class AllocaInst extends Instr {
    private Type t;

    public AllocaInst(String instrname, Type t) {
        super(instrname);
        this.t = t;
    }

    public Type getType(){return this.t;}
}
