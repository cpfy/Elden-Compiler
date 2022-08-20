package backend.Reg;

public class Register {

    private boolean virtual;    // 虚拟Reg
    private boolean valid;  // 有效

    private String name;

    // 包含全部virtual/physical Reg
    public Register(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    public boolean isVirtual() {
        return virtual;
    }

    public boolean isValid() {
        return valid;
    }
}
