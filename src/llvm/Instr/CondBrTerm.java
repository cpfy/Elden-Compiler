package llvm.Instr;

import llvm.Ident;
import llvm.Instr.Instr;
import llvm.Value;

public class CondBrTerm extends Instr {
    private Value v;
    private Ident l1;
    private Ident l2;

    // "condbr"
    public CondBrTerm(String instrname, Value v, Ident l1, Ident l2) {
        super(instrname);
        this.v = v;
        this.l1 = l1;
        this.l2 = l2;
    }

    public Value getV() {
        return v;
    }

    public void setV(Value v){this.v = v;}

    public Ident getL1() {
        return l1;
    }

    public Ident getL2() {
        return l2;
    }

    // 跳转name
    public String getLabel1Name(){
        return String.valueOf(l1.getId());
    }

    public String getLabel2Name(){
        return String.valueOf(l2.getId());
    }

    public void setL1(Ident L1){this.l1 = L1;}
    public void setL2(Ident L2){this.l2 = L2;}
}
