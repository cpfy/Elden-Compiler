package llvm.Instr;

import llvm.Type.Type;
import llvm.Value;

public class LoadInst extends Instr {
    private Type t1;
    private Type t2;
    private Value v;

    public LoadInst(String instrname, Type t1, Type t2, Value v) {
        super(instrname);
        this.t1 = t1;
        this.t2 = t2;
        this.v = v;
    }

    @Override
    public String toString() {
        return "load " + t1.toString() + ", " + t2.toString() + " " + v.toString();
    }

    public Value getV() {
        return v;
    }
}
