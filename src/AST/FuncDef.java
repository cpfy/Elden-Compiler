package AST;

import symbolTable.items.FunctionItem;

import java.util.ArrayList;

public class FuncDef extends Def {
    private String funcType;
    private ID id;
    private ArrayList<FuncFParam> funcFParams;
    private StmtBlock block;

    public FuncDef(String funcType, ID id, ArrayList<FuncFParam> funcFParams, StmtBlock block) {
        this.funcType = funcType;
        this.id = id;
        this.funcFParams = funcFParams;
        this.block = block;
    }

    @Override
    public void addMidCode() {
        addCode("\n");
        table.addFunc(new FunctionItem(id.getRawWord().getName(), funcType, null));
        table.newFunc();
        labels = 0;
        addCode("define dso_local " + funcType + " @" + id.getRawWord().getName() + "(");
        for (int i = 0; i < funcFParams.size(); i++) {
            if (i > 0) {
                addCode(", ");
            }
            funcFParams.get(i).addMidCode();
        }
        addCode(") {\n");
        newLable();
        for (FuncFParam funcFParam: funcFParams) {
            funcFParam.copyValue();
        }
        block.addMidCode();
        if (funcType.equals("void")) {
            addCode("ret void\n");
        }
        else {
            addCode("ret " + funcType + " 0\n");
        }
        addCode("}\n");
    }

    void tableInsert(String tempName) {

    }
}
