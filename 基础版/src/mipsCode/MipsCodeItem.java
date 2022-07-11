package mipsCode;

public class MipsCodeItem {
    private MipsCodeType type;
    private String x;
    private String y;
    private String z;
    private int i;

    public MipsCodeItem(MipsCodeType type, String x, String y, String z) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public MipsCodeItem(MipsCodeType type, String x, String y, String z, int i) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.z = z;
        this.i = i;
    }

    @Override
    public String toString() {
        switch (type) {
            case dataSeg: {
                return ".data";
            }
            case asciizSeg: {
                return x + ": .asciiz" + " \"" + y + "\"";
            }
            case textSeg: {
                return ".text";
            }
            case j: {
                return "j " + x;
            }
            case lw: {
                return "lw " + x + ", " + i + "(" + y + ")";
            }
            case sw: {
                return "sw " + x + ", " + i + "(" + y + ")";
            }
            case li: {
                return "li " + z + ", " + x;
            }
            case addi: {
                return "addi " + z + ", " + x + ", " + y;
            }
            case add: {
                return "add " + z + ", " + x + ", " + y;
            }
            case sub: {
                return "sub " + z + ", " + x + ", " + y;
            }
            case mult: {
                return "mul $zero, " + x + ", " + y;
            }
            case divop:
            case modop: {
                return "div " + x + ", " + y;
            }
            case la: {
                return "la " + z + ", " + x;
            }
            case syscall: {
                return "syscall";
            }
            case label: {
                return z + ":";
            }
            case mfhi: {
                return "mfhi " + z;
            }
            case mflo: {
                return "mflo " + z;
            }
            case sll: {
                return "sll " + z + ", " + x + ", " + y;
            }
            case moveop: {
                return "move " + z + ", " + x;
            }
            case jr: {
                return "jr " + x;
            }
            case jal: {
                return "jal " + x;
            }
            case beq: {
                return "beq " + x + ", " + y + ", " + z;
            }
            case sne: {
                return "sne " + z + ", " + x + ", " + y;
            }
            case seq: {
                return "seq " + z + ", " + x + ", " + y;
            }
            case sle: {
                return "sle " + z + ", " + x + ", " + y;
            }
            case slt: {
                return "slt " + z + ", " + x + ", " + y;
            }
            case slti: {
                return "slti " + z + ", " + x + ", " + y;
            }
            case sge: {
                return "sge " + z + ", " + x + ", " + y;
            }
            case sgt: {
                return "sgt " + z + ", " + x + ", " + y;
            }
        }
        return "undefined: " + type;
    }
}
