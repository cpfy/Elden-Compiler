package llvm.Instr;

import llvm.Type.Type;
import llvm.Value;

import java.util.ArrayList;

public class SIToFPInst extends Instr {
    private Type t1;
    private Type t2;
    private Value v;

    // SIToFPInst : "sitofp" Type Value "to" Type OptCommaSepMetadataAttachmentList
    public SIToFPInst(String instrname, Type t1, Type t2, Value v) {
        super(instrname);
        this.t1 = t1;
        this.t2 = t2;
        this.v = v;
    }

    @Override
    public String toString() {
        return "sitofp " + t1.toString() + " " + v.toString() + " to " + t2.toString();
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

    public void setV(Value v) {this.v = v;}
    //todo 补充

    @Override
    public void renameUses(Value newValue, Value oldValue) {

    }

    @Override
    public Value mergeConst() {
        return null;
    }

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
        return new ArrayList<>();
    }
}
