package llvm.Instr;

import llvm.Ident;
import llvm.Value;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class AssignInstr extends Instr {
    private boolean isFloat;
    private Ident localident;
    private Instr valueinstr;
    private boolean isPointer = false;
    // LocalIdent "=" ValueInstruction
    public AssignInstr(String instrname, Ident localident, Instr valueinstr) {
        super(instrname);
        this.localident = localident;
        this.valueinstr = valueinstr;
        isFloat = valueinstr.setAssignType();
    }

    @Override
    public String toString() {
        return localident.toString() + " = " + valueinstr.toString();
    }

    public Ident getIdent() {
        return localident;
    }

    //public void setDeleted(boolean deleted){this.deleted = deleted;}
    public void setIdent(Ident localident) {
        this.localident = localident;
    }

    public Instr getValueinstr() {
        return valueinstr;
    }

    @Override
    public void renameUses(Value newValue, Value oldValue) {
        valueinstr.renameUses(newValue, oldValue);
    }

    @Override
    public Value mergeConst() {
        return valueinstr.mergeConst();
    }

    @Override
    public ArrayList<String> getUses() {
        return valueinstr.getUses();
    }

    @Override
    public String getDef() {
        return localident.toString();
    }

    @Override
    public HashMap<String, Boolean> getUsesAndTypes() {
        return valueinstr.getUsesAndTypes();
    }

    @Override
    public HashMap<String, Boolean> getDefAndType() {
        HashMap<String, Boolean> ans = new HashMap<>();
        ans.put(localident.getName(), isFloat);
        return null;
    }

    @Override
    public ArrayList<String> getRoots() {
        ArrayList<String> ans = new ArrayList<>();
        if (valueinstr instanceof CallInst) {
            ans.add(localident.toString());
        }
        ans.addAll(valueinstr.getRoots());
        return ans;
    }

    @Override
    public boolean setAssignType() {
        System.out.println("ERROR!!!");
        return false;
    }
}
