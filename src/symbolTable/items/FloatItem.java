package symbolTable.items;

import java.util.ArrayList;

public class FloatItem extends TableItem {
    private String tempName;
    private String name;
    private boolean isConst;
    private ArrayList<Integer> dims = new ArrayList<>();
    private ArrayList<Float> values = new ArrayList<>();
    private boolean isGlobal = false;
    private String detailType;

    public FloatItem(String name, boolean isConst, ArrayList<Integer> dims, ArrayList<Float> values, String tempName) {
        this.name = name;
        this.isConst = isConst;
        this.dims = dims;
        this.values = values;
        this.tempName = tempName;
        this.detailType = "float";
    }

    public FloatItem(String name, boolean isConst, ArrayList<Integer> dims, ArrayList<Float> values, String tempName, String detailType) {
        this.name = name;
        this.isConst = isConst;
        this.dims = dims;
        this.values = values;
        this.tempName = tempName;
        this.detailType = detailType;
    }

    public String getDetailType() {
        return detailType;
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

    public float getValue(int i) {
        return values.get(i);
    }

    public void setValue(int i, float value) {
        values.set(i, value);
    }

    @Override
    public String getValueString() {
        return String.valueOf(values.get(0));
    }

    @Override
    public String getVarType() {
        return "float";
    }
}
