package AST;

public class StmtAssign extends Stmt {
    private LVal lVal;
    private Exp exp;

    public StmtAssign(LVal lVal, Exp exp) {
        this.lVal = lVal;
        this.exp = exp;
    }

    @Override
    public void addMidCode() {
        lVal.setAssign(true);
        exp.addMidCode();
        lVal.addMidCode();
        String tempExp = exp.getTemp();
        String tempLVal = lVal.getTemp();
        if (exp.getType().equals("i32") && lVal.getType().equals("float")) {
            String newTemp = newTemp();
            addCode(newTemp + " = sitofp i32 " + tempExp + " to float\n");
            tempExp = newTemp;
        }
        else if (exp.getType().equals("float") && lVal.getType().equals("i32")) {
            String newTemp = newTemp();
            addCode(newTemp + " = fptosi float " + tempExp + " to i32\n");
            tempExp = newTemp;
        }
        addCode("store " + lVal.getType() + " " + tempExp + ", "
                + lVal.getType() + "* " + tempLVal + "\n");
    }
}
