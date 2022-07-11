package symbolTable.items;

import java.util.ArrayList;

public class FunctionItem {
    private String name;
    private String retType;
    private ArrayList<ParamItem> paramItems = new ArrayList<>();
    private int len;

    private int paramNum = 0;

    public FunctionItem(String name, String retType, ArrayList<ParamItem> paramItems) {
        this.name = name;
        this.retType = retType;
        this.paramItems = paramItems;
    }

    public String getName() {
        return name;
    }

    public String getRetType() {
        return retType;
    }

    public ArrayList<ParamItem> getParamItems() {
        return paramItems;
    }

    public int getLen() {
        return len;
    }

    public void setLen(int len) {
        this.len = len;
    }

    public int getParamNum() {
        return paramNum;
    }

    public void addParamNum() {
        paramNum++;
    }
}
