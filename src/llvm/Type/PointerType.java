package llvm.Type;

public class PointerType extends Type {
    private Type t;

    public PointerType(TypeC typec, Type t) {
        super(typec);
        this.t = t;
    }

    @Override
    public String toString() {
        return t.toString() + "*";
    }

    @Override
    public int getSpace() {
        return 4;
    }

    @Override
    public int getOffset() {
        return t.getOffset();
    }
}
