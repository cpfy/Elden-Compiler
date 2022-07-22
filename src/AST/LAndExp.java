package AST;

import java.util.ArrayList;

public class LAndExp extends Node {

    private ArrayList<Exp> exps = new ArrayList<>();
    private ArrayList<String> temps = new ArrayList<>();
    private ArrayList<String> lables = new ArrayList<>();

    private String preCode = "";

    public LAndExp(ArrayList<Exp> exps) {
        this.exps = exps;
    }

    @Override
    public void addMidCode() {

    }

    public String getLastLable() {
        return lables.get(lables.size() - 1);
    }

    public void addMidCode(String jump1, String jump2, boolean isLast) {
        int i;
        String out = lables.get(lables.size() - 1);
        for (i = 0; i < exps.size(); i++) {
            if (i == exps.size() - 1) {
                if (isLast) {
                    exps.get(i).getCodes().add("br i1 " + temps.get(i) + ", "
                            + "label %" + jump1 + ", "
                            + "label %" + jump2 + "\n");
                }
                else {
                    exps.get(i).getCodes().add("br i1 " + temps.get(i) + ", "
                            + "label %" + jump1 + ", "
                            + "label %" + out + "\n");
                }
            }
            else {
                if (isLast) {
                    exps.get(i).getCodes().add("br i1 " + temps.get(i) + ", "
                            + "label %" + lables.get(i) + ", "
                            + "label %" + jump2 + "\n");
                }
                else {
                    exps.get(i).getCodes().add("br i1 " + temps.get(i) + ", "
                            + "label %" + lables.get(i) + ", "
                            + "label %" + out + "\n");
                }
            }
            exps.get(i).getCodes().add(lables.get(i) + ":\n");
            exps.get(i).generate();
        }
    }

    public void addMidCodePre() {
        for (Exp exp: exps) {
            exp.addCodePre();
            String t = newTemp();
            temps.add(t);
            exp.getCodes().add(t + " = icmp ne i32 " + exp.getTemp() + ", 0\n");
            lables.add(newLable());
//            exp.generate();
        }
    }
}
