package llvm;

import llvm.Type.Type;

public class Ident {

    private boolean isIdent;
    private boolean global;

    private Type type;
    private String name;
    private int id;

    // 初始化为0
    private boolean zeroinit = false;

    // Reg用，分配寄存器编号
    // 没用，不是同一个对象。在register static里面记
//    private int no = -1;

    public Ident(int id) {
        this.isIdent = false;
        this.id = id;
    }

    public Ident(String name) {
        this.isIdent = true;
        this.name = name;
    }

    @Override
    public String toString() {
        if (global) {
            if (isIdent) {
                return "@" + name;
            }
            return "@" + String.valueOf(id);
        }
        if (isIdent) {
            return "%" + name;
        }
        return "%" + String.valueOf(id);
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public Type getType() {
        return type;
    }

    public boolean isIdent() {
        return isIdent;
    }

    public boolean isGlobal() {
        return global;
    }

    //frequently
    public void setType(Type type) {
        this.type = type;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setGlobal(boolean global) {
        this.global = global;
    }

    public void setZeroinit(boolean zeroinit) {
        this.zeroinit = zeroinit;
    }

    @Override
    public boolean equals(Object obj) {
        return this.toString().equals(obj.toString());
    }

    // Reg分配时的key
    public String getMapname() {
        if (this.global) {
            return this.name;
        }
        return String.valueOf(this.id);
    }
}
