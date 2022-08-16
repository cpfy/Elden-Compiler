package backend.Arm;

public class HeadArm extends Arm {
    private String str;

    // 完整的一个str
    public HeadArm(String str) {
        super("head");
        this.str = str;
    }

    @Override
    public String toString() {
        if (super.isWithtab()) {
            return "\t" + str;
        }
        return str;
    }

    public String getStr() {
        return str;
    }
}
