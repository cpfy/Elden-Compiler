package AST;

import java.util.ArrayList;

public class InitVal extends Node {
    private ArrayList<Exp> exps;

    public InitVal(ArrayList<Exp> exps) {
        this.exps = exps;
    }

    public ArrayList<Integer> getIntValues() {
        ArrayList<Integer> initValues = new ArrayList<>();
        for (Exp exp: exps) {
            initValues.add(exp.getValue());
        }
        return initValues;
    }

    public ArrayList<Float> getFloatValues() {
        ArrayList<Float> initValues = new ArrayList<>();
        for (Exp exp: exps) {
            initValues.add(exp.getValueF());
        }
        return initValues;
    }

    public ArrayList<Exp> getExps() {
        return exps;
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
