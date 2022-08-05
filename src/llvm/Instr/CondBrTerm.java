package llvm.Instr;

import llvm.Ident;
import llvm.Instr.Instr;
import llvm.Type.IntType;
import llvm.Value;

import java.util.ArrayList;

public class CondBrTerm extends Instr {
    private IntType it;
    private Value v;
    private Ident l1;
    private Ident l2;

    // "condbr"
    public CondBrTerm(String instrname, IntType it, Value v, Ident l1, Ident l2) {
        super(instrname);
        this.it = it;
        this.v = v;
        this.l1 = l1;
        this.l2 = l2;
    }

    @Override
    public String toString() {
        return "br " + it.toString() + " " + v.toString() + ", label " + l1.toString() + ", label " + l2.toString();
    }

    public Value getV() {
        return v;
    }

    public void setV(Value v) {
        this.v = v;
    }

    public Ident getL1() {
        return l1;
    }

    public Ident getL2() {
        return l2;
    }

    // 跳转name
    public String getLabel1Name() {
        return String.valueOf(l1.getId());
    }

    public String getLabel2Name() {
        return String.valueOf(l2.getId());
    }

    public void setL1(Ident L1) {
        this.l1 = L1;
    }

    public void setL2(Ident L2) {
        this.l2 = L2;
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
