package llvm.Instr;

import llvm.Type.Type;
import llvm.TypeValue;
import llvm.Value;

import java.util.ArrayList;
import java.util.HashMap;


//    GetElementPtrInst
//	: "getelementptr" OptInBounds Type "," Type Value OptCommaSepMetadataAttachmentList
//	| "getelementptr" OptInBounds Type "," Type Value "," CommaSepTypeValueList OptCommaSepMetadataAttachmentList

public class GetElementPtrInst extends Instr {
    private Type t1;
    private Type t2;
    private Value v;

    private boolean hasFourth;  // 是否有第4个
    private ArrayList<TypeValue> commas;    //前端保证必为1个或2个

    public GetElementPtrInst(String instrname, Type t1, Type t2, Value v, ArrayList<TypeValue> commas) {
        super(instrname);
        this.t1 = t1;
        this.t2 = t2;
        this.v = v;
        this.commas = commas;
        this.hasFourth = (commas.size() == 2);
    }

    @Override
    public String toString() {
        String ans = "getelementptr inbounds " + t1.toString() + ", " + t2.toString() + " " + v.toString() + ", i32 " +  commas.get(0).getValue().toString();
        if (hasFourth) {
            ans += ", i32 " +  commas.get(1).getValue().toString();
        }
        return ans;
    }

    public void setValue(Value i) {
        this.v = i;
    }

    public Value getV() {
        return this.v;
    }

    public Type getT1() {
        return t1;
    }

    public Type getT2() {
        return t2;
    }

    public boolean hasFourth() {
        return hasFourth;
    }

    public ArrayList<TypeValue> getCommas() {
        return commas;
    }

    // commas里设定为t3 v3, t4 v4
    public Type getT3() {
        return commas.get(0).getType();
    }

    public Value getV3() {
        return commas.get(0).getValue();
    }

    public Type getT4() {
        return commas.get(1).getType();
    }

    public Value getV4() {
        return commas.get(1).getValue();
    }


    @Override
    public void renameUses(Value newValue, Value oldValue) {
        if (v.isIdent() && v.getIdent().equals(oldValue.getIdent())) {
            v = newValue;
        }
        if (commas.get(0).getValue().isIdent() && commas.get(0).getValue().getIdent().equals(oldValue.getIdent())) {
            commas.get(0).setValue(newValue);
        }
        if (hasFourth) {
            if (commas.get(1).getValue().isIdent() && commas.get(1).getValue().getIdent().equals(oldValue.getIdent())) {
                commas.get(1).setValue(newValue);
            }
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
        if (commas.get(0).getValue().isIdent()) {
            ans.add(commas.get(0).getValue().getIdent().toString());
        }
        if (hasFourth) {
            if (commas.get(1).getValue().isIdent()) {
                ans.add(commas.get(1).getValue().getIdent().toString());
            }
        }
        return ans;
    }

    @Override
    public String getDef() {
        //todo
        return null;
    }

    @Override
    public HashMap<String, Boolean> getUsesAndTypes() {
        HashMap<String, Boolean> ans = new HashMap<>();
        if (v.isIdent()) {
            ans.put(v.getIdent().toString(), false);
        }
        if (commas.get(0).getValue().isIdent()) {
            ans.put(commas.get(0).getValue().getIdent().toString(), false);
        }
        if (hasFourth) {
            if (commas.get(1).getValue().isIdent()) {
                ans.put(commas.get(1).getValue().getIdent().toString(), false);
            }
        }
        return ans;
    }

    @Override
    public HashMap<String, Boolean> getDefAndType() {
        return new HashMap<>();
    }

    @Override
    public ArrayList<String> getRoots() {
        //todo
        ArrayList<String> ans = new ArrayList<>();
        return ans;
    }

    @Override
    public boolean setAssignType() {
        return t1.isFloat();
    }
}
