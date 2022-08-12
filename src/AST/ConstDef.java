package AST;

import symbolTable.items.FloatItem;
import symbolTable.items.IntegerItem;

import java.util.ArrayList;


public class ConstDef extends Def {
    private ID id;
    private ArrayList<Exp> dims;
    private ConstInitVal constInitVal;
    private String tempName;

    public ConstDef(ID id, ArrayList<Exp> dims, ConstInitVal constInitVal) {
        this.id = id;
        this.dims = dims;
        this.constInitVal = constInitVal;
    }



    @Override
    public void addMidCode() {
        tableInsert();
        addElement();
    }

    private void addElement() {
        if (dims.size() == 0) { //非数组形式
        }
        else {  //数组形式
            if (isGlobal) {
                addCode(tempName + " = dso_local constant ");
                if (constInitVal != null) {
                    ArrayList<Integer> dimsInt = new ArrayList<>();
                    for (Exp exp: dims) {
                        dimsInt.add(exp.getValue());
                    }
                    ArrayList<String> values = new ArrayList<>();
                    if (getDeclType().equals("i32")) {
                        for (Integer integer: constInitVal.getIntValues(dimsInt, 0)) {
                            values.add(String.valueOf(integer));
                        }
                    }
                    else if (getDeclType().equals("float")) {
                        for (Float f: constInitVal.getFloatValues(dimsInt, 0)) {
                            values.add(String.valueOf(f));
                        }
                    }
                    ArrayList<Integer> p = new ArrayList<>();
                    p.add(0);
                    addCode(globalArrayInit(declType, dimsInt, values, 0, p) + "\n");
                } else {
                    addCode(getArrayType(dims, getDeclType()) + " zeroinitializer\n");
                }
            }
            else {
                addCode(tempName + " = alloca " + getArrayType(dims, getDeclType()) + "\n");
                ArrayList<Integer> dimsInt = new ArrayList<>();
                for (Exp exp : dims) {
                    dimsInt.add(exp.getValue());
                }
                ArrayList<String> values = new ArrayList<>();
                if (getDeclType().equals("i32")) {
                    for (Integer integer: constInitVal.getIntValues(dimsInt, 0)) {
                        values.add(String.valueOf(integer));
                    }
                }
                else if (getDeclType().equals("float")) {
                    for (Float f: constInitVal.getFloatValues(dimsInt, 0)) {
                        values.add(String.valueOf(f));
                    }
                }
                ArrayList<Integer> p = new ArrayList<>();
                p.add(0);
                localArrayInit(declType, dimsInt, values, 0, p, tempName);

            }
        }
    }

    void tableInsert() {
        if (isGlobal) {
            tempName = "@_" + newGlobalName(id.getRawWord().getName());
        }
        else if (dims.size() != 0) {
            tempName = newTemp();
        }
        ArrayList<Integer> dimsInt = new ArrayList<>();
        for (Exp exp: dims) {
            dimsInt.add(exp.getValue());
        }
        if (getDeclType().equals("i32")) {
            table.addInteger(new IntegerItem(id.getRawWord().getName(), true, dimsInt, constInitVal.getIntValues(dimsInt, 0), tempName));
        }
        else if (getDeclType().equals("float")) {
            table.addFloat(new FloatItem(id.getRawWord().getName(), true, dimsInt, constInitVal.getFloatValues(dimsInt, 0), tempName));
        }

    }
}
