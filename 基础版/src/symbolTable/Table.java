package symbolTable;

import symbolTable.items.FunctionItem;
import symbolTable.items.IntegerItem;
import symbolTable.items.ParamItem;

import java.util.ArrayList;

public class Table {
    private IntegerTable tableNow;
    private FuncTable funcTable;
    private IntegerTable globalTable;
    private FunctionItem functionNow = null;
    private boolean funcFlag = false;
    private int addr = 0;

    public Table() {
        tableNow = new IntegerTable(null);
        globalTable = tableNow;
        funcTable = new FuncTable();
    }

    public void newFunc() {
        tableNow = new IntegerTable(tableNow);
        funcFlag = true;
    }

    public void newBlock() {
        if (funcFlag) {
            funcFlag = false;
            return;
        }
        tableNow = new IntegerTable(tableNow);
    }

    public void deleteBlock() {
        tableNow.setMaxaddr(addr); //  <
        tableNow = tableNow.getParent();
    }

    public int getFuncLen(String name) {
        return funcTable.searchFunc(name).getLen();
    }

    public void endMain() {
        functionNow.setLen(addr);
    }

    public boolean addFunc(FunctionItem functionItem) {
        if (true) {
            if (functionNow != null) {
                functionNow.setLen(addr);
                addr = 0;
            }
            functionNow = functionItem;
            funcTable.addFunc(functionItem);
            return true;
        }
        return false;
    }

    public boolean addInteger(IntegerItem integerItem) {
        if (tableNow.searchInteger(integerItem.getName()) == null) {
            tableNow.addInteger(integerItem);
            integerItem.setAddr(addr);
            ArrayList<Integer> dims = integerItem.getDims();
            if (dims.size() == 0 || integerItem.isPointer()) {
                addr++;
            }
            else if (dims.size() == 1) {
                addr += dims.get(0);
            }
            else if (dims.size() == 2) {
                addr += dims.get(0) * dims.get(1);
            }
            return true;
        }
        return false;
    }

    public String getFuncType(String name) {
        return funcTable.searchFunc(name).getRetType();
    }

    public int getValue(String name, int index) {
        return tableNow.searchAllInteger(name).getValue(index);
    }

    public int getDimNum(String name) {
        return tableNow.searchAllInteger(name).getDims().size();
    }

    public int getDim2(String name) {
        return tableNow.searchAllInteger(name).getDim2();
    }

    public int getAddr(String name) {
        if (tableNow.searchAllInteger(name) != null) {
            return tableNow.searchAllInteger(name).getAddr();
        }
        return -1;
    }

    public int getParamNum(String name) {
        return funcTable.searchFunc(name).getParamNum();
    }

    public boolean isPointer(String name) {
        if (tableNow.searchAllInteger(name) == null) {
            return false;
        }
        return tableNow.searchAllInteger(name).isPointer();
    }

}
