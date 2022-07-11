package AST;

import midCode.MidCodeType;

public class StmtReturn extends Stmt {
    private Exp exp;

    public StmtReturn(Exp exp) {
        this.exp = exp;
    }

    @Override
    public void addMidCode() {
        if (exp != null) {
            exp.addMidCode();
            midCodeList.addMidCodeItem(MidCodeType.RET, null, null, exp.getTemp());
        }
        else {
            midCodeList.addMidCodeItem(MidCodeType.RET, null, null, null);
        }
    }
}
