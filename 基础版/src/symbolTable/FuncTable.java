package symbolTable;

import symbolTable.items.FunctionItem;

import java.util.HashMap;

public class FuncTable {
    private HashMap<String, FunctionItem> functionItemHashMap = new HashMap<>();

    public FuncTable() {

    }

    public void addFunc(FunctionItem functionItem) {
        functionItemHashMap.put(functionItem.getName(), functionItem);
    }

    public FunctionItem searchFunc(String name) {
        if (functionItemHashMap.containsKey(name)) {
            return functionItemHashMap.get(name);
        }
        return null;
    }
}
