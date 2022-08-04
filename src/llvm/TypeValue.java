package llvm;

import llvm.Type.Type;

public class TypeValue {
    private Type type;
    private Value value;

    public TypeValue(Type type, Value value) {
        this.type = type;
        this.value = value;
    }

    @Override
    public String toString() {
        return type.toString() + " " + value.toString();
    }

    public Type getType() {
        return type;
    }

    public Value getValue() {
        return value;
    }

    public void setValue(Value value) {
        this.value = value;
    }
}
