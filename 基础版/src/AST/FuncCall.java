package AST;

import midCode.MidCodeType;

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
    public String getTemp() {
        return temp;
    }

    @Override
    public void calculate() {

    }

    @Override
    public void addMidCode() {
        for (Exp exp: params) {
            exp.addMidCode();
            midCodeList.addMidCodeItem(MidCodeType.PUSH, null, null, exp.getTemp());
        }
        midCodeList.addMidCodeItem(MidCodeType.CALL, null, null, id.getRawWord().getName());
        if (table.getFuncType(id.getRawWord().getName()).equals("int")) {
            temp = newTemp();
            midCodeList.addMidCodeItem(MidCodeType.RETVALUE, null, null, temp);
        }
    }
}
