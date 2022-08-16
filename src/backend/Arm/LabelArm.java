package backend.Arm;

public class LabelArm extends Arm {

    private String label;

    // 默认
    public LabelArm(String label) {
        super("label");
        this.label = label;
    }

    @Override
    public String toString() {
        return label + ":";
    }

    public String getLabel() {
        return label;
    }
}
