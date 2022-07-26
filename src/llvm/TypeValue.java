package llvm;

public class TypeValue {
    private Type type;
    private Value value;

    public TypeValue(Type type, Value value){
        this.type = type;
        this.value = value;
    }

    public Type getType() {
        return type;
    }

    public Value getValue() {
        return value;
    }
}
