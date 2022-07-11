package AST;

import midCode.MidCodeType;
import symbolTable.items.IntegerItem;

import java.util.ArrayList;


public class ConstDef extends Def {
    private ID id;
    private ArrayList<Exp> dims;
    private ConstInitVal constInitVal;

    public ConstDef(ID id, ArrayList<Exp> dims, ConstInitVal constInitVal) {
        this.id = id;
        this.dims = dims;
        this.constInitVal = constInitVal;
    }



    @Override
    public void addMidCode() {
        tableInsert();
        if (dims.size() == 0) {
            midCodeList.addMidCodeItem(MidCodeType.CONST, String.valueOf(constInitVal.getInitValues().get(0)), null, id.getRawWord().getName());
        }
        else if (dims.size() == 1) {
            midCodeList.addMidCodeItem(MidCodeType.ARRAY, String.valueOf(dims.get(0).getValue()), null, id.getRawWord().getName());
            for (int i = 0; i < dims.get(0).getValue(); i++) {
                midCodeList.addMidCodeItem(MidCodeType.PUTARRAY, String.valueOf(table.getValue(id.getRawWord().getName(), i)), String.valueOf(i), id.getRawWord().getName());
            }
        }
        else if (dims.size() == 2) {
            midCodeList.addMidCodeItem(MidCodeType.ARRAY, String.valueOf(dims.get(0).getValue()),
                    String.valueOf(dims.get(1).getValue()), id.getRawWord().getName());
            int t = dims.get(1).getValue();
            for (int i = 0; i < dims.get(0).getValue(); i++) {
                for (int j = 0; j < dims.get(1).getValue(); j++) {
                    midCodeList.addMidCodeItem(MidCodeType.PUTARRAY, String.valueOf(table.getValue(id.getRawWord().getName(), i * t + j)), String.valueOf(i * t + j), id.getRawWord().getName());
                }
            }
        }
    }

    @Override
    void tableInsert() {
        ArrayList<Integer> dimsInt = new ArrayList<>();
        for (Exp exp: dims) {
            dimsInt.add(exp.getValue());
        }
        table.addInteger(new IntegerItem(id.getRawWord().getName(), true, dimsInt, constInitVal.getInitValues()));

    }
}
