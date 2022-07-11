package symbolTable.items;

import java.util.ArrayList;

public class ParamItem {
    private String name;
    private ArrayList<Integer> dims = new ArrayList<>();

    public ParamItem(String name, ArrayList<Integer> dims) {
        this.name = name;
        this.dims = dims;
    }

    public String getName() {
        return name;
    }

    public ArrayList<Integer> getDims() {
        return dims;
    }

    public IntegerItem toIntegerItem() {
        return new IntegerItem(name, false, dims, null);
    }
}
