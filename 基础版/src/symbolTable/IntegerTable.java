package symbolTable;

import symbolTable.items.IntegerItem;

import java.util.HashMap;

public class IntegerTable {
    private HashMap<String, IntegerItem> integerItemHashMap = new HashMap<>();
    private IntegerTable parent;
    private int maxaddr;

    public IntegerTable(IntegerTable parent) {
        this.parent = parent;
    }

    public void addInteger(IntegerItem integerItem) {
        integerItemHashMap.put(integerItem.getName(), integerItem);
    }

    public IntegerItem searchInteger(String name) {
        if (integerItemHashMap.containsKey(name)) {
            return integerItemHashMap.get(name);
        }
        return null;
    }

    public int getMaxaddr() {
        return maxaddr;
    }

    public void setMaxaddr(int maxaddr) {
        this.maxaddr = maxaddr;
    }

    public IntegerItem searchAllInteger(String name) {
        if (integerItemHashMap.containsKey(name)) {
            return integerItemHashMap.get(name);
        }
        if (parent == null) {
            return null;
        }
        return parent.searchAllInteger(name, maxaddr);
    }

    public IntegerItem searchAllInteger(String name, int addr) {
        if (integerItemHashMap.containsKey(name)) {
            return integerItemHashMap.get(name);
        }
        if (parent == null) {
            return null;
        }
        return parent.searchAllInteger(name, addr);
    }

    public IntegerTable getParent() {
        return parent;
    }
}
