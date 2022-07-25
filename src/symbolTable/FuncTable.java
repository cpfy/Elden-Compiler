package symbolTable;

import symbolTable.items.FloatItem;
import symbolTable.items.FunctionItem;
import symbolTable.items.IntegerItem;
import symbolTable.items.TableItem;

import java.util.ArrayList;
import java.util.HashMap;

public class FuncTable {
    private HashMap<String, FunctionItem> functionItemHashMap = new HashMap<>();

    public FuncTable() {
        addGetint();
        addGetch();
        addGetfloat();
        addGetarray();
        addGetfarray();
        addPutint();
        addPutch();
        addPutfloat();
        addPutarray();
        addPutfarray();
        addStarttime();
        addStoptime();
    }

    private void addGetint() {
        ArrayList<TableItem> params = new ArrayList<>();
        FunctionItem functionItem = new FunctionItem("getint", "i32", params);
        functionItemHashMap.put(functionItem.getName(), functionItem);
    }

    private void addGetch() {
        ArrayList<TableItem> params = new ArrayList<>();
        FunctionItem functionItem = new FunctionItem("getch", "i32", params);
        functionItemHashMap.put(functionItem.getName(), functionItem);
    }

    private void addGetfloat() {
        ArrayList<TableItem> params = new ArrayList<>();
        FunctionItem functionItem = new FunctionItem("getfloat", "float", params);
        functionItemHashMap.put(functionItem.getName(), functionItem);
    }

    private void addGetarray() {
        ArrayList<TableItem> params = new ArrayList<>();
        ArrayList<Integer> dims = new ArrayList<>();
        dims.add(-1);
        params.add(new IntegerItem(null, false, dims, null, null, "i32*"));
        FunctionItem functionItem = new FunctionItem("getarray", "i32", params);
        functionItemHashMap.put(functionItem.getName(), functionItem);
    }

    private void addGetfarray() {
        ArrayList<TableItem> params = new ArrayList<>();
        ArrayList<Integer> dims = new ArrayList<>();
        dims.add(-1);
        params.add(new FloatItem(null, false, dims, null, null, "float*"));
        FunctionItem functionItem = new FunctionItem("getfarray", "i32", params);
        functionItemHashMap.put(functionItem.getName(), functionItem);
    }

    private void addPutint() {
        ArrayList<TableItem> params = new ArrayList<>();
        ArrayList<Integer> dims = new ArrayList<>();
        params.add(new IntegerItem(null, false, dims, null, null));
        FunctionItem functionItem = new FunctionItem("putint", "void", params);
        functionItemHashMap.put(functionItem.getName(), functionItem);
    }

    private void addPutch() {
        ArrayList<TableItem> params = new ArrayList<>();
        ArrayList<Integer> dims = new ArrayList<>();
        params.add(new IntegerItem(null, false, dims, null, null));
        FunctionItem functionItem = new FunctionItem("putch", "void", params);
        functionItemHashMap.put(functionItem.getName(), functionItem);
    }

    private void addPutfloat() {
        ArrayList<TableItem> params = new ArrayList<>();
        ArrayList<Integer> dims = new ArrayList<>();
        params.add(new FloatItem(null, false, dims, null, null));
        FunctionItem functionItem = new FunctionItem("putfloat", "void", params);
        functionItemHashMap.put(functionItem.getName(), functionItem);
    }

    private void addPutarray() {
        ArrayList<TableItem> params = new ArrayList<>();
        ArrayList<Integer> dims = new ArrayList<>();
        dims.add(-1);
        params.add(new IntegerItem(null, false, new ArrayList<>(), null, null));
        params.add(new IntegerItem(null, false, dims, null, null, "i32*"));
        FunctionItem functionItem = new FunctionItem("putarray", "void", params);
        functionItemHashMap.put(functionItem.getName(), functionItem);
    }

    private void addPutfarray() {
        ArrayList<TableItem> params = new ArrayList<>();
        ArrayList<Integer> dims = new ArrayList<>();
        dims.add(-1);
        params.add(new IntegerItem(null, false, new ArrayList<>(), null, null));
        params.add(new FloatItem(null, false, dims, null, null, "float*"));
        FunctionItem functionItem = new FunctionItem("putfarray", "void", params);
        functionItemHashMap.put(functionItem.getName(), functionItem);
    }

    private void addStarttime() {
        ArrayList<TableItem> params = new ArrayList<>();
        FunctionItem functionItem = new FunctionItem("starttime", "void", params);
        functionItemHashMap.put(functionItem.getName(), functionItem);
    }

    private void addStoptime() {
        ArrayList<TableItem> params = new ArrayList<>();
        FunctionItem functionItem = new FunctionItem("stoptime", "void", params);
        functionItemHashMap.put(functionItem.getName(), functionItem);
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
