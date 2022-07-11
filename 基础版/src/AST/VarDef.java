package AST;

import midCode.MidCodeType;
import symbolTable.items.IntegerItem;

import java.util.ArrayList;

public class VarDef extends Def {
    private ID id;
    private ArrayList<Exp> dims;
    private InitVal initVal;

    public VarDef(ID id, ArrayList<Exp> dims, InitVal initVal) {
        this.id = id;
        this.dims = dims;
        this.initVal = initVal;
    }

    @Override
    public void addMidCode() {
        tableInsert();
        ArrayList<Exp> exps = null;
        if (dims.size() == 0) {
            if (initVal != null) {
                initVal.addMidCode();
                midCodeList.addMidCodeItem(MidCodeType.VAR, initVal.getInit().get(0).getTemp(), null, id.getRawWord().getName());
            }
            else {
                midCodeList.addMidCodeItem(MidCodeType.VAR, null, null, id.getRawWord().getName());
            }
        }
        else if (dims.size() == 1) {
            midCodeList.addMidCodeItem(MidCodeType.ARRAY, String.valueOf(dims.get(0).getValue()), null, id.getRawWord().getName());
            if (initVal != null) {
                initVal.addMidCode();
                exps = initVal.getInit();
                for (int i = 0; i < dims.get(0).getValue(); i++) {
                    midCodeList.addMidCodeItem(MidCodeType.PUTARRAY, exps.get(i).getTemp(), String.valueOf(i), id.getRawWord().getName());
                }
            }
        }
        else if (dims.size() == 2) {
            midCodeList.addMidCodeItem(MidCodeType.ARRAY, String.valueOf(dims.get(0).getValue()),
                    String.valueOf(dims.get(1).getValue()), id.getRawWord().getName());
            if (initVal != null) {
                initVal.addMidCode();
                exps = initVal.getInit();
                int t = dims.get(1).getValue();
                for (int i = 0; i < dims.get(0).getValue(); i++) {
                    for (int j = 0; j < dims.get(1).getValue(); j++) {
                        midCodeList.addMidCodeItem(MidCodeType.PUTARRAY, exps.get(i * t + j).getTemp(), String.valueOf(i * t + j), id.getRawWord().getName());
                    }
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
        table.addInteger(new IntegerItem(id.getRawWord().getName(), false, dimsInt, null));
    }
}
