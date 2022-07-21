package AST;

import symbolTable.items.FloatItem;
import symbolTable.items.IntegerItem;

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
                        addCode(tempName + " = dso_local global i32 " + initVal.getInit().get(0).getValue() + "\n");
                    } else {
                        addCode(tempName + " = dso_local global float " + initVal.getInit().get(0).getValueF() + "\n");
                    }
                } else {
                    if (getDeclType().equals("i32")) {
                        addCode(tempName + " = dso_local global i32 0\n");
                    } else {
                        addCode(tempName + " = dso_local global float 0.000000e+00\n");
                    }
                }
            } else {
                addCode(tempName + " = alloca " + getDeclType() + "\n");
                if (initVal != null) {
                    initVal.addMidCode();
                    String temp = initVal.getInit().get(0).getTemp();
                    if (getDeclType().equals("i32") && initVal.getInit().get(0).getType().equals("float")) {
                        String nt = newTemp();
                        addCode(nt + " = fptosi float " + temp + " to i32\n");
                        temp = nt;
                    } else if (getDeclType().equals("float") && initVal.getInit().get(0).getType().equals("i32")) {
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
                addCode(getArrayType(dims, getDeclType()) + " ");
                if (initVal != null) {

                } else {
                    addCode("zeroinitializer\n");
                }
            }
            else {
                addCode(tempName + " = alloca " + getArrayType(dims, getDeclType()) + "\n");
                if (initVal != null) {
                    //todo 数组初始化！！！！
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
            tempName = "@" + id.getRawWord().getName();
        }
        else {
            tempName = newTemp();
        }
        ArrayList<Integer> dimsInt = new ArrayList<>();
        for (Exp exp : dims) {
            dimsInt.add(exp.getValue());
            System.out.println(exp.getValue());
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
