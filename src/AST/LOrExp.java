package AST;

import java.util.ArrayList;

public class LOrExp extends Node {
    private ArrayList<LAndExp> lAndExps = new ArrayList<>();

    public LOrExp(ArrayList<LAndExp> lAndExps) {
        this.lAndExps = lAndExps;
    }

    @Override
    public void addMidCode() {

    }

    public void addMidCode(String jump) {
        for (int i = 0; i < lAndExps.size(); i++) {
            lAndExps.get(i).addMidCodePre();
        }

        String jump2Cond1 = lAndExps.get(lAndExps.size() - 1).getLastLable();

        for (int i = 0; i < lAndExps.size(); i++) {
            if (i == lAndExps.size() - 1) {
                lAndExps.get(i).addMidCode(jump2Cond1, jump, true);
            }
            else {
                lAndExps.get(i).addMidCode(jump2Cond1,null, false);
            }
        }
        
    }

    public void addMidCodePre() {
        for (LAndExp lAndExp: lAndExps) {

//            exp.generate();
        }
    }
}
