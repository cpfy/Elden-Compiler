package llvm.Instr;

import llvm.Type.Type;
import llvm.Value;

import java.util.ArrayList;

public class LoadInst extends Instr {
    private Type t1;
    private Type t2;
    private Value v;

    // %2 = load i32, i32* %1
    // %3 = load i32, i32* @b
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

    public Type getT1() {
        return t1;
    }

    public Type getT2() {
        return t2;
    }

    public Value getV() {
        return v;
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

    @Override
    public ArrayList<String> getUses() {
        ArrayList<String> ans = new ArrayList<>();
        if (v.isIdent()) {
            ans.add(v.getIdent().toString());
        }
        return ans;
    }

    @Override
    public String getDef() {
        return null;
    }

    @Override
    public ArrayList<String> getRoots() {
        ArrayList<String> ans = new ArrayList<>();
        if (v.isIdent()) {
            ans.add(v.getIdent().toString());
        }
        return ans;
    }
}
