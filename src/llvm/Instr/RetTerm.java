package llvm.Instr;

import llvm.Type.Type;
import llvm.Value;

public class RetTerm extends Instr {
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

    @Override
    public String toString() {
        return "ret " + retype.toString() + " " + v.toString();
    }

    public Type getRetype() {
        return retype;
    }

    public Value getV() {
        return v;
    }

    public void setV(Value v) {
        this.v = v;
    }

    @Override
    public void renameUses(Value newValue, Value oldValue) {
        if (v.isIdent() && v.getIdent().equals(oldValue.getIdent())) {
            v = newValue;
        }
    }

    @Override
    public Value mergeConst() {
        return null;
    }


}
