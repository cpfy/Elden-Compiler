package llvm.Type;

public abstract class Type {

    private TypeC typec;

    // 是否在pointer内最贴近的一层
    private boolean inpointer = false;


    public Type(TypeC typec) {
        this.typec = typec;
    }

    public TypeC getTypec() {
        return typec;
    }

    public boolean isInpointer() {
        return inpointer;
    }

    public void setInpointer(boolean ip) {
        this.inpointer = ip;
    }

    // 占用空间，初始化用
    public abstract int getSpace();

    // 计算type对应需要offset的空间
    public abstract int getOffset();
}
