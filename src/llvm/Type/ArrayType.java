package llvm.Type;

import java.util.ArrayList;

public class ArrayType extends Type {
    private int dimen;
    private ArrayList<Integer> dimenlist;

    public ArrayType(TypeC typec, ArrayList<Integer> dimenlist) {
        super(typec);
        this.dimenlist = dimenlist;
    }
}
