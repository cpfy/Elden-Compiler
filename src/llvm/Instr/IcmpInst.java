package llvm.Instr;

import llvm.Type.Type;
import llvm.Value;

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

}
