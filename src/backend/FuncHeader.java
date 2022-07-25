package backend;

import java.util.ArrayList;

public class FuncHeader {
    //    文法定义
    //    FunctionHeader
    //	: [Opt] ReturnAttrs Type GlobalIdent "(" Params ")" [Opt] FuncAttrs [Opt]

    private Type type;
    private String fname;
    private Ident idn;
    private ArrayList<Ident> paras;

    public FuncHeader(String fname, Type type, ArrayList<Ident> paras) {
        this.type = type;
        this.fname = fname;
        this.paras = paras;
    }

    public FuncHeader(Ident idn, Type type, ArrayList<Ident> paras) {
        this.type = type;
        this.idn = idn;
        this.paras = paras;
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
