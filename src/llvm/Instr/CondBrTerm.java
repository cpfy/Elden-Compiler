package llvm.Instr;

import llvm.Ident;
import llvm.Type.IntType;
import llvm.Value;

import java.util.ArrayList;

public class CondBrTerm extends Instr {
    private IntType it;
    private Value v;
    private Ident i1;
    private Ident i2;

    // "condbr"
    public CondBrTerm(String instrname, IntType it, Value v, Ident i1, Ident i2) {
        super(instrname);
        this.it = it;
        this.v = v;
        this.i1 = i1;
        this.i2 = i2;
    }

    @Override
    public String toString() {
        return "br " + it.toString() + " " + v.toString() + ", label %l" + i1.toString().replace("%", "") + ", label %l" + i2.toString().replace("%", "");
    }

    public IntType getIt() {
        return it;
    }

    public Value getV() {
        return v;
    }

    public void setV(Value v) {
        this.v = v;
    }

    public Ident getI1() {
        return i1;
    }

    public Ident getI2() {
        return i2;
    }

    // 跳转name
    public String getLabel1Name() {
        return String.valueOf(i1.getId());
    }

    public String getLabel2Name() {
        return String.valueOf(i2.getId());
    }

    public void setI1(Ident i1) {
        this.i1 = i1;
    }

    public void setI2(Ident i2) {
        this.i2 = i2;
    }

    @Override
    public void renameUses(Value newValue, Value oldValue) {
        if (v.isIdent() && v.getIdent().equals(oldValue.getIdent())) {
            v = newValue;
        }
        else if (i1.equals(oldValue.getIdent())) {
            i1 = newValue.getIdent();
        }
        else if (i2.equals(oldValue.getIdent())) {
            i2 = newValue.getIdent();
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
