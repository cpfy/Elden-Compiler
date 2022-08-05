package llvm;

import llvm.Type.Type;

import java.util.ArrayList;

public class FuncHeader {
    //    文法定义
    //    FunctionHeader
    //	: [Opt] ReturnAttrs Type GlobalIdent "(" Params ")" [Opt] FuncAttrs [Opt]

    private Type type;
    private String fname;
    //    private Ident idn;
    private ArrayList<Ident> paras;

    public FuncHeader(String fname, Type type, ArrayList<Ident> paras) {
        this.type = type;
        this.fname = fname;
        this.paras = paras;
    }

    public FuncHeader(Ident idn, Type type, ArrayList<Ident> paras) {
        this.type = type;
//        this.idn = idn;
        this.fname = idn.getName();

        this.paras = paras;
    }

    @Override
    public String toString() {
        String parastr = "";
        for (Ident i : paras) {
            parastr += i.toString();
            parastr += ", ";
        }
        if (!parastr.isEmpty()) {
            parastr = parastr.substring(0, parastr.length() - 2);
        }
        return type.toString() + " @" + fname + "(" + parastr + ")";
    }

    public Type getType() {
        return type;
    }

    public String getFname() {
        return fname;
    }

    public ArrayList<Ident> getParas() {
        return paras;
    }
}
