package symbolTable.items;

import symbolTable.Table;

import java.util.ArrayList;

public class FunctionItem {
    private String name;
    private String retType;
    private ArrayList<TableItem> paramItems = new ArrayList<>();

    public FunctionItem(String name, String retType, ArrayList<TableItem> paramItems) {
        this.name = name;
        this.retType = retType;
        if (paramItems != null) {
            this.paramItems.addAll(paramItems);
        }
    }

    public String getName() {
        return name;
    }

    public String getRetType() {
        return retType;
    }

    public ArrayList<TableItem> getParamItems() {
        return paramItems;
    }

    public void addParam(TableItem paramItem) {
        paramItems.add(paramItem);
    }
}
