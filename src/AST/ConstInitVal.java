package AST;

import java.util.ArrayList;

public class ConstInitVal extends Node {
    private ArrayList<Object> exps;

    public ConstInitVal(ArrayList<Object> exps) {
        this.exps = exps;
    }

    private int getN(ArrayList<Integer> dims, int n, int p) {
        if (p == 0) {
            return n + 1;
        }
        int max = 1;
        for (int i = n; i < dims.size(); i++) {
            max *= dims.get(i);
        }
        while (p % max != 0) {
            max /= dims.get(n);
            n++;
        }
        return n;
    }

    public ArrayList<Integer> getIntValues(ArrayList<Integer> dims, int n) {
        int count = 0;
        int max = 1;
        for (int i = n; i < dims.size(); i++) {
            max *= dims.get(i);
        }
        ArrayList<Integer> initValues = new ArrayList<>();
        for (Object exp: exps) {
            if (exp instanceof ConstInitVal) {
                ArrayList<Integer> temps = ((ConstInitVal) exp).getIntValues(dims, getN(dims, n, count));
                count += temps.size();
                initValues.addAll(temps);
            }
            else {
                initValues.add(((Exp) exp).getValue());
                count++;
            }
        }

        while (initValues.size() < max) {
            initValues.add(0);
        }

        return initValues;
    }

    public ArrayList<Float> getFloatValues(ArrayList<Integer> dims, int n) {
        int count = 0;
        int max = 1;
        for (int i = n; i < dims.size(); i++) {
            max *= dims.get(i);
        }
        ArrayList<Float> initValues = new ArrayList<>();
        for (Object exp: exps) {
            if (exp instanceof ConstInitVal) {
                ArrayList<Float> temps = ((ConstInitVal) exp).getFloatValues(dims, getN(dims, n, count));
                count += temps.size();
                initValues.addAll(temps);
            }
            else {
                initValues.add(((Exp) exp).getValueF());
                count++;
            }
        }

        while (initValues.size() < max) {
            initValues.add(Float.parseFloat("0"));
        }

        return initValues;
    }


    @Override
    public void addMidCode() {

    }
}
