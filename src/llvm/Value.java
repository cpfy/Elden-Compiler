package llvm;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class Value {

    private boolean hex;    // 10 or 16进制
    private int val;
    private String hexVal;

    private boolean isIdent = false;  // 是否ident
    private Ident ident;

    // Zeroinitailizer
    private boolean keys = false;

    // TypeConst 列表，数组初始化用
    private boolean isTClist = false;
    private ArrayList<TypeValue> tclist;

    // Zeroinitailizer 用
    public Value() {
        this.keys = true;
    }

    public Value(String str) {
        if (str.length() == 1) {
            this.hex = false;
            this.val = Integer.parseInt(str);
        } else if (str.charAt(0) == '0') {
            this.hex = true;
            this.hexVal = str;
        } else {
            this.hex = false;
            this.val = Integer.parseInt(str);
        }
    }

    public Value(Ident ident) {
        this.isIdent = true;
        this.ident = ident;
    }

    public Value(int val) {
        this.hex = false;
        this.val = val;
    }

    public Value(ArrayList<TypeValue> tclist) {
        this.tclist = tclist;
        this.isTClist = true;
    }

    @Override
    public String toString() {
        if (isIdent) {
            return ident.toString();
        }
        else if (isHex()) {
            return hexVal;
        }
        return String.valueOf(val);
    }

    public boolean isHex() {
        return hex;
    }

    public int getVal() {
        return val;
    }

    public String getHexVal() {
        return hexVal;
    }

    public void setHex(boolean hex) {
        this.hex = hex;
    }

    public void setHexVal(String hexVal) {
        this.hexVal = hexVal;
    }

    public void setVal(int val) {
        this.val = val;
    }

    public boolean isIdent() {
        return isIdent;
    }

    public Ident getIdent() {
        return ident;
    }

    public boolean isKeys() {
        return keys;
    }

    public boolean isTClist() {
        return isTClist;
    }

    public ArrayList<TypeValue> getTclist() {
        return tclist;
    }

    // 16进制转浮点
    // 见: https://stackoverflow.com/questions/1071904/how-to-convert-hex-string-to-float-in-java
    public float hexToFloat() {
        String hex = this.hexVal.substring(2, this.hexVal.length());   // 0x...去除首两字符
        Long i = Long.parseLong(hex, 16);
        Float f = Float.intBitsToFloat(i.intValue());
        return f;
    }
}
