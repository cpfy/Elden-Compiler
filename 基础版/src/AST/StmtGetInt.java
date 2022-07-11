package AST;

import midCode.MidCodeType;

public class StmtGetInt extends Stmt {
    private LVal lVal;

    public StmtGetInt(LVal lVal) {
        this.lVal = lVal;
    }

    @Override
    public void addMidCode() {
        lVal.setAssign(true);
        lVal.addMidCode();
        if (!lVal.isArray()) {
            midCodeList.addMidCodeItem(MidCodeType.SCAN, null, null, lVal.getTemp());
        }
        else {
            String temp1 = lVal.newTemp();
            midCodeList.addMidCodeItem(MidCodeType.SCAN, null, null, temp1);
            midCodeList.addMidCodeItem(MidCodeType.PUTARRAY, temp1, lVal.getTemp(), lVal.getId().getRawWord().getName());
        }
    }
}
