package symbolTable.items;

import java.util.ArrayList;

public class IntegerItem extends TableItem {
    private String tempName;
    private String name;
    private boolean isConst;
    private ArrayList<Integer> dims = new ArrayList<>();
    private ArrayList<Integer> values = new ArrayList<>();
    private boolean isGlobal = false;

    public IntegerItem(String name, boolean isConst, ArrayList<Integer> dims, ArrayList<Integer> values, String tempName) {
        this.name = name;
        this.isConst = isConst;
        this.dims = dims;
        this.values = values;
        this.tempName = tempName;
    }

    @Override
    public String getTempName() {
        return tempName;
    }

    public boolean isGlobal() {
        return isGlobal;
    }

    public void setGlobal(boolean global) {
        isGlobal = global;
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

    @Override
    public String getVarType() {
        return "i32";
    }
}
