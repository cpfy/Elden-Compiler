package AST;

public class StmtBreak extends Stmt {
    public StmtBreak() {
    }

    @Override
    public void addMidCode() {
        addCode("br label %" + jumps.get(1) + "\t;break\n");
        addCode(newLable() + ":\n");
//        midCodeList.addMidCodeItem(MidCodeType.GOTO, null, null, jumps.get(1));
    }
}
