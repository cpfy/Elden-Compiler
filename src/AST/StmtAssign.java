package AST;

import midCode.MidCodeType;

public class StmtAssign extends Stmt {
    private LVal lVal;
    private Exp exp;

    public StmtAssign(LVal lVal, Exp exp) {
        this.lVal = lVal;
        this.exp = exp;
    }

    @Override
    public void addMidCode() {
        lVal.setAssign(true);
        lVal.addMidCode();
        exp.addMidCode();
        if (!lVal.isArray()) {
            midCodeList.addMidCodeItem(MidCodeType.ASSIGNOP, exp.getTemp(), null, lVal.getTemp());
        }
        else {
            midCodeList.addMidCodeItem(MidCodeType.PUTARRAY, exp.getTemp(), lVal.getTemp(), lVal.getId().getRawWord().getName());
        }
    }
}
