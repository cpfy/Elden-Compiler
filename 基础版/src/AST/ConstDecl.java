package AST;

import java.util.ArrayList;

public class ConstDecl extends Decl {
    private String type;
    private ArrayList<ConstDef> constDefs = new ArrayList<>();

    public ConstDecl(String type, ArrayList<ConstDef> constDefs) {
        this.type = type;
        this.constDefs = constDefs;
    }

    @Override
    public void addMidCode() {
        for (ConstDef constDef: constDefs) {
            constDef.addMidCode();
        }
    }
}
