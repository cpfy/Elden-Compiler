package llvm.Instr;

import llvm.Type.Type;
import llvm.Value;

import java.util.ArrayList;

public class StoreInstr extends Instr {
    private Type t1;
    private Type t2;
    private Value v1;
    private Value v2;
    private boolean hidden = false;
    public StoreInstr(String instrname, Type t1, Type t2, Value v1, Value v2) {
        super(instrname);
        this.t1 = t1;
        this.t2 = t2;
        this.v1 = v1;
        this.v2 = v2;
    }

    @Override
    public String toString() {
        if(hidden) return "";
        return "store " + t1.toString() + " " + v1.toString() + ", " + t2.toString() + " " + v2.toString();
    }

    public Type getT1() {
        return t1;
    }

    public Type getT2() {
        return t2;
    }

    public Value getV1() {
        return this.v1;
    }

    public Value getV2() {
        return this.v2;
    }

    public void setHidden(boolean hidden){this.hidden = hidden;}

    @Override
    public void renameUses(Value newValue, Value oldValue) {
        if (v1.isIdent() && v1.getIdent().equals(oldValue.getIdent())) {
            v1 = newValue;
        }
        if (v2.isIdent() && v2.getIdent().equals(oldValue.getIdent())) {
            v2 = newValue;
        }
    }

    @Override
    public Value mergeConst() {
        return null;
    }

    @Override
    public ArrayList<String> getUses() {
        ArrayList<String> ans = new ArrayList<>();
        if (v1.isIdent()) {
            ans.add(v1.getIdent().toString());
        }
        if (v2.isIdent()) {
            ans.add(v2.getIdent().toString());
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
        if (v1.isIdent()) {
            ans.add(v1.getIdent().toString());
        }
        if (v2.isIdent()) {
            ans.add(v2.getIdent().toString());
        }
        return ans;
    }
}
