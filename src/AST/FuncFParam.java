package AST;

import symbolTable.items.FloatItem;
import symbolTable.items.IntegerItem;

import java.util.ArrayList;

public class FuncFParam extends Def {
    String type;
    ID id;
    ArrayList<Exp> dims;
    String paramTemp;
    String detailType;

    public FuncFParam(String type, ID id, ArrayList<Exp> dims) {
        this.type = type;
        this.id = id;
        this.dims = dims;
        this.detailType = type;
    }

    private void setType() {
        if (dims.size() > 0) {
            ArrayList<Integer> dimsInt = new ArrayList<>();
            for (Exp exp: dims) {
                dimsInt.add(exp.getValue());
            }
            detailType = getArrayType(dimsInt, type, 1) + "*";
        }
    }

    @Override
    public void addMidCode() {
        //todo
        setType();
        paramTemp = newTemp();
        addCode(detailType + " " + paramTemp);
        ArrayList<Integer> dimsInt = new ArrayList<>();
        for (Exp exp: dims) {
            dimsInt.add(exp.getValue());
        }
        if (type.equals("i32")) {
            table.addParam(new IntegerItem(id.getRawWord().getName(), false, dimsInt, null, null, detailType));
        }
        else if (type.equals("float")) {
            table.addParam(new FloatItem(id.getRawWord().getName(), false, dimsInt, null, null, detailType));
        }
        else {
            System.out.println("\nERROR in FuncF!!!\n");
        }
    }

    public void copyValue() {
        //todo
        String temp = newTemp();
        tableInsert(temp);
        addCode(temp + " = alloca " + detailType + "\n");
        addCode("store " + detailType + " " + paramTemp + ", "
                + detailType + "* " + temp + "\n");
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
