package AST;

import symbolTable.items.FloatItem;
import symbolTable.items.IntegerItem;
import tool.OutputControl;

import java.util.ArrayList;

public class VarDef extends Def {
    private ID id;
    private ArrayList<Exp> dims;
    private InitVal initVal;
    private String tempName;

    public VarDef(ID id, ArrayList<Exp> dims, InitVal initVal) {
        this.id = id;
        this.dims = dims;
        this.initVal = initVal;
    }

    private void addElement() {
        if (dims.size() == 0) { //非数组形式
            if (isGlobal) {
                if (initVal != null) {
                    if (getDeclType().equals("i32")) {
                        addCode(tempName + " = dso_local global i32 " + initVal.getInit(null, 0).get(0).getValue() + "\n");
                    } else {
                        addCode(tempName + " = dso_local global float " + getFloatString(initVal.getInit(null, 0).get(0).getValueF()) + "\n");
                    }
                } else {
                    if (getDeclType().equals("i32")) {
                        addCode(tempName + " = dso_local global i32 0\n");
                    } else {
                        addCode(tempName + " = dso_local global float 0x00000000\n");
                    }
                }
            } else {
                addCode(tempName + " = alloca " + getDeclType() + "\n");
                if (initVal != null) {
                    initVal.addMidCode();
                    String temp = initVal.getInit(null, 0).get(0).getTemp();
                    if (getDeclType().equals("i32") && initVal.getInit(null, 0).get(0).getType().equals("float")) {
                        String nt = newTemp();
                        addCode(nt + " = fptosi float " + temp + " to i32\n");
                        temp = nt;
                    } else if (getDeclType().equals("float") && initVal.getInit(null, 0).get(0).getType().equals("i32")) {
                        String nt = newTemp();
                        addCode(nt + " = sitofp i32 " + temp + " to float\n");
                        temp = nt;
                    }
                    addCode("store " + getDeclType() + " " + temp + ", "
                            + getDeclType() + "* " + tempName + "\n");
                }
            }
        }
        else {  //数组形式
            if (isGlobal) {
                addCode(tempName + " = dso_local global ");
                if (initVal != null && !(getDeclType().equals("i32") && initVal.isAllZero())) {
                    ArrayList<Integer> dimsInt = new ArrayList<>();
                    for (Exp exp: dims) {
                        dimsInt.add(exp.getValue());
                    }
                    ArrayList<String> values = new ArrayList<>();
                    if (getDeclType().equals("i32")) {
                        for (Integer integer: initVal.getIntValues(dimsInt, 0)) {
                            values.add(String.valueOf(integer));
                        }
                    }
                    else if (getDeclType().equals("float")) {
                        for (Float f: initVal.getFloatValues(dimsInt, 0)) {
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
                if (initVal != null) {
                    ArrayList<Integer> dimsInt = new ArrayList<>();
                    int m = 1;
                    for (Exp exp : dims) {
                        dimsInt.add(exp.getValue());
                        m *= exp.getValue();
                    }
                    ArrayList<String> values = new ArrayList<>();
                    for (Exp exp: initVal.getInit(dimsInt, 0)) {
                        exp.addMidCode();
                        if (declType.equals("float") && exp.getType().equals("i32")) {
                            String nt = newTemp();
                            addCode(nt + " = sitofp i32 " + exp.getTemp() + " to float\n");
                            values.add(nt);
                        }
                        else {
                            values.add(exp.getTemp());
                        }
                    }
                    ArrayList<Integer> p = new ArrayList<>();
                    p.add(0);
                    if (m != 524288) {
                        localArrayInit(declType, dimsInt, values, 0, p, tempName);
                    }
                }
            }
        }
    }



    @Override
    public void addMidCode() {
        tableInsert();
        //todo 数组、初始化
        addElement();

    }

    void tableInsert() {
        if (isGlobal) {
            tempName = "@" + newGlobalName(id.getRawWord().getName());
        }
        else {
            tempName = newTemp();
        }
        ArrayList<Integer> dimsInt = new ArrayList<>();
        for (Exp exp : dims) {
            dimsInt.add(exp.getValue());
//            OutputControl.printMessage(exp.getValue());
        }
        if (getDeclType().equals("i32")) {
            table.addInteger(new IntegerItem(id.getRawWord().getName(), false, dimsInt, null, tempName));
        }
        else if (getDeclType().equals("float")) {
            table.addFloat(new FloatItem(id.getRawWord().getName(), false, dimsInt, null, tempName));
        }
        else {
            OutputControl.printMessage("\nERROR in VarDef!!!\n");
        }
    }
}
