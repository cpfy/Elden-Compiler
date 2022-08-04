package llvm.Instr;

import llvm.Ident;
import llvm.Type.Type;
import llvm.TypeValue;

import java.util.ArrayList;

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

    public Type getReturntype() {
        return returntype;
    }

    public String getFuncname() {
        return funcname;
    }

    public ArrayList<TypeValue> getArgs() {
        return args;
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
                name.equals("putfarray");
    }


}
