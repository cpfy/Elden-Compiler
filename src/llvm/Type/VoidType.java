package llvm.Type;

public class VoidType extends Type{

    public VoidType(TypeC typec){
        super(typec);
    }

    @Override
    public String toString() {
        return "void";
    }

    @Override
    public int getSpace() {
        return 0;
    }

    @Override
    public int getOffset() {
        return 0;
    }
}
