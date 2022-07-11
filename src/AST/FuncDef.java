package AST;

import midCode.MidCodeType;
import symbolTable.items.FunctionItem;
import word.RawWord;

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
        table.addFunc(new FunctionItem(id.getRawWord().getName(), funcType, null));
        table.newFunc();
        midCodeList.addMidCodeItem(MidCodeType.FUNC, funcType, null, id.getRawWord().getName());
        for (FuncFParam funcFParam: funcFParams) {
            funcFParam.addMidCode();
        }
        block.addMidCode();
    }

    @Override
    void tableInsert() {

    }
}
