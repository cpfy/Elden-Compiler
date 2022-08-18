package llvm.Instr;

import llvm.Type.Type;
import llvm.Value;

import java.util.ArrayList;

public class FPToSIInst extends Instr {
    private Type t1;
    private Type t2;
    private Value v;

    // FPToSIInst : "fptosi" Type Value "to" Type OptCommaSepMetadataAttachmentList
    public FPToSIInst(String instrname, Type t1, Type t2, Value v) {
        super(instrname);
        this.t1 = t1;
        this.t2 = t2;
        this.v = v;
    }

    private float hex2Float(String hex) {
        Long i = Long.parseLong(hex.substring(2), 16);
        Float f = Float.intBitsToFloat(i.intValue());
        return f;
    }

    private String float2Hex(float f) {
        return "0x" + Integer.toHexString(Float.floatToIntBits(f));
    }

    @Override
    public String toString() {
        return "fptosi " + t1.toString() + " " + v.toString() + " to " + t2.toString();
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

    public void setV(Value v){this.v = v;}
    //todo 补充

    @Override
    public void renameUses(Value newValue, Value oldValue) {
        if (v.isIdent() && v.getIdent().equals(oldValue.getIdent())) {
            v = newValue;
        }
    }

    @Override
    public Value mergeConst() {
        if (!v.isIdent()) {
            return new Value(String.valueOf((int) hex2Float(v.getHexVal())));
        }
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
        return new ArrayList<>();
    }
}
