package AST;

import midCode.MidCodeType;

public class StmtWhile extends Stmt {

    private Cond cond;
    private Stmt stmt;

    public StmtWhile(Cond cond, Stmt stmt) {
        this.cond = cond;
        this.stmt = stmt;
    }

    @Override
    public void addMidCode() {
        String jump1 = "jump" + newJumpDst();
        String jump2 = "jump" + newJumpDst();
        String jump3 = "jump" + newJumpDst();
        jumps.add(0, jump1);
        jumps.add(0, jump3);
        midCodeList.addMidCodeItem(MidCodeType.JUMP, null, null, jump3);
        cond.addMidCode(jump2);
        midCodeList.addMidCodeItem(MidCodeType.GOTO, null, null, jump1);
        midCodeList.addMidCodeItem(MidCodeType.JUMP, null, null, jump2);
        stmt.addMidCode();
        midCodeList.addMidCodeItem(MidCodeType.GOTO, null, null, jump3);
        midCodeList.addMidCodeItem(MidCodeType.JUMP, null, null, jump1);
        jumps.remove(0);
        jumps.remove(0);
    }
}
