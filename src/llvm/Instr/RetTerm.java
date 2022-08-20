package llvm.Instr;

import llvm.Type.Type;
import llvm.Value;

import java.util.ArrayList;
import java.util.HashMap;

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
        if (this.v == null) {
            return "ret " + retype.toString();
        }
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
        if (v == null) {
            return;
        }
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
        if (v != null && v.isIdent()) {
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
        if (v != null && v.isIdent()) {
            ans.put(v.getIdent().toString(), retype.isFloat());
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
        if (v != null && v.isIdent()) {
            ans.add(v.getIdent().toString());
        }
        return ans;
    }

    @Override
    public boolean setAssignType() {
        System.err.println("ERROR!!");
        return false;
    }
}
