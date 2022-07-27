package llvm.Type;

public class IntType extends Type {

    private int decimal;

    public IntType(TypeC typec, int decimal) {
        super(typec);
        this.decimal = decimal;
    }
}
