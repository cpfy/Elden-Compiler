package AST;

import symbolTable.items.FloatItem;
import symbolTable.items.IntegerItem;

import java.util.ArrayList;

public class VarDef extends Def {
    private ID id;
    private ArrayList<Exp> dims;
    private InitVal initVal;
    private String tempName = null;

    public VarDef(ID id, ArrayList<Exp> dims, InitVal initVal) {
        this.id = id;
        this.dims = dims;
        this.initVal = initVal;
    }

    private void addElement() {
        if (isGlobal) {
            if (initVal != null) {
                if (getDeclType().equals("i32")) {
                    addCode(tempName + " = dso_local global i32 " + initVal.getInit().get(0).getValue() + "\n");
                }
                else {
                    addCode(tempName + " = dso_local global float " + initVal.getInit().get(0).getValueF() + "\n");
                }
            }
            else {
                if (getDeclType().equals("i32")) {
                    addCode(tempName + " = dso_local global i32 0\n");
                }
                else {
                    addCode(tempName + " = dso_local global float 0.000000e+00\n");
                }
            }
        }
        else {
            addCode(tempName + " = alloca " + getDeclType() + "\n");
            String temp = initVal.getInit().get(0).getTemp();
            if (initVal != null) {
                //todo 类型转换
                initVal.addMidCode();
                if (getDeclType().equals("i32") && initVal.getInit().get(0).getType().equals("float")) {
                    String nt = newTemp();
                    addCode(nt + " = fptosi float " + temp + " to i32\n");
                    temp = nt;
                }
                else if (getDeclType().equals("float") && initVal.getInit().get(0).getType().equals("i32")) {
                    String nt = newTemp();
                    addCode(nt + " = sitofp i32 " + temp + " to float\n");
                    temp = nt;
                }
                addCode("store " + getDeclType() + " " + temp + ", "
                        + getDeclType() + "* " + tempName + "\n");
            }
        }
    }

    @Override
    public void addMidCode() {
        tableInsert();
        //todo 数组、初始化
        addElement();
        


//        ArrayList<Exp> exps = null;
//        if (dims.size() == 0) {
//            if (initVal != null) {
//                initVal.addMidCode();
//                midCodeList.addMidCodeItem(MidCodeType.VAR, initVal.getInit().get(0).getTemp(), null, id.getRawWord().getName());
//            }
//            else {
//                midCodeList.addMidCodeItem(MidCodeType.VAR, null, null, id.getRawWord().getName());
//            }
//        }
//        else if (dims.size() == 1) {
//            midCodeList.addMidCodeItem(MidCodeType.ARRAY, String.valueOf(dims.get(0).getValue()), null, id.getRawWord().getName());
//            if (initVal != null) {
//                initVal.addMidCode();
//                exps = initVal.getInit();
//                for (int i = 0; i < dims.get(0).getValue(); i++) {
//                    midCodeList.addMidCodeItem(MidCodeType.PUTARRAY, exps.get(i).getTemp(), String.valueOf(i), id.getRawWord().getName());
//                }
//            }
//        }
//        else if (dims.size() == 2) {
//            midCodeList.addMidCodeItem(MidCodeType.ARRAY, String.valueOf(dims.get(0).getValue()),
//                    String.valueOf(dims.get(1).getValue()), id.getRawWord().getName());
//            if (initVal != null) {
//                initVal.addMidCode();
//                exps = initVal.getInit();
//                int t = dims.get(1).getValue();
//                for (int i = 0; i < dims.get(0).getValue(); i++) {
//                    for (int j = 0; j < dims.get(1).getValue(); j++) {
//                        midCodeList.addMidCodeItem(MidCodeType.PUTARRAY, exps.get(i * t + j).getTemp(), String.valueOf(i * t + j), id.getRawWord().getName());
//                    }
//                }
//            }
//        }
    }

    void tableInsert() {
        if (isGlobal) {
            tempName = "@" + id.getRawWord().getName();
        }
        else {
            tempName = newTemp();
        }
        ArrayList<Integer> dimsInt = new ArrayList<>();
        for (Exp exp : dims) {
            dimsInt.add(exp.getValue());
        }
        if (getDeclType().equals("i32")) {
            table.addInteger(new IntegerItem(id.getRawWord().getName(), false, dimsInt, null, tempName));
        }
        else if (getDeclType().equals("float")) {
            table.addFloat(new FloatItem(id.getRawWord().getName(), false, dimsInt, null, tempName));
        }
        else {
            System.out.println("\nERROR in VarDef!!!\n");
        }
    }
}
