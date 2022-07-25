package symbolTable.items;

import java.util.ArrayList;

public abstract class TableItem {
    public abstract String getVarType();

    public abstract String getTempName();

    public abstract ArrayList<Integer> getDims();

    public abstract boolean isConst();

    public abstract String getValueString();

    public abstract String getDetailType();
}
