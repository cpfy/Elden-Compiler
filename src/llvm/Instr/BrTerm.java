package llvm.Instr;

import llvm.Ident;
import llvm.TypeValue;
import llvm.Value;

import java.util.ArrayList;

public class BrTerm extends Instr {
    private Ident li;

    // "br"
    public BrTerm(String instrname, Ident li) {
        super(instrname);
        this.li = li;
    }

    @Override
    public String toString() {
        return "br label %l" + li.toString().replace("%", "");
    }

    public Ident getLi() {
        return li;
    }

    public void setLi(Ident ident) {
        this.li = ident;
    }

    // 跳转name
    public String getLabelName() {
        return String.valueOf(li.getId());
    }

    @Override
    public void renameUses(Value newValue, Value oldValue) {
        if (li.equals(oldValue.getIdent())) {
            li = newValue.getIdent();
        }
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
    public ArrayList<String> getRoots() {
        ArrayList<String> ans = new ArrayList<>();
        return ans;
    }
}
