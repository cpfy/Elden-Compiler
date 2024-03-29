package AST;

import symbolTable.items.FunctionItem;

public class MainFuncDef extends Node {
    private StmtBlock block;

    public MainFuncDef(StmtBlock block) {
        this.block = block;
    }

    @Override
    public void addMidCode() {
//        midCodeList.addMidCodeItem(MidCodeType.FUNC, "int", null, "main");
        isMain = true;
        labels = 0;
        table.addFunc(new FunctionItem("main", "i32", null));
        table.newFunc();
        addCode("\n");
        addCode("define dso_local i32 @main() {\n" );
        addCode(newLable() + ":\n");
        block.addMidCode();
        addCode("ret " + "i32 " + " 0   ;不会被用到\n");
        addCode("}\n");
        
    }
}
