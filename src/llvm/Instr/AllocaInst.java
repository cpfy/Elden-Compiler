package llvm.Instr;

import llvm.Type.Type;
import llvm.Value;

import java.util.ArrayList;
import java.util.HashMap;

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

    @Override
    public ArrayList<String> getUses() {
        ArrayList<String> ans = new ArrayList<>();
        return ans;
    }

    @Override
    public String getDef() {
        return null;
    }

    @Override
    public HashMap<String, Boolean> getUsesAndTypes() {
        return new HashMap<>();
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
