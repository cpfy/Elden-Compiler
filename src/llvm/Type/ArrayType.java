package llvm.Type;

import java.util.ArrayList;

public class ArrayType extends Type {
    private int dimen;
    private ArrayList<Integer> dimenlist;

    // ArrayType : "[" int_lit "x" Type "]"
    private int int_lit;
    private Type t;

    // 实际未用
    public ArrayType(TypeC typec, ArrayList<Integer> dimenlist) {
        super(typec);
        this.dimenlist = dimenlist;
    }

    public ArrayType(TypeC typec, int i, Type t) {
        super(typec);
        this.int_lit = i;
        this.t = t;
    }

    @Override
    public String toString() {
        return "[" + String.valueOf(int_lit) + " x " + t.toString() + "]";
    }


    // get space
    public int getSpace() {
        return int_lit * this.t.getSpace();
    }

}
