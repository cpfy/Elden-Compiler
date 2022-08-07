package llvm.Instr;

import llvm.Ident;
import llvm.Type.Type;
import llvm.Value;

import java.util.ArrayList;

public class GlobalDefInst extends Instr {
    private Ident gi;
    private Type t;
    private Value v;

    // "globaldef"
    public GlobalDefInst(String instrname, Ident gi, Type t, Value v) {
        super(instrname);
        this.gi = gi;
        this.t = t;
        this.v = v;
    }

    public Ident getGi() {
        return gi;
    }

    public Type getT() {
        return t;
    }

    public Value getV() {
        return v;
    }

    @Override
    public String toString() {
        return gi.toString() + " = dso_local global " + t.toString() + " " + v.toString();
    }

    //todo 以下全部需要

    @Override
    public void renameUses(Value newValue, Value oldValue) {
        return;
    }

    @Override
    public Value mergeConst() {
        return null;
    }

    @Override
    public ArrayList<String> getUses() {
        return null;
    }

    @Override
    public String getDef() {
        return null;
    }

    @Override
    public ArrayList<String> getRoots() {
        return null;
    }


}
