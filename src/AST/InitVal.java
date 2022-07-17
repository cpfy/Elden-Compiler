package AST;

import java.util.ArrayList;

public class InitVal extends Node {
    private ArrayList<Exp> exps;

    public InitVal(ArrayList<Exp> exps) {
        this.exps = exps;
    }

    @Override
    public void addMidCode() {
        //todo
        for (Exp exp: exps) {
            exp.addMidCode();
        }
        
    }

    public ArrayList<Exp> getInit() {
        return exps;
    }
}
