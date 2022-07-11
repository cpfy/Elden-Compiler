package AST;

import midCode.MidCodeType;
import word.WordType;

import java.util.ArrayList;

public class LVal extends ExpPrimary {
    private ID id;
    private ArrayList<Exp> dims = new ArrayList<>();
    private String temp;
    private boolean isAssign = false;

    public LVal(ID id, ArrayList<Exp> dims) {
        this.id = id;
        this.dims = dims;
    }

    public ID getId() {
        return id;
    }

    public void setAssign(boolean assign) {
        isAssign = assign;
    }

    @Override
    public String getTemp() {
        if (dims.size() == 0) {
            return id.getRawWord().getName();
        }
        return temp;
    }

    @Override
    public void calculate() {
        int index = 0;
        if (dims.size() == 1) {
            index = dims.get(0).getValue();
        }
        else if (dims.size() == 2){
            index = dims.get(0).getValue() * table.getDim2(id.getRawWord().getName()) + dims.get(1).getValue();
        }
        value = table.getValue(id.getRawWord().getName(), index);
    }

    public boolean isArray() {
        return dims.size() != 0;
    }

    @Override
    public void addMidCode() {
        if (isAssign) {
            if (dims.size() == 2) {
                String temp1 = newTemp();
                dims.get(0).addMidCode();
                dims.get(1).addMidCode();
                midCodeList.addMidCodeItem(MidCodeType.MULTOP, dims.get(0).getTemp(),
                        String.valueOf(table.getDim2(id.getRawWord().getName())), temp1);
                temp = newTemp();
                midCodeList.addMidCodeItem(MidCodeType.PLUSOP, temp1, dims.get(1).getTemp(), temp);
            } else if (dims.size() == 1) {
                dims.get(0).addMidCode();
                temp = dims.get(0).getTemp();
            } else {

            }
        }
        else {
            if (dims.size() == 2) {
                String temp1 = newTemp();
                String temp2 = newTemp();
                dims.get(0).addMidCode();
                dims.get(1).addMidCode();
                midCodeList.addMidCodeItem(MidCodeType.MULTOP, dims.get(0).getTemp(),
                        String.valueOf(table.getDim2(id.getRawWord().getName())), temp1);
                midCodeList.addMidCodeItem(MidCodeType.PLUSOP, temp1, dims.get(1).getTemp(), temp2);
                temp = newTemp();
                midCodeList.addMidCodeItem(MidCodeType.GETARRAY, temp2, id.getRawWord().getName(), temp);
            } else if (dims.size() == 1) {
                if (table.getDimNum(id.getRawWord().getName()) == 1) {
                    dims.get(0).addMidCode();
                    temp = newTemp();
                    midCodeList.addMidCodeItem(MidCodeType.GETARRAY, dims.get(0).getTemp(), id.getRawWord().getName(), temp);
                }
                else {
                    dims.get(0).addMidCode();
                    temp = id.getRawWord().getName() + "@" + dims.get(0).getTemp();
                }
            } else {

            }
        }
    }
}
