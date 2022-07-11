package symbolTable.items;

import java.util.ArrayList;

public class IntegerItem {
    private String name;
    private boolean isConst;
    private boolean isPointer;
    private ArrayList<Integer> dims = new ArrayList<>();
    private ArrayList<Integer> values = new ArrayList<>();
    private int addr;
    private boolean isGlobal = false;

    public IntegerItem(String name, boolean isConst, ArrayList<Integer> dims, ArrayList<Integer> values) {
        this.name = name;
        this.isConst = isConst;
        this.dims = dims;
        this.values = values;
        this.isPointer = false;
    }

    public IntegerItem(String name, boolean isConst, ArrayList<Integer> dims, ArrayList<Integer> values, boolean isPointer) {
        this.name = name;
        this.isConst = isConst;
        this.dims = dims;
        this.values = values;
        this.isPointer = isPointer;
    }

    public boolean isGlobal() {
        return isGlobal;
    }

    public void setGlobal(boolean global) {
        isGlobal = global;
    }

    public boolean isPointer() {
        return isPointer;
    }

    public String getName() {
        return name;
    }

    public boolean isConst() {
        return isConst;
    }

    public ArrayList<Integer> getDims() {
        return dims;
    }

    public int getValue(int i) {
        return values.get(i);
    }

    public void setValue(int i, int value) {
        values.set(i, value);
    }

    public int getDim2() {
        return dims.get(1);
    }

    public int getAddr() {
        return addr;
    }

    public void setAddr(int addr) {
        this.addr = addr;
        System.out.println(name + " : " + addr);
    }
}
