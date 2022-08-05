package llvm.Instr;

import llvm.Type.Type;
import llvm.Value;

import java.util.ArrayList;

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
        return getInstrname() + " " + t.toString() + " " + v1.toString() + ", " + v2.toString();
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
        //todo 浮点数未处理
        if (v1.isIdent() || v2.isIdent()) {
            return null;
        }
        Value value = null;
        switch (op) {
            case "add":
                value = new Value(String.valueOf(v1.getVal() + v2.getVal()));
                break;
            case "fadd":
                value = null;
                break;
            case "sub":
                value = new Value(String.valueOf(v1.getVal() - v2.getVal()));
                break;
            case "fsub":
                value = null;
                break;
            case "mul":
                value = new Value(String.valueOf(v1.getVal() * v2.getVal()));
                break;
            case "fmul":
                value = null;
                break;
            case "sdiv":
                value = new Value(String.valueOf(v1.getVal() / v2.getVal()));
                break;
            case "fdiv":
                value = null;
                break;
            case "srem":
                value = new Value(String.valueOf(v1.getVal() % v2.getVal()));
                break;
            default:
                break;
        }
        return value;
    }
}
