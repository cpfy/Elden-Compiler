package llvm.Instr;

import llvm.Type.Type;
import llvm.Value;

import java.util.ArrayList;
import java.util.HashMap;

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

    public Type getT1() {
        return t1;
    }

    public Type getT2() {
        return t2;
    }

    @Override
    public String toString() {
        return "zext " + t1.toString() + " " + v.toString() + " to " + t2.toString();
    }

    public Value getV() {
        return this.v;
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
        if (!v.isIdent()) {
            return new Value(v.getVal());
        }
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
    public HashMap<String, Boolean> getUsesAndTypes() {
        HashMap<String, Boolean> ans = new HashMap<>();
        if (v.isIdent()) {
            ans.put(v.getIdent().toString(), false);
        }

        return ans;
    }

    @Override
    public HashMap<String, Boolean> getDefAndType() {
        return new HashMap<>();
    }

    @Override
    public ArrayList<String> getRoots() {
        ArrayList<String> ans = new ArrayList<>();
        return ans;
    }

    @Override
    public boolean setAssignType() {
        return false;
    }
}
