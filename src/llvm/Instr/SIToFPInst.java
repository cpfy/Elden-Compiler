package llvm.Instr;

import llvm.Type.Type;
import llvm.Value;

import java.util.ArrayList;

public class SIToFPInst extends Instr{
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

    //todo 补充

    @Override
    public void renameUses(Value newValue, Value oldValue) {

    }

    @Override
    public Value mergeConst() {
        return null;
    }

    @Override
    public ArrayList<String> getUses() {
        return null;
    }

    @Override
    public String getDef() {
        return null;
    }

    @Override
    public ArrayList<String> getRoots() {
        return null;
    }
}
