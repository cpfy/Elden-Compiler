package llvm.Type;

public class PointerType extends Type{
    private Type t;

    public PointerType(TypeC typec, Type t) {
        super(typec);
        this.t=t;
    }
}
