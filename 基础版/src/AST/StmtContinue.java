package AST;

import midCode.MidCodeType;

public class StmtContinue extends Stmt {
    public StmtContinue() {
    }

    @Override
    public void addMidCode() {
        midCodeList.addMidCodeItem(MidCodeType.GOTO, null, null, jumps.get(0));
    }
}
