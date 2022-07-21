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
                addCode(tempName + " = dso_local global ");
                addCode(getArrayType(dims, getDeclType()) + " ");
                if (constInitVal != null) {
                    constInitVal.addMidCode();
                } else {
                    addCode("zeroinitializer\n");
                }
            }
            else {
                addCode(tempName + " = alloca " + getArrayType(dims, getDeclType()) + "\n");
                //todo 数组初始化！！！！
                constInitVal.addMidCode();
            }
        }
    }

    void tableInsert() {
        if (isGlobal) {
            tempName = "@" + id.getRawWord().getName();
        }
        else {
            tempName = newTemp();
        }
        ArrayList<Integer> dimsInt = new ArrayList<>();
        for (Exp exp: dims) {
            dimsInt.add(exp.getValue());
        }
        if (getDeclType().equals("i32")) {
            table.addInteger(new IntegerItem(id.getRawWord().getName(), true, dimsInt, constInitVal.getIntValues(), tempName));
        }
        else if (getDeclType().equals("float")) {
            table.addFloat(new FloatItem(id.getRawWord().getName(), true, dimsInt, constInitVal.getFloatValues(), tempName));
        }

    }
}
