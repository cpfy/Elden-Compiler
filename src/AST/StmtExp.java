package AST;

public class StmtExp extends Stmt {
    private Exp exp;

    public StmtExp(Exp exp) {
        this.exp = exp;
    }

    @Override
    public void addMidCode() {
        if (exp != null) {
            exp.addMidCode();
        }
        
    }
}
