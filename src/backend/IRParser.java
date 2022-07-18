package backend;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class IRParser {
    private Token sym;
    private int index;
    private final ArrayList<Token> tokenList;
    private ArrayList<String> grammarList;
    private SymbolTable table;

    //private Symbol curFunc = null;  //当前调用的函数
    private int funcParaIndex;      //对照参数是否匹配时的index
    private int curDimen = 0;           //当前变量数组维度
    private boolean isGlobal = true;    //是否为顶层

    private final String OUTPUT_DIR = "output.txt";

    public IRParser(ArrayList<Token> tokenList) {
        this.index = 0;
        this.sym = tokenList.get(0);
        this.tokenList = tokenList;
        this.grammarList = new ArrayList<>();
//        this.erdp = new ErrorDisposal();
        this.table = new SymbolTable();
    }

    public void start(int output, int erroutput) {
        CompUnit();
        if (output == 1) {
            try {
                writefile(OUTPUT_DIR);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
//        if (erroutput == 1) {
//            try {
//                erdp.writefile();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
    }


    public void writefile(String dir) throws IOException {
        File file = new File(dir);
        FileWriter writer = new FileWriter(file);
        for (Token t : tokenList) {
            writer.write(t.tostring() + "\n");
            System.out.println(t.tostring());
        }
        writer.flush();
        writer.close();
    }

    // 成分部分
    private void CompUnit() {
        while ((symCodeIs("CONSTTK") && symPeek("INTTK", 1) && symPeek("IDENFR", 2)) ||
                (symCodeIs("INTTK") && symPeek("IDENFR", 1) && symPeek("LBRACK", 2))
        ) {
            GlobalDecl();
        }
        while ((symCodeIs("VOIDTK") && symPeek("IDENFR", 1) && symPeek("LPARENT", 2)) ||
                (symCodeIs("INTTK") && symPeek("IDENFR", 1) && symPeek("LPARENT", 2))
        ) {
            FuncDef();
        }
        isGlobal = false;
        MainFuncDef();
        grammarList.add("<CompUnit>");
    }

    //    GlobalDecl
//	: GlobalIdent "=" ExternLinkage OptPreemptionSpecifier OptVisibility OptDLLStorageClass OptThreadLocal OptUnnamedAddr OptAddrSpace OptExternallyInitialized Immutable Type GlobalAttrs FuncAttrs
    private void GlobalDecl() {

        GlobalIdent();

    }

    //    GlobalIdent
//	: global_ident
    private void GlobalIdent() {
        global_ident();
    }

    //    global_ident
//	: _global_name
//	| _global_id
    private void global_ident() {
    }

    private void FuncDef() {

    }

    private void MainFuncDef() {

    }

    private void Instruction() {
        switch (sym.getTokenValue()) {
            case "store":
                StoreInst();
                break;
            case "load":
                LoadInst();
                break;
            default:
                ValueInstruction();
                break;
        }
    }

    private void LoadInst() {
        getsym("load");
    }

    // "store" OptVolatile Type Value "," Type Value
    private void StoreInst() {
        getsym("store");

    }

    private void ValueInstruction() {
        switch (sym.getTokenValue()) {
            case "add":
                AddInst();
                break;
            case "fadd":
                FAddInst();
                break;
            case "sub":
                SubInst();
                break;
            case "fsub":
                FSubInst();
                break;
            case "mul":
                MulInst();
                break;
            case "fmul":
                FMulInst();
                break;
            case "sdiv":
                SDivInst();
                break;
            case "fdiv":
                FDivInst();
                break;
            default:
                break;
        }
    }


    // Binary运算
//    AddInst	: "add" OverflowFlags Type Value "," Value OptCommaSepMetadataAttachmentList
    private void AddInst() {
        getsym("add");
        Type();
    }


    private void FAddInst() {
        getsym("fadd");
    }

    private void SubInst() {
        getsym("sub");
    }

    private void FSubInst() {
        getsym("fsub");
    }

    private void MulInst() {
        getsym("mul");
    }

    private void FMulInst() {
        getsym("fmul");
    }

    private void SDivInst() {
        getsym("sdiv");
    }

    private void FDivInst() {
        getsym("fdiv");
    }


    //    Type大类
    private void Type() {
    }


    //基础操作；辅助函数
    private void getsym() {
        grammarList.add(sym.tostring());
        if (index < tokenList.size() - 1) {
            index += 1;
            sym = tokenList.get(index);
        } else {
            //todo System.out.println("Token List to End.");
        }
    }

    //    含判断sym
    private void getsym(String str) {
        if (!symIs(str)) {
            System.err.println(str + ": Not Match!");
        }

        grammarList.add(sym.tostring());
        if (index < tokenList.size() - 1) {
            index += 1;
            sym = tokenList.get(index);
        } else {
            //todo System.out.println("Token List to End.");
        }
    }

    private void error() {
        System.err.println("Error!");
        System.out.println("目前输出：");
        for (String s : grammarList) {
            System.out.println(s);
        }
        System.out.println("Error at :" + sym.tostring());
        System.exit(0);
    }

    private void match(String s) {   //匹配字符string本身
        if (!symIs(s)) {
            switch (s) {
                case ";":
//                    erdp.ErrorAt(getLastToken(), ErrorDisposal.ER.ERR_I);
                    break;
                case ")":
//                    erdp.ErrorAt(getLastToken(), ErrorDisposal.ER.ERR_J);
                    break;
                case "]":
//                    erdp.ErrorAt(getLastToken(), ErrorDisposal.ER.ERR_K);
                    break;
                default:
                    error();
                    break;
            }
        } else {
            getsym();
        }
    }

    private void matchCode(String s) {   //匹配字符string本身
        if (!symCodeIs(s)) {
            error();
        } else {
            getsym();

        }
    }

    private boolean symIs(String s) {
        return sym.getTokenValue().equals(s);
    }

    private boolean symCodeIs(String s) {
        return sym.getTokenCode().equals(s);
    }

    private boolean symPeek(String s, int offset) {
        if (index + offset >= tokenList.size()) {
            return false;
        }
        Token newsym = tokenList.get(index + offset);
        return newsym.getTokenCode().equals(s);
    }

    private boolean assignPeek() {   //查找分号前有无等号
        int offset = 1;
        int curRow = tokenList.get(index).getRow();
        while (index + offset < tokenList.size()) {
            Token newsym = tokenList.get(index + offset);
            if (newsym.getTokenValue().equals(";") ||
                    newsym.getRow() > curRow) {
                break;
            } else if (newsym.getTokenValue().equals("=")) {
                return true;
            }
            offset += 1;
        }
        return false;
    }

    private Token getLastToken() {    //获取上一个 符
        if (index == 0) {
            return tokenList.get(0);
        }
        return tokenList.get(index - 1);
    }
}
