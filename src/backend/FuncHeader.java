package backend;

import java.util.ArrayList;

public class FuncHeader {
//    文法定义
//    FunctionHeader
//	: OptPreemptionSpecifier OptVisibility OptDLLStorageClass OptCallingConv ReturnAttrs Type GlobalIdent "(" Params ")" OptUnnamedAddr FuncAttrs OptSection OptComdat OptGC OptPrefix OptPrologue OptPersonality

//    enum TYPE {
//        I, V, A, F  //integer, void, array, function
//    }

    private Symbol.TYPE type;
    private String fname;
    private ArrayList<Symbol> paras;

    public FuncHeader(String fname, Symbol.TYPE type, ArrayList<Symbol> paras) {
        this.type = type;
        this.fname = fname;
        this.paras = paras;
    }

    public Symbol.TYPE getType() {
        return type;
    }

    public String getFname() {
        return fname;
    }

    public ArrayList<Symbol> getParas() {
        return paras;
    }
}
