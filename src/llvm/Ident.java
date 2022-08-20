package llvm;

import llvm.Type.Type;

import java.util.Objects;

public class Ident {

    private boolean isIdent;
    private boolean global;

    private Type type;
    private String name;
    private int id;
    private boolean isPointer = false; //是否为指针；
    // 初始化为0
    private boolean zeroinit = false;

    // 是否已经加载到寄存器，第一次后变为true
    private boolean load = false;

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

    public void setPointer(Boolean isPointer) {
        this.isPointer = isPointer;
    }

    public Boolean getIsPointer() {
        return isPointer;
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

    public boolean isLoad() {
        return load;
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

    public void setLoad(boolean load) {
        this.load = load;
    }

    @Override
    public boolean equals(Object obj) {
        return this.toString().equals(obj.toString());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.toString());
    }

    // Reg分配时的key
    public String getMapname() {
        if (this.global) {
            return this.name;
        }
        return String.valueOf(this.id);
    }

}
