package AST;

import java.util.ArrayList;

public class ConstInitVal extends Node {
    private ArrayList<Exp> exps;

    public ConstInitVal(ArrayList<Exp> exps) {
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

    @Override
    public void addMidCode() {

    }
}
