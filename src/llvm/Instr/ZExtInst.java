package llvm.Instr;

import llvm.Type.Type;
import llvm.Value;

public class ZExtInst extends Instr {
    private Type t1;
    private Type t2;
    private Value v;

    public ZExtInst(String instrname, Type t1, Type t2, Value v) {
        super(instrname);
        this.t1 = t1;
        this.t2 = t2;
        this.v = v;
    }
    public Value getV(){return this.v;}
    public void setV(Value v){this.v = v;}
}
