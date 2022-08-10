package llvm.Instr;

import llvm.Type.Type;
import llvm.Value;

import java.util.ArrayList;

public class FCmpInst extends Instr {
    private String fpred;
    private Type t;
    private Value v1;
    private Value v2;

    public FCmpInst(String instrname, String ipred, Type t, Value v1, Value v2) {
        super(instrname);
        this.fpred = ipred;
        this.t = t;
        this.v1 = v1;
        this.v2 = v2;
    }

    @Override
    public String toString() {
        return "fcmp " + fpred + " " + t.toString() + " " + v1.toString() + ", " + v2.toString();
    }

    public String getFpred() {
        return fpred;
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

    // llvm 的ipred转为bne、beq等
    public String predToBr() {
        switch (fpred) {
            case "oeq":
                return "eq";
            case "one":
                return "ne";
            case "oge":
                return "ge";
            case "ogt":
                return "gt";
            case "ole":
                return "le";
            case "olt":
                return "lt";
            case "uge":
            case "ugt":
            case "ule":
            case "ult":
            default:
                return "qqqq";
            //todo
        }
    }

    // llvm 的ipred转为相反的ne、eq等
    public String predToOppoBr() {
        switch (fpred) {
            case "oeq":
                return "ne";
            case "one":
                return "eq";
            case "oge":
                return "lt";
            case "ogt":
                return "le";
            case "ole":
                return "gt";
            case "olt":
                return "ge";
            case "uge":
            case "ugt":
            case "ule":
            case "ult":
            default:
                return "qqqq";
            //todo
        }
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
        return null;
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
