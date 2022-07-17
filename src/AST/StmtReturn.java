package AST;

public class StmtReturn extends Stmt {
    private Exp exp;

    public StmtReturn(Exp exp) {
        this.exp = exp;
    }

    @Override
    public void addMidCode() {
        if (exp != null) {
            String funcType = table.getFunctionNow().getRetType();
            exp.addMidCode();
            String temp = exp.getTemp();
            if (funcType.equals("i32") && exp.getType().equals("float")) {
                String nt = newTemp();
                addCode(nt + " = fptosi float " + temp + " to i32\n");
                temp = nt;
            }
            else if (funcType.equals("float") && exp.getType().equals("i32")) {
                String nt = newTemp();
                addCode(nt + " = sitofp i32 " + temp + " to float\n");
                temp = nt;
            }
            addCode("ret " + funcType + " " + temp + "\n");
        }
        else {
            addCode("ret void");
        }
        
    }
}
