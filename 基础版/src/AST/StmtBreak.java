package AST;

import midCode.MidCodeType;

public class StmtBreak extends Stmt {
    public StmtBreak() {
    }

    @Override
    public void addMidCode() {
        midCodeList.addMidCodeItem(MidCodeType.GOTO, null, null, jumps.get(1));
    }
}
