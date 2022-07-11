package AST;

import midCode.MidCodeType;

public class StmtIf extends Stmt {
    private Cond cond;
    private Stmt stmt1;
    private Stmt stmt2;

    public StmtIf(Cond cond, Stmt stmt1, Stmt stmt2) {
        this.cond = cond;
        this.stmt1 = stmt1;
        this.stmt2 = stmt2;
    }

    @Override
    public void addMidCode() {
        if (stmt2 == null) {
            String jump1 = "jump" + newJumpDst();
            String jump2 = "jump" + newJumpDst();
            cond.addMidCode(jump2);
            midCodeList.addMidCodeItem(MidCodeType.GOTO, null, null, jump1);
            midCodeList.addMidCodeItem(MidCodeType.JUMP, null, null, jump2);
            stmt1.addMidCode();
            midCodeList.addMidCodeItem(MidCodeType.JUMP, null, null, jump1);
        }
        else {
            String jump1 = "jump" + newJumpDst();
            String jump2 = "jump" + newJumpDst();
            String jump3 = "jump" + newJumpDst();
            cond.addMidCode(jump2);
            midCodeList.addMidCodeItem(MidCodeType.GOTO, null, null, jump1);
            midCodeList.addMidCodeItem(MidCodeType.JUMP, null, null, jump2);
            stmt1.addMidCode();
            midCodeList.addMidCodeItem(MidCodeType.GOTO, null, null, jump3);
            midCodeList.addMidCodeItem(MidCodeType.JUMP, null, null, jump1);
            stmt2.addMidCode();
            midCodeList.addMidCodeItem(MidCodeType.JUMP, null, null, jump3);
        }

    }
}
