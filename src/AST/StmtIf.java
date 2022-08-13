package AST;

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
        addCode("\n");
        String jump2cond2 = newLable();
        String end = newLable();
        cond.addMidCode(jump2cond2);
        stmt1.addMidCode();
        addCode("br label %" + jump2cond2 + "\n");

        if (stmt2 != null) {
            addCode(jump2cond2 + ":\n");
            stmt2.addMidCode();
            addCode("br label %" + end + "\n");
            addCode(end + ":\n");
        }
        else {
            addCode(jump2cond2 + ":\n");
        }

    }
}
