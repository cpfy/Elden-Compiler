package AST;

import symbolTable.items.FloatItem;
import symbolTable.items.IntegerItem;
import word.WordType;

import java.util.ArrayList;

public class LVal extends ExpPrimary {
    private ID id;
    private ArrayList<Exp> dims = new ArrayList<>();
    private String temp;
    private boolean isAssign = false;

    public LVal(ID id, ArrayList<Exp> dims) {;
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
    public ArrayList<String> addCodePre() {
        setType(table.getVarType(id.getRawWord().getName()));
        if (table.getValue(id.getRawWord().getName()).isConst()) {
            calculate();
            if (isCanCal()) {
                if (type.equals("i32")) {
                    temp = String.valueOf(getValue());
                }
                else {
                    temp = getFloatString(getValueF());
                }
            }
            return getCodes();
        }
        if (isAssign) {
            addAsAssign();
        }
        else {
            addNotAsAssign();
        }
        return getCodes();
    }

    @Override
    public String getTemp() {
        return temp;
    }

    @Override
    public void calculate() {
        setType(table.getVarType(id.getRawWord().getName()));
        if (!table.getValue(id.getRawWord().getName()).isConst()) {
            setCanCal(false);
            return;
        }
        ArrayList<Integer> myDims = new ArrayList<>();
        for (Exp exp: dims) {
            exp.calculate();
            if (exp.isCanCal()) {
                myDims.add(exp.getValue());
            }
            else {
                setCanCal(false);
                return;
            }
        }
        int index = table.getIndex(id.getRawWord().getName(), myDims);
        if (type.equals("i32")) {
            value = ((IntegerItem) table.getValue(id.getRawWord().getName())).getValue(index);
            valueF = value;
        }
        else if (type.equals("float")) {
            valueF = ((FloatItem) table.getValue(id.getRawWord().getName())).getValue(index);
            value = (int) valueF;
        }
    }

    public boolean isArray() {
        return table.getVarDimsByName(id.getRawWord().getName()).size() != 0;
    }

    private ArrayList<String> getPtr() {
        ArrayList<String> ans = new ArrayList<>();
        this.temp = table.getVarTempName(id.getRawWord().getName());
        if (dims.size() == 0) {
            return ans;
        }
        ArrayList<Integer> varDims = table.getVarDimsByName(id.getRawWord().getName());
        if (varDims.get(0) == -1) {
            String nt = newTemp();
            String arrayType = getArrayType(varDims, type, 1);
            ans.add(nt + " = load " + arrayType + "*, " + arrayType + "** " + temp + "\n");
            this.temp = nt;
            ans.addAll(dims.get(0).addCodePre());
            nt = newTemp();
            ans.add(nt + " = getelementptr inbounds " + arrayType + ", " + arrayType + "* " + temp + ", i32 " + dims.get(0).getTemp() + "\n");
            this.temp = nt;
        }
        else {
            ans.addAll(dims.get(0).addCodePre());
            String nt = newTemp();
            String arrayType = getArrayType(varDims, type, 0);
            ans.add(nt + " = getelementptr inbounds " + arrayType + ", " + arrayType + "* " + temp + ", i32 0" + ", i32 " + dims.get(0).getTemp() + "\n");
            this.temp = nt;
        }
        for (int i = 1; i < dims.size(); i++) {
            ans.addAll(dims.get(i).addCodePre());
            String nt = newTemp();
            String arrayType = getArrayType(varDims, type, i);
            ans.add(nt + " = getelementptr inbounds " + arrayType + ", " + arrayType + "* " + temp + ", i32 0" + ", i32 " + dims.get(i).getTemp() + "\n");
            this.temp = nt;
        }
        return ans;
    }

    private void addAsAssign() {
        if (!isArray()) {
            temp = table.getVarTempName(id.getRawWord().getName());
        }
        else {
            addCode(getPtr());
        }
    }

    private void addNotAsAssign() {
        if (!isArray()) {
            if (!table.isConst(id.getRawWord().getName())) {
                temp = newTemp();
                addCode(temp + " = load " + table.getVarType(id.getRawWord().getName())
                        + ", " + table.getVarType(id.getRawWord().getName()) + "* "
                        + table.getVarTempName(id.getRawWord().getName()) + "\n");
            }
            else {
                temp = table.getValue(id.getRawWord().getName()).getValueString();
            }
        }
        else {
            addCode(getPtr());
            ArrayList<Integer> varDims = table.getVarDimsByName(id.getRawWord().getName());
            String arrayType = getArrayType(varDims, type, dims.size());
            String nt = newTemp();
            if (isPoint()) {
                if (varDims.get(0) == -1 && dims.size() == 0) {
                    arrayType = getArrayType(varDims, type, dims.size() + 1);
                    addCode(nt + " = load " + arrayType
                            + "*, " + arrayType + "** "
                            + temp + "\n");
                }
                else {
                    addCode(nt + " = getelementptr inbounds " + arrayType
                            + ", " + arrayType + "* " + temp + ", i32 0, i32 0\n");
                }
            }
            else {
                addCode(nt + " = load " + arrayType
                        + ", " + arrayType + "* "
                        + temp + "\n");
            }
            temp = nt;
        }
    }

    @Override
    public void addMidCode() {
        addCodePre();
        generate();
    }
}
