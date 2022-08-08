package llvm.Type;

public class FloatType extends Type{

    public FloatType(TypeC typec){
        super(typec);
    }

    @Override
    public String toString() {
        return "float";
    }

    @Override
    public int getSpace() {
        return 4;
    }

    @Override
    public int getOffset() {
        return 4;
    }

    //todo
}
