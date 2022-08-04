package llvm;

public class Value {

    private boolean hex;    // 10 or 16进制
    private int val;
    private String hexVal;

    private boolean isIdent = false;  // 是否ident
    private Ident ident;

    public Value(String str) {
        if (str.charAt(0) == '0') {
            this.hex = true;
            this.hexVal = str;
        } else {
            this.hex = false;
            this.val = Integer.parseInt(str);
        }

    }

    @Override
    public String toString() {
        if(isIdent){
            return ident.toString();
        }
        return String.valueOf(val);
    }

    public Value(Ident ident){
        this.isIdent = true;
        this.ident = ident;
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
}
