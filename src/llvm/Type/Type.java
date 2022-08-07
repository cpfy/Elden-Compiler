package llvm.Type;

public abstract class Type {

    private TypeC typec;

    public Type(TypeC typec) {
        this.typec = typec;
    }

    public TypeC getTypec() {
        return typec;
    }

    // 占用空间
    public abstract int getSpace();
}
