package AST;

import symbolTable.items.IntegerItem;

import java.util.ArrayList;


public class ConstDef extends Def {
    private ID id;
    private ArrayList<Exp> dims;
    private ConstInitVal constInitVal;

    public ConstDef(ID id, ArrayList<Exp> dims, ConstInitVal constInitVal) {
        this.id = id;
        this.dims = dims;
        this.constInitVal = constInitVal;
    }



    @Override
    public void addMidCode() {

    }

    void tableInsert(String tempName) {
        ArrayList<Integer> dimsInt = new ArrayList<>();
        for (Exp exp: dims) {
            dimsInt.add(exp.getValue());
        }
        table.addInteger(new IntegerItem(id.getRawWord().getName(), true, dimsInt, constInitVal.getInitValues(), tempName));

    }
}
