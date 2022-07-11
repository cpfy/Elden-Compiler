package AST;

import java.util.ArrayList;

public class CompUnit extends Node {
    private ArrayList<Decl> decls = new ArrayList<>();
    private ArrayList<FuncDef> funcDefs = new ArrayList<>();
    private MainFuncDef mainFuncDef = null;

    public CompUnit(ArrayList<Decl> decls, ArrayList<FuncDef> funcDefs, MainFuncDef mainFuncDef) {
        this.decls = decls;
        this.funcDefs = funcDefs;
        this.mainFuncDef = mainFuncDef;
    }

    @Override
    public void addMidCode() {
        for (Decl decl: decls) {
            decl.addMidCode();
        }
        for (FuncDef funcDef: funcDefs) {
            funcDef.addMidCode();
        }
        mainFuncDef.addMidCode();
    }
}
