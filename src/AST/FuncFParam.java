package AST;

import symbolTable.items.FloatItem;
import symbolTable.items.IntegerItem;

import java.util.ArrayList;

public class FuncFParam extends Def {
    String type;
    ID id;
    ArrayList<Exp> dims;
    String paramTemp = null;

    public FuncFParam(String type, ID id, ArrayList<Exp> dims) {
        this.type = type;
        this.id = id;
        this.dims = dims;
    }

    @Override
    public void addMidCode() {
        //todo
        paramTemp = newTemp();
        addCode(type + " " + paramTemp);
        ArrayList<Integer> dimsInt = new ArrayList<>();
        for (Exp exp: dims) {
            dimsInt.add(exp.getValue());
        }
        if (type.equals("i32")) {
            table.addParam(new IntegerItem(id.getRawWord().getName(), false, dimsInt, null, null));
        }
        else if (type.equals("float")) {
            table.addParam(new FloatItem(id.getRawWord().getName(), false, dimsInt, null, null));
        }
        else {
            System.out.println("\nERROR in FuncF!!!\n");
        }
//        tableInsert();
//        if (dims.size() == 0) {
//            midCodeList.addMidCodeItem(MidCodeType.PARAM, "0", null, id.getRawWord().getName());
//        }
//        else if (dims.size() == 1) {
//            midCodeList.addMidCodeItem(MidCodeType.PARAM, "1", null, id.getRawWord().getName());
//        }
//        else if (dims.size() == 2) {
//            midCodeList.addMidCodeItem(MidCodeType.PARAM, "2", String.valueOf(table.getDim2(id.getRawWord().getName())), id.getRawWord().getName());
//        }
    }

    public void copyValue() {
        //todo
        String temp = newTemp();
        tableInsert(temp);
        addCode(temp + " = alloca " + type + "\n");
        //todo 类型转换
        addCode("store " + type + " " + paramTemp + ", "
                + type + "* " + temp + "\n");
    }

    void tableInsert(String tempName) {
        ArrayList<Integer> dimsInt = new ArrayList<>();
        for (Exp exp: dims) {
            dimsInt.add(exp.getValue());
        }
        if (type.equals("i32")) {
            table.addInteger(new IntegerItem(id.getRawWord().getName(), false, dimsInt, null, tempName));
        }
        else if (type.equals("float")) {
            table.addFloat(new FloatItem(id.getRawWord().getName(), false, dimsInt, null, tempName));
        }
        else {
            System.out.println("\nERROR in FuncF!!!\n");
        }
    }
}
