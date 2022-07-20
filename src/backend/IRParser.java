package backend;

import sun.jvm.hotspot.debugger.cdbg.Sym;

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
        while (symCodeIs("AT")) {
            GlobalDef();
        }
        while (symCodeIs("DEFINETK")) {
            FunctionDef();
        }
        isGlobal = false;
        MainFuncDef();
        grammarList.add("<CompUnit>");
    }

    //    GlobalDef
//	: GlobalIdent "=" OptLinkage OptPreemptionSpecifier OptVisibility OptDLLStorageClass OptThreadLocal OptUnnamedAddr OptAddrSpace OptExternallyInitialized Immutable Type Constant GlobalAttrs FuncAttrs
//    exa:@a = dso_local global i32 3, align 4
    private void GlobalDef() {
        getsym("@");
        String name = GlobalIdent();
        if (symIs("dso_local")) {
            getsym();
        }
        getsym("global");
        Symbol.TYPE sybtype = Type();

    }

    //    GlobalIdent
    //	: global_ident
    private String GlobalIdent() {
        return global_ident();
    }

    //    global_ident
    //	: _global_name
    //	| _global_id
    private String global_ident() {
        //此处应当必然name
        return _global_name();
    }

    //    _global_name
    //	: '@' ( _name | _quoted_name )
    private String _global_name() {
        getsym("@");
        String mydefname = sym.getTokenValue();
        getsym();
        return mydefname;
    }

    // FunctionDef: "define" OptLinkage FunctionHeader MetadataAttachments FunctionBody
    // ema:define dso_local i32 @main() #0 {
    private void FunctionDef() {
        getsym("define");
        if (symIs("dso_local")) {
            getsym();
        }

        FuncHeader hd = FunctionHeader();
        FunctionBody();
    }

    //    FunctionHeader: ReturnAttrs Type GlobalIdent "(" Params ")" FuncAttrs
    private FuncHeader FunctionHeader() {
        Symbol.TYPE typ = Type();
        String fname = GlobalIdent();
        getsym("(");
        ArrayList<Symbol> paras = Params();
        getsym(")");
        FuncHeader funchd = new FuncHeader(fname, typ, paras);
        return funchd;
    }

//    Params
//	: empty
//	| "..."
//            | ParamList
//	| ParamList "," "..."

    private ArrayList<Symbol> Params() {
        if (!symIs(")")) {
            empty();
            return null;
        }
        return ParamList();
    }


    //    ParamList	: Param	| ParamList "," Param
    private ArrayList<Symbol> ParamList() {
        ArrayList<Symbol> plist = new ArrayList<>();
        Param();
        while (symIs(",")) {
            getsym();
            Symbol p = Param();
        }
        return plist;
    }

    //    Param
//	: Type ParamAttrs
//	| Type ParamAttrs LocalIdent
    private Symbol Param() {
        return null;
    }

    // 无任何用
    private void empty() {

    }

    //    FunctionBody: "{" BasicBlockList UseListOrders "}"
    private void FunctionBody() {
        getsym("{");
        BasicBlockList();
        getsym("}");
    }

    //    BasicBlockList
//	: BasicBlock
//	| BasicBlockList BasicBlock
    private void BasicBlockList() {
        //todo 不一定准确
        while (!symIs("}")) {
            BasicBlock();
        }

    }

    //    BasicBlock: OptLabelIdent Instructions Terminator
    private void BasicBlock() {
        Instructions();
        Terminator();
    }

    private void Instructions() {
    }

    private void Terminator() {
        switch (sym.getTokenValue()) {
            case "ret":
                RetTerm();
                break;
            case "br":
                BrTerm();
                break;
            default:
                //todo
                break;
        }
    }

    //    RetTer	: "ret" VoidType OptCommaSepMetadataAttachmentList
//	| "ret" ConcreteType Value OptCommaSepMetadataAttachmentList
    private void RetTerm() {
        getsym("ret");
    }

    private void BrTerm() {
        getsym("br");
        LabelType();
        LocalIdent();
    }

    private void LabelType() {
    }

    private void LocalIdent() {
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
        Symbol.TYPE typ1 = Type();
//        Value v = Value();

        getsym(",");
        Symbol.TYPE typ2 = Type();
//        Value v = Value();

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
        Value();
        getsym(",");
        Value();
    }

    private void Value() {
    }


    private void FAddInst() {
        getsym("fadd");
        Type();
        Value();
        getsym(",");
        Value();
    }

    private void SubInst() {
        getsym("sub");
        Type();
        Value();
        getsym(",");
        Value();
    }

    private void FSubInst() {
        getsym("fsub");
        Type();
        Value();
        getsym(",");
        Value();
    }

    private void MulInst() {
        getsym("mul");
        Type();
        Value();
        getsym(",");
        Value();
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
    private Symbol.TYPE Type() {
        switch (sym.getTokenValue()) {
            case "i32":
                return Symbol.TYPE.I;
            case "float":
                return Symbol.TYPE.F;
            default:
                System.err.println("Error Type!");
                return null;
        }
        //return null;
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
//        扩展支持code+str
        if (!symIs(str) && !symCodeIs(str)) {
            System.err.println("Not Match!: " + str);
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
