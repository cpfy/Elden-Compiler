package AST;

import midCode.MidCodeType;

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
        for (LAndExp lAndExp: lAndExps) {
            String jump1 = "jump" + newJumpDst();
            lAndExp.addMidCode(jump1);
            midCodeList.addMidCodeItem(MidCodeType.GOTO, null, null, jump);
            midCodeList.addMidCodeItem(MidCodeType.JUMP, null, null, jump1);
        }
    }
}
