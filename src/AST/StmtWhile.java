package AST;

public class StmtWhile extends Stmt {

    private Cond cond;
    private Stmt stmt;

    public StmtWhile(Cond cond, Stmt stmt) {
        this.cond = cond;
        this.stmt = stmt;
    }

    @Override
    public void addMidCode() {

        String start = newLable();
        String end = newLable();
        addCode("br label %" + start + "\n");
        jumps.add(0,end);
        jumps.add(0,start);
        addCode(start + ":\n");
        cond.addMidCode(end);
        stmt.addMidCode();
        addCode("br label %" + start + "\n");

        jumps.remove(0);
        jumps.remove(0);


        addCode(end + ":\n");


//        String jump1 = "jump" + newJumpDst();
//        String jump2 = "jump" + newJumpDst();
//        String jump3 = "jump" + newJumpDst();
//        jumps.add(0, jump1);
//        jumps.add(0, jump3);
//        midCodeList.addMidCodeItem(MidCodeType.JUMP, null, null, jump3);
//        cond.addMidCode(jump2);
//        midCodeList.addMidCodeItem(MidCodeType.GOTO, null, null, jump1);
//        midCodeList.addMidCodeItem(MidCodeType.JUMP, null, null, jump2);
//        stmt.addMidCode();
//        midCodeList.addMidCodeItem(MidCodeType.GOTO, null, null, jump3);
//        midCodeList.addMidCodeItem(MidCodeType.JUMP, null, null, jump1);
//        jumps.remove(0);
//        jumps.remove(0);
    }
}
