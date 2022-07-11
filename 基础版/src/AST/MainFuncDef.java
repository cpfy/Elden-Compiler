package AST;

import midCode.MidCodeType;

public class MainFuncDef extends Node {
    private StmtBlock block;

    public MainFuncDef(StmtBlock block) {
        this.block = block;
    }

    @Override
    public void addMidCode() {
        midCodeList.addMidCodeItem(MidCodeType.FUNC, "int", null, "main");
        block.addMidCode();
    }
}
