package AST;

import java.util.ArrayList;

public class Cond extends Node {
    private LOrExp lOrExps;

    public Cond(LOrExp lOrExps) {
        this.lOrExps = lOrExps;
    }

    public void addMidCode(String jump) {
        lOrExps.addMidCode(jump);
    }

    @Override
    public void addMidCode() {

    }
}
