package llvm.Instr;

import llvm.Ident;
import llvm.Type;
import llvm.TypeValue;
import llvm.Value;

import java.util.ArrayList;

public class CallInst extends Instr {
    private Type returntype;
    private String funcname;
    ArrayList<TypeValue> args;

    // "call"
    public CallInst(String instrname, Type t, Ident vi, ArrayList<TypeValue> args) {
        super(instrname);
        this.returntype = t;
        this.funcname = vi.getName();
        this.args = args;
    }


}
