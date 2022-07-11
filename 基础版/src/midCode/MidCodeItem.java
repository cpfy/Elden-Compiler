package midCode;

public class MidCodeItem {
    private MidCodeType type;
    private String xx;
    private String yy;
    private String zz;

    public MidCodeItem(MidCodeType type, String xx, String yy, String zz) {
        this.type = type;
        this.xx = xx;
        this.yy = yy;
        this.zz = zz;
    }

    @Override
    public String toString() {
        String s = "";
        if (type == MidCodeType.ASSIGNOP) {
            s = zz + " = " + xx;
        }
        else if (type == MidCodeType.PLUSOP) {
            s = zz + " = " + xx + " + " + yy;
        }
        else if (type == MidCodeType.MINUOP) {
            s = zz + " = " + xx + " - " + yy;
        }
        else if (type == MidCodeType.MULTOP) {
            s = zz + " = " + xx + " * " + yy;
        }
        else if (type == MidCodeType.DIVOP) {
            s = zz + " = " + xx + " / " + yy;
        }
        else if (type == MidCodeType.MODOP) {
            s = zz + " = " + xx + " % " + yy;
        }
        else if (type == MidCodeType.NEQOP) {
            s = zz + " = " + xx + " != " + yy;
        }
        else if (type == MidCodeType.EQLOP) {
            s = zz + " = " + xx + " == " + yy;
        }
        else if (type == MidCodeType.LEQOP) {
            s = zz + " = " + xx + " <= " + yy;
        }
        else if (type == MidCodeType.LSSOP) {
            s = zz + " = " + xx + " < " + yy;
        }
        else if (type == MidCodeType.GEQOP) {
            s = zz + " = " + xx + " >= " + yy;
        }
        else if (type == MidCodeType.GREOP) {
            s = zz + " = " + xx + " > " + yy;
        }
        else if (type == MidCodeType.CONST) {
            s = "CONST " + zz;
            if (xx != null) {
                s += " = " + xx;
            }
        }
        else if (type == MidCodeType.VAR) {
            s = "VAR " + zz;
            if (xx != null) {
                s += " = " + xx;
            }
        }
        else if (type == MidCodeType.ARRAY) {
            s = "ARRAY " + zz;
            if (yy == null) {  //一维数组
                s += "[" + xx + "]";
            }
            else {
                s += "[" + xx + "][" + yy + "]";
            }
        }
        else if (type == MidCodeType.FUNC) {
            s = "\nFUNC " + xx + " " + zz + "()";
        }
        else if (type == MidCodeType.PARAM) {
            if (xx.equals("0")) {
                s = "PARAM " + zz;
            }
            else  if (xx.equals("1")) {
                s = "PARAM " + zz + "[]";
            }
            else if (xx.equals("2")) {
                s = "PARAM " + zz + "[][" + yy + "]";
            }
        }
        else if (type == MidCodeType.PUTARRAY) {
            s = zz + "[" + yy + "] = " + xx;
        }
        else if (type == MidCodeType.GETARRAY) {
            s = zz + " = " + yy + "[" + xx + "]";
        }
        else if (type == MidCodeType.SCAN) {
            s = zz + " = getint()";
        }
        else if (type == MidCodeType.LABEL) {
            s = "\t<LABLE" + xx + " " + yy + ">";
        }
        else if (type == MidCodeType.JUMP) {
            s = "\t\t<JUMPDST " + zz + ">";
        }
        else if (type == MidCodeType.BZ) {
            s = "if " + xx + " == 0 then goto " + zz;
        }
        else {
            s = type.name();
            if (zz != null) {
                s += " " + zz;
            }
            if (yy != null) {
                s += " " + yy;
            }
            if (xx != null) {
                s += " = " + xx;
            }
        }
        return s;
    }

    public MidCodeType getType() {
        return type;
    }

    public String getXx() {
        return xx;
    }

    public String getYy() {
        return yy;
    }

    public String getZz() {
        return zz;
    }
}
