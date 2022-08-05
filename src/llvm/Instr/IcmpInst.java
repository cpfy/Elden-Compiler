package llvm.Instr;

import llvm.Type.Type;
import llvm.Value;

import java.util.ArrayList;

public class IcmpInst extends Instr {
    private String ipred;
    private Type t;
    private Value v1;
    private Value v2;

    public IcmpInst(String instrname, String ipred, Type t, Value v1, Value v2) {
        super(instrname);
        this.ipred = ipred;
        this.t = t;
        this.v1 = v1;
        this.v2 = v2;
    }

    @Override
    public String toString() {
        return "icmp " + ipred + " " + t.toString() + " " + v1.toString() + ", " + v2.toString();
    }

    public String getIpred() {
        return ipred;
    }

    public Type getT() {
        return t;
    }

    public Value getV1() {
        return v1;
    }

    public Value getV2() {
        return v2;
    }

    public void setV1(Value v1) {
        this.v1 = v1;
    }

    public void setV2(Value v2) {
        this.v2 = v2;
    }

    @Override
    public void renameUses(Value newValue, Value oldValue) {
        if (v1.isIdent() && v1.getIdent().equals(oldValue.getIdent())) {
            v1 = newValue;
        }
        if (v2.isIdent() && v2.getIdent().equals(oldValue.getIdent())) {
            v2 = newValue;
        }
    }

    @Override
    public Value mergeConst() {
        if (v1.isIdent() || v2.isIdent()) {
            return null;
        }
        Value value = null;
        if (ipred.equals("ne")) {
            String s = "0";
            if (v1.getVal() != v2.getVal()) {
                s = "1";
            }
            value = new Value(s);
        }
        else if (ipred.equals("eq")) {
            String s = "0";
            if (v1.getVal() == v2.getVal()) {
                s = "1";
            }
            value = new Value(s);

        }
        else if (ipred.equals("sle")) {
            String s = "0";
            if (v1.getVal() <= v2.getVal()) {
                s = "1";
            }
            value = new Value(s);
        }
        else if (ipred.equals("slt")) {
            String s = "0";
            if (v1.getVal() < v2.getVal()) {
                s = "1";
            }
            value = new Value(s);
        }
        else if (ipred.equals("sge")) {
            String s = "0";
            if (v1.getVal() >= v2.getVal()) {
                s = "1";
            }
            value = new Value(s);
        }
        else if (ipred.equals("sgt")) {
            String s = "0";
            if (v1.getVal() > v2.getVal()) {
                s = "1";
            }
            value = new Value(s);
        }
        return value;
    }

    @Override
    public ArrayList<String> getUses() {
        ArrayList<String> ans = new ArrayList<>();
        if (v1.isIdent()) {
            ans.add(v1.getIdent().toString());
        }
        if (v2.isIdent()) {
            ans.add(v2.getIdent().toString());
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
        return ans;
    }
}
