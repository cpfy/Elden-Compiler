package llvm;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Objects;

public class Value {

    private String type;    // 废弃，先堆屎山

    /* type所有种类如下（仅目前，可能还会增加）：

    [变量]: ident
    [整数]: int
    [float浮点]: float
    [16进制浮点]: hex

    */

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

    //float
    private boolean isfloat = false;
    private float f;

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

    // float
    public Value(float f) {
        this.isfloat = true;
        this.f = f;
    }

    @Override
    public String toString() {
        if (isIdent) {
            return ident.toString();

        } else if (isHex()) {
            return hexVal;

        } else if (isTClist) {
            // 好神奇，ArrayList自己能处理成带括号的
            return tclist.toString();

        } else if (isfloat) {
            return String.valueOf(this.f);
        }


        //todo 一些种类如zeroinitializer有隐患
        return String.valueOf(val);
    }

    public boolean isHex() {
        return hex;
    }

    public int getVal() {
        return val;
    }

//    public String getHexVal() {
//        return hexVal;
//    }

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

    public boolean isFloat() {
        return isfloat;
    }

    public float getF() {
        return f;
    }

    // 16进制转浮点
    // 见: https://stackoverflow.com/questions/1071904/how-to-convert-hex-string-to-float-in-java
    public float hexToFloat() {
        String hex = this.hexVal.substring(2, this.hexVal.length());   // 0x...去除首两字符
        Long i = Long.parseLong(hex, 16);
        Float f = Float.intBitsToFloat(i.intValue());
        return f;
    }

    public int hexToIntLow() {
        String hex = this.hexVal.substring(2, this.hexVal.length());   // 0x...去除首两字符
        int i = Integer.parseInt(hex, 16);
        i = i & 0xffff;
        return i;
    }

    public int hexToIntHigh() {
        String hex = this.hexVal.substring(2, this.hexVal.length());   // 0x...去除首两字符
        int i = Integer.parseInt(hex, 16);
        i = (i >> 16) & 0xffff;
        return i;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Value)) {
            return false;
        }
        Value v2 = (Value) obj;
        return v2.toString().equals(this.toString());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.toString());
    }

    // 当tclist为多维数组array时，返回拆解
    public ArrayList<Value> getTCValuePackage() {

        ArrayList<Value> pkg = new ArrayList<>();

        if (isTClist) {
            for (TypeValue tv : tclist) {
                ArrayList<Value> vpkg = tv.getValue().getTCValuePackage();
                pkg.addAll(vpkg);
            }

        } else {
            pkg.add(this);
        }

        return pkg;
    }
}
