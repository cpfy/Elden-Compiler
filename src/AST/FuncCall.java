package AST;


import symbolTable.NumberTable;
import symbolTable.items.TableItem;

import javax.print.attribute.standard.NumberOfInterveningJobs;
import java.util.ArrayList;

public class FuncCall extends ExpUnary {
    private ID id;
    private ArrayList<Exp> params = new ArrayList<>();
    private String temp;

    public FuncCall(ID id, ArrayList<Exp> params) {
        this.id = id;
        this.params = params;
    }



    @Override
    public String addCodePre() {
        setType(table.getFunction(id.getRawWord().getName()).getRetType());
        String retType = table.getFuncType(id.getRawWord().getName());
        ArrayList<TableItem> funcFParams = table.getFunction(id.getRawWord().getName()).getParamItems();
        ArrayList<String> realParams = new ArrayList<>();
        for (int i = 0; i < funcFParams.size(); i++) {
            addCode(params.get(i).addCodePre());
            String paraTemp = params.get(i).getTemp();
            if (funcFParams.get(i).getVarType().equals("i32") && params.get(i).getType().equals("float")) {
                String nt = newTemp();
                addCode(nt + " = fptosi float " + paraTemp + " to i32\n");
                paraTemp = nt;
            }
            else if (funcFParams.get(i).getVarType().equals("float") && params.get(i).getType().equals("i32")) {
                String nt = newTemp();
                addCode(nt + " = sitofp i32 " + paraTemp + " to float\n");
                paraTemp = nt;
            }
            realParams.add(paraTemp);
        }

        if (!retType.equals("void")) {
            temp = newTemp();
            addCode(temp + " = ");
        }
        addCode("call " + retType + " @" + id.getRawWord().getName() + "(");

        for (int i = 0; i < params.size(); i++) {
            if (i > 0) {
                addCode(", ");
            }
            addCode(funcFParams.get(i).getVarType() + " " + realParams.get(i));
        }

        addCode(")\n");
        return getCodes().toString();
//        for (Exp exp: params) {
//            exp.addMidCode();
//            midCodeList.addMidCodeItem(MidCodeType.PUSH, null, null, exp.getTemp());
//        }
//        midCodeList.addMidCodeItem(MidCodeType.CALL, null, null, id.getRawWord().getName());
//        if (table.getFuncType(id.getRawWord().getName()).equals("int")) {
//            temp = newTemp();
//            midCodeList.addMidCodeItem(MidCodeType.RETVALUE, null, null, temp);
    }

    @Override
    public String getTemp() {
        return temp;
    }

    @Override
    public void calculate() {

    }

    @Override
    public void addMidCode() {
        addCodePre();
        generate();
    }
}
