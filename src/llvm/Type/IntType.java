package llvm.Type;

public class IntType extends Type {

    private int decimal;

    public IntType(TypeC typec, int decimal) {
        super(typec);
        this.decimal = decimal;
    }

    @Override
    public String toString() {
        return "i" + decimal;
    }

    @Override
    public int getSpace() {
        //return (decimal + 7) / 8;
        return 4;
    }

    @Override
    public int getOffset() {
        return 4;
    }
}
