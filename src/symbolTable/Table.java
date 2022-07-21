package symbolTable;

import symbolTable.items.FloatItem;
import symbolTable.items.FunctionItem;
import symbolTable.items.IntegerItem;
import symbolTable.items.TableItem;

import java.util.ArrayList;

public class Table {
    private NumberTable tableNow;
    private FuncTable funcTable;
    private FunctionItem functionNow = null;
    private boolean funcFlag = false;

    public Table() {
        tableNow = new NumberTable(null);
        funcTable = new FuncTable();
    }

    public void newFunc() {
        tableNow = new NumberTable(tableNow);
        funcFlag = true;
    }

    public void newBlock() {
        if (funcFlag) {
            funcFlag = false;
            return;
        }
        tableNow = new NumberTable(tableNow);
    }

    public void deleteBlock() {
        tableNow = tableNow.getParent();
    }

    public boolean addFunc(FunctionItem functionItem) {
        if (true) {
            functionNow = functionItem;
            funcTable.addFunc(functionItem);
            return true;
        }
        return false;
    }

    public FunctionItem getFunctionNow() {
        return functionNow;
    }

    //通过变量名获取变量类型
    public String getVarType(String name) {
        return tableNow.searchAllNumber(name).getVarType();
    }

    public boolean addInteger(IntegerItem integerItem) {
        if (tableNow.searchNumber(integerItem.getName()) == null) {
            tableNow.addInteger(integerItem);
            return true;
        }
        return false;
    }

    public boolean addFloat(FloatItem floatItem) {
        if (tableNow.searchNumber(floatItem.getName()) == null) {
            tableNow.addFloat(floatItem);
            return true;
        }
        return false;
    }

    public ArrayList<Integer> getVarDimsByName(String name) {
        return tableNow.searchAllNumber(name).getDims();
    }

    public void addParam(TableItem param) {
        functionNow.addParam(param);
    }

    public String getVarTempName(String name) {
        return tableNow.searchAllNumber(name).getTempName();
    }

    public String getFuncType(String name) {
        return funcTable.searchFunc(name).getRetType();
    }

    public TableItem getValue(String name) {
        return tableNow.searchAllNumber(name);
    }

    public int getIndex(String name, ArrayList<Integer> inputDims) {
        ArrayList<Integer> dims = tableNow.searchAllNumber(name).getDims();
        int index = 0;
        int align = 1;
        for (Integer integer: dims) {
            align *= integer;
        }
        for (int i = 0; i < inputDims.size(); i++) {
            align /= dims.get(i);
            index += align * inputDims.get(i);
        }
        return index;
    }

    public boolean isConst(String name) {
        return tableNow.searchAllNumber(name).isConst();
    }

    public FunctionItem getFunction(String name) {
        return funcTable.searchFunc(name);
    }
}
