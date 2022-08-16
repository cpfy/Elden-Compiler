package backend.Arm;

public class TmpArm extends Arm {
    private String str;

    // 给ldr，str过渡用
    public TmpArm(String str) {
        super("tmp");
        this.str = str;
    }

    @Override
    public String toString() {
        return str;
    }

    public String getStr() {
        return str;
    }
}
