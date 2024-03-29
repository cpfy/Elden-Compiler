package llvm.Instr;

import llvm.Ident;
import llvm.Type.Type;
import llvm.TypeValue;
import llvm.Value;

import java.util.ArrayList;
import java.util.HashMap;

public class CallInst extends Instr {
    private Type returntype;
    private String funcname;
    ArrayList<TypeValue> args;

    private boolean standardCall;

    // "call"
    public CallInst(String instrname, Type t, Ident vi, ArrayList<TypeValue> args) {
        super(instrname);
        this.returntype = t;
        this.funcname = vi.getName();
        this.args = args;

        // 检查是否标准函数调用
        this.standardCall = checkStandardCall(this.funcname);
    }

    @Override
    public String toString() {
        String argstr = "";
        for (TypeValue tv : args) {
            argstr += tv.toString();
            argstr += ", ";
        }
        if (!argstr.isEmpty()) {
            argstr = argstr.substring(0, argstr.length() - 2);
        }
        return "call " + returntype.toString() + " @" + funcname + "(" + argstr + ")";
    }

    public Type getReturntype() {
        return returntype;
    }

    public String getFuncname() {
        return funcname;
    }

    public ArrayList<TypeValue> getArgs() {
        return args;
    }

    public int getArgsNum() {
        return args.size();
    }

    public boolean isStandardCall() {
        return standardCall;
    }

    // isStandard()判断可放在这里
    private boolean checkStandardCall(String name) {
        return name.equals("getint") || name.equals("getch") ||
                name.equals("getfloat") || name.equals("getarray") ||
                name.equals("getfarray") ||
                name.equals("putint") || name.equals("putch") ||
                name.equals("putfloat") || name.equals("putarray") ||
                name.equals("putfarray") ||
                name.equals("_sysy_starttime") || name.equals("_sysy_stoptime");
    }


    @Override
    public void renameUses(Value newValue, Value oldValue) {
        for (TypeValue typeValue : args) {
            Value v = typeValue.getValue();
            if (v.isIdent() && v.getIdent().equals(oldValue.getIdent())) {
                typeValue.setValue(newValue);
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
        for (TypeValue typeValue : args) {
            if (typeValue.getValue().isIdent()) {
                ans.add(typeValue.getValue().getIdent().toString());
            }
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
        for (TypeValue typeValue : args) {
            if (typeValue.getValue().isIdent()) {
                ans.put(typeValue.getValue().getIdent().toString(), typeValue.getType().isFloat());
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
        ArrayList<String> ans = new ArrayList<>();
        for (TypeValue typeValue : args) {
            if (typeValue.getValue().isIdent()) {
                ans.add(typeValue.getValue().getIdent().toString());
            }
        }
        return ans;
    }

    @Override
    public boolean setAssignType() {
        return returntype.isFloat();
    }
}
