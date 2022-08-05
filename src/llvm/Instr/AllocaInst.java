package llvm.Instr;

import llvm.Type.Type;
import llvm.Value;

public class AllocaInst extends Instr {
    private Type t;

    public AllocaInst(String instrname, Type t) {
        super(instrname);
        this.t = t;
    }

    @Override
    public String toString() {
        return "alloca " + t.toString();
    }

    public Type getT() {
        return t;
    }

    public Type getType() {
        return this.t;
    }


    @Override
    public void renameUses(Value newValue, Value oldValue) {

    }

    @Override
    public Value mergeConst() {
        return null;
    }
}
