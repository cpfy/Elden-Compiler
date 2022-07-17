package AST;

public class StmtContinue extends Stmt {
    public StmtContinue() {
    }

    @Override
    public void addMidCode() {
        addCode("br label %" + jumps.get(0) + "\t;continue\n");
        addCode(newLable() + ":\n");
//        midCodeList.addMidCodeItem(MidCodeType.GOTO, null, null, jumps.get(0));
    }
}
