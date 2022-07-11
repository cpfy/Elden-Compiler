package AST;

import midCode.MidCodeType;

import java.util.ArrayList;

public class LAndExp extends Node {

    private ArrayList<Exp> exps = new ArrayList<>();

    public LAndExp(ArrayList<Exp> exps) {
        this.exps = exps;
    }

    @Override
    public void addMidCode() {

    }

    public void addMidCode(String jump) {
        for (Exp exp: exps) {
            exp.addMidCode();
            midCodeList.addMidCodeItem(MidCodeType.BZ, exp.getTemp(), null, jump);
        }
    }
}
