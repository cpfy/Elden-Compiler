package AST;

import midCode.MidCodeType;
import symbolTable.items.IntegerItem;

import java.util.ArrayList;

public class FuncFParam extends Def {
    String type;
    ID id;
    ArrayList<Exp> dims;

    public FuncFParam(String type, ID id, ArrayList<Exp> dims) {
        this.type = type;
        this.id = id;
        this.dims = dims;
    }

    @Override
    public void addMidCode() {
        //todo
        tableInsert();
        if (dims.size() == 0) {
            midCodeList.addMidCodeItem(MidCodeType.PARAM, "0", null, id.getRawWord().getName());
        }
        else if (dims.size() == 1) {
            midCodeList.addMidCodeItem(MidCodeType.PARAM, "1", null, id.getRawWord().getName());
        }
        else if (dims.size() == 2) {
            midCodeList.addMidCodeItem(MidCodeType.PARAM, "2", String.valueOf(table.getDim2(id.getRawWord().getName())), id.getRawWord().getName());
        }
    }

    @Override
    void tableInsert() {
        ArrayList<Integer> dimsInt = new ArrayList<>();
        for (Exp exp: dims) {
            dimsInt.add(exp.getValue());
        }
        table.addInteger(new IntegerItem(id.getRawWord().getName(), false, dimsInt, null));
    }
}
