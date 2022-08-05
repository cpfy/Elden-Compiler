package llvm.Instr;

import llvm.Ident;
import llvm.Value;

import java.util.ArrayList;

public class AssignInstr extends Instr {
    private Ident localident;
    private Instr valueinstr;

    // LocalIdent "=" ValueInstruction
    public AssignInstr(String instrname, Ident localident, Instr valueinstr) {
        super(instrname);
        this.localident = localident;
        this.valueinstr = valueinstr;
    }

    @Override
    public String toString() {
        return localident.toString() + " = " + valueinstr.toString();
    }

    public Ident getIdent() {
        return localident;
    }

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
    public ArrayList<String> getRoots() {
        ArrayList<String> ans = new ArrayList<>();
        if (valueinstr instanceof CallInst) {
            ans.add(localident.toString());
        }
        ans.addAll(valueinstr.getRoots());
        return ans;
    }
}
