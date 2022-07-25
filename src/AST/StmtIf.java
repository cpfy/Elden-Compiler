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

        String jump2cond2 = newReload();
        String end = newReload();
        cond.addMidCode(jump2cond2);
        stmt1.addMidCode();
        addCode("br label %" + end + "\n");


        if (stmt2 != null) {
            String label = newLable();
            addReload(jump2cond2, label);
            addCode(label + ":\n");
            stmt2.addMidCode();
            addCode("br label %" + end + "\n");
        }

        String label2 = newLable();
        if (stmt2 == null) {
            addReload(jump2cond2, label2);
        }
        addReload(end, label2);
        addCode(label2 + ":\n");
    }
}
