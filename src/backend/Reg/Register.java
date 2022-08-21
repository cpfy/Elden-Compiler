package backend.Reg;

public class Register implements Comparable<Register> {

    private boolean virtual;    // 虚拟Reg
    private boolean valid;  // 有效

    private int no; // r0-r2,sp,pc,lr分别为0-15；s0-s31为16-47号
    private String name;

    // 包含全部virtual/physical Reg
    public Register(int no, String name) {
        this.no = no;
        this.name = name;
    }

    public int getNo() {
        return no;
    }

    public String getName() {
        return name;
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

    @Override
    public int compareTo(Register Other) {
        return Integer.compare(no, Other.getNo());
    }
}
