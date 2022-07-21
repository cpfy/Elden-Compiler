package symbolTable;

import symbolTable.items.FloatItem;
import symbolTable.items.IntegerItem;
import symbolTable.items.TableItem;

import java.util.ArrayList;
import java.util.HashMap;

public class NumberTable {
    private HashMap<String, TableItem> numberItemHashMap = new HashMap<>();
    private NumberTable parent;

    public NumberTable(NumberTable parent) {
        this.parent = parent;
    }

    public void addInteger(IntegerItem integerItem) {
        numberItemHashMap.put(integerItem.getName(), integerItem);
    }

    public void addFloat(FloatItem floatItem) {
        numberItemHashMap.put(floatItem.getName(), floatItem);
    }

    public TableItem searchNumber(String name) {
        if (numberItemHashMap.containsKey(name)) {
            return numberItemHashMap.get(name);
        }
        return null;
    }

    public TableItem searchAllNumber(String name) {
        if (numberItemHashMap.containsKey(name)) {
            return numberItemHashMap.get(name);
        }
        if (parent == null) {
            return null;
        }
        return parent.searchAllNumber(name);
    }

    public NumberTable getParent() {
        return parent;
    }
}
