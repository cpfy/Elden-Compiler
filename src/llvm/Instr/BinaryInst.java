package llvm.Instr;

import llvm.Block;
import llvm.Type.Type;
import llvm.TypeValue;
import llvm.Value;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class BinaryInst extends Instr {
    private String op;      //运算符
    private Type t;
    private Value v1;
    private Value v2;

    public BinaryInst(String instrname, String op, Type t, Value v1, Value v2) {
        super(instrname);
        this.op = op;
        this.t = t;
        this.v1 = v1;
        this.v2 = v2;
    }

    @Override
    public String toString() {
        return op + " " + t.toString() + " " + v1.toString() + ", " + v2.toString();
    }

    public String getOp() {
        return op;
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

    private void ValueInstruction() {
        String sym = "";
        switch (sym) {
            case "add":
                AddInst();
                break;
            case "fadd":
                FAddInst();
                break;
            case "sub":
                SubInst();
                break;
            case "fsub":
                FSubInst();
                break;
            case "mul":
                MulInst();
                break;
            case "fmul":
                FMulInst();
                break;
            case "sdiv":
                SDivInst();
                break;
            case "fdiv":
                FDivInst();
                break;
            default:
                break;
        }
    }

    private void FAddInst() {
    }

    private void SubInst() {
    }

    private void FSubInst() {
    }

    private void MulInst() {
    }

    private void FMulInst() {
    }

    private void SDivInst() {
    }

    private void FDivInst() {
    }

    private void AddInst() {
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

    private float hex2Float(String hex) {
        Long i = Long.parseLong(hex.substring(2), 16);
        Float f = Float.intBitsToFloat(i.intValue());
        return f;
    }

    private String float2Hex(float f) {
        return "0x" + Integer.toHexString(Float.floatToIntBits(f));
    }

    @Override
    public Value mergeConst() {
        Value value = null;
        if (v1.isIdent() && v2.isIdent()) {
            return null;
        }
        else if (v1.isIdent() && !v2.isIdent()) {   //v2是常数
            switch (op) {
                case "add":
                    if (v2.getVal() == 0) {
                        value = v1;
                    }
                    break;
                case "fadd":
                    break;
                case "sub":
                    if (v2.getVal() == 0) {
                        value = v1;
                    }
                    break;
                case "fsub":
                    break;
                case "mul":
                    if (v2.getVal() == 1) {
                        value = v1;
                    }
                    else if (v2.getVal() == 0) {
                        value = new Value(0);
                    }
                    break;
                case "fmul":
                    break;
                case "sdiv":
                    if (v2.getVal() == 1) {
                        value = v1;
                    }
                    break;
                case "fdiv":
                    break;
                case "srem":
                    if (v2.getVal() == 1) {
                        value = new Value(0);
                    }
                    break;
                default:
                    break;
            }
        }
        else if (!v1.isIdent() && v2.isIdent()) {   //v1是常数
            switch (op) {
                case "add":
                    if (v1.getVal() == 0) {
                        value = v2;
                    }
                    break;
                case "fadd":
                    break;
                case "sub":
                    break;
                case "fsub":
                    break;
                case "mul":
                    if (v1.getVal() == 1) {
                        value = v2;
                    } else if (v1.getVal() == 0) {
                        value = new Value(0);
                    }
                    break;
                case "fmul":
                    break;
                case "sdiv":
                    if (v1.getVal() == 0) {
                        value = new Value(0);
                    }
                    break;
                case "fdiv":
                    break;
                case "srem":
                    if (v1.getVal() == 0) {
                        value = new Value(0);
                    }
                    break;
                default:
                    break;
            }
        }
        else {
            switch (op) {
                case "add":
                    value = new Value(String.valueOf(v1.getVal() + v2.getVal()));
                    break;
                case "fadd":
                    value = new Value(float2Hex(hex2Float(v1.getHexVal()) + hex2Float(v2.getHexVal())));
                    break;
                case "sub":
                    value = new Value(String.valueOf(v1.getVal() - v2.getVal()));
                    break;
                case "fsub":
                    value = new Value(float2Hex(hex2Float(v1.getHexVal()) - hex2Float(v2.getHexVal())));
                    break;
                case "mul":
                    value = new Value(String.valueOf(v1.getVal() * v2.getVal()));
                    break;
                case "fmul":
                    value = new Value(float2Hex(hex2Float(v1.getHexVal()) * hex2Float(v2.getHexVal())));
                    break;
                case "sdiv":
                    value = new Value(String.valueOf(v1.getVal() / v2.getVal()));
                    break;
                case "fdiv":
                    value = new Value(float2Hex(hex2Float(v1.getHexVal()) / hex2Float(v2.getHexVal())));
                    break;
                case "srem":
                    value = new Value(String.valueOf(v1.getVal() % v2.getVal()));
                    break;
                default:
                    break;
            }
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
    public HashMap<String, Boolean> getUsesAndTypes() {
        HashMap<String, Boolean> ans = new HashMap<>();
        if (v1.isIdent()) {
            ans.put(v1.getIdent().toString(), t.isFloat());
        }
        if (v2.isIdent()) {
            ans.put(v2.getIdent().toString(), t.isFloat());
        }
        return ans;
    }

    @Override
    public HashMap<String, Boolean> getDefAndType() {
        return new HashMap<>();
    }

    @Override
    public ArrayList<String> getRoots() {
        ArrayList<String> ans = new ArrayList<>();
        return ans;
    }

    @Override
    public boolean setAssignType() {
        return t.isFloat();
    }
}
