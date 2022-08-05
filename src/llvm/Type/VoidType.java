package llvm.Type;

public class VoidType extends Type{

    public VoidType(TypeC typec){
        super(typec);
    }

    @Override
    public String toString() {
        return "void";
    }
}
