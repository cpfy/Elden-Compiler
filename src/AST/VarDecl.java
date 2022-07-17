package AST;

import java.util.ArrayList;

public class VarDecl extends Decl {
    private String type;
    private ArrayList<VarDef> varDefs;

    public VarDecl(String type, ArrayList<VarDef> varDefs) {
        this.type = type;
        this.varDefs = varDefs;
    }

    @Override
    public void addMidCode() {
        setDeclType(type);
        for (VarDef varDef: varDefs) {
            varDef.addMidCode();
        }
        
    }
}
