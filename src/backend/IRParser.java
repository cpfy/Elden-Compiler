package backend;

import java.io.File;
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

    private ArrayList<IRCode> lirlist;
    private ArrayList<Instr> instrList;

    public IRParser(ArrayList<Token> tokenList) {
        this.index = 0;
        this.sym = tokenList.get(0);
        this.tokenList = tokenList;
        this.grammarList = new ArrayList<>();
//        this.erdp = new ErrorDisposal();
        this.table = new SymbolTable();

        this.instrList = new ArrayList<>();

        this.lirlist = new ArrayList<>();
    }

    //    外层使用的api
    public void start(int output) {
        CompUnit();
        if (output == 1) {
            try {
                writefile(OUTPUT_DIR);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
        while(symCodeIs("DECLARETK")){
            FunctionDecl();
        }
    }

    // FunctionDecl : "declare" MetadataAttachments OptExternLinkage FunctionHeader
    private void FunctionDecl() {
        match("declare");
        FuncHeader fh = FunctionHeader();
    }

    // GlobalDef: GlobalIdent "=" OptLinkage OptPreemptionSpecifier[Opt] Immutable Type Constant GlobalAttrs FuncAttrs
    // @buf = dso_local global [2 x [100 x i32]] zeroinitializer, align 4
    private void GlobalDef() {
        Ident g_idn = GlobalIdent();
        match("=");
        OptLinkage();
        OptPreemptionSpecifier();   // 可选dso_local
        OptUnnamedAddr();   // "unnamed_addr"
        Immutable();    // "constant"或"global"
        Type t = Type();
        Constant();     // zeroinitializer 可处理
        GlobalAttrs();  // 处理Align等
        FuncAttrs();


    }

    // GlobalAttrs: empty| "," GlobalAttrList;
    private void GlobalAttrs() {
        if (symIs(",")) {
            getsym();
            GlobalAttrList();
        }
    }

    // GlobalAttrList: GlobalAttr| GlobalAttrList "," GlobalAttr;
    private void GlobalAttrList() {
        GlobalAttr();
        while (symIs(",")) {
            GlobalAttr();
        }
    }

    // GlobalAttr: Section| Comdat| Alignment| MetadataAttachment;
    private void GlobalAttr() {
        //todo 只Alignment
        Alignment();
    }

    //    Alignment : "align" int_lit    ;
    private void Alignment() {
        match("align");
        getsym();
    }

    // OptPreemptionSpecifier: empty | PreemptionSpecifier;
    // PreemptionSpecifier: "dso_local" | "dso_preemptable";
    private void OptPreemptionSpecifier() {
        if (symIs("dso_local") || symIs("dso_preemptable")) {
            getsym();
        }
        //否则不用管
    }

    // Immutable	: "constant" | "global"
    private void Immutable() {
        if (symIs("constant") || symIs("global")) {
            getsym();
        } else {
            error();
        }
    }

    // GlobalIdent: global_ident
    private Ident GlobalIdent() {
        return global_ident();
    }

    // global_ident: _global_name | _global_id
    private Ident global_ident() {
        //此处应当必然name
        return _global_name();
    }

    //    _global_name: '@' ( _name | _quoted_name )
    private Ident _global_name() {
        match("@");
        String mydefname = sym.getTokenValue();
        getsym();
        Ident g_idn = new Ident(mydefname);
        return g_idn;
    }

    //直接空
    private void FuncAttrs() {
        if(symIs("#")){
            getsym();
            getsym();
        }
        return;
    }

    // FunctionDef: "define" OptLinkage FunctionHeader MetadataAttachments FunctionBody
    // example: define dso_local i32 @main() #0 {
    private void FunctionDef() {
        match("define");
//        match("dso_local");

        FuncHeader hd = FunctionHeader();
        FunctionBody();
    }

    // FunctionHeader: OptPreemptionSpecifier ReturnAttrs Type GlobalIdent "(" Params ")" FuncAttrs
    private FuncHeader FunctionHeader() {
        OptPreemptionSpecifier();
        ReturnAttrs();
        Type t = Type();
        Ident g_idn = GlobalIdent();
        match("(");
        ArrayList<Ident> paras = Params();
        match(")");
        FuncAttrs();
        FuncHeader funchd = new FuncHeader(g_idn, t, paras);
        return funchd;
    }

    //    ReturnAttrs
//	: empty
//	| ReturnAttrList
//    ;
//
//    ReturnAttrList
//	: ReturnAttr
//	| ReturnAttrList ReturnAttr
//            ;
//
//    ReturnAttr
//	: Alignment
//	| Dereferenceable
//	| StringLit
//	| "inreg"
//            | "noalias"
//            | "nonnull"
//            | "signext"
//            | "zeroext"
//    ;
    private void ReturnAttrs() {
        return;
    }

    // Params: empty| "..."| ParamList| ParamList "," "..."
    private ArrayList<Ident> Params() {
        ArrayList<Ident> ilist = new ArrayList<>();
        if (symIs(")")) {
            empty();
            return ilist;
        } else {
            return ParamList();
        }
    }

    // ParamList: Param	| ParamList "," Param
    private ArrayList<Ident> ParamList() {
        ArrayList<Ident> plist = new ArrayList<>();
        Ident p = Param();
        plist.add(p);
        while (symIs(",")) {
            getsym();
            p = Param();
            plist.add(p);
        }
        return plist;
    }

    // Param: Type ParamAttrs | Type ParamAttrs LocalIdent
    private Ident Param() {
        Type t = Type();
        ParamAttrs();
        Ident l_idn = LocalIdent();
        l_idn.setType(t);   // 设置type
        return l_idn;
    }

    // ParamAttrs : empty | ParamAttrList ;
    private void ParamAttrs() {
        if (nextisParamAttr(sym)) {
            ParamAttrList();
        } else {
            empty();
        }
    }

    // ParamAttrList : ParamAttr | ParamAttrList ParamAttr ;
    private void ParamAttrList() {
        ParamAttr();
        if (nextisParamAttr(sym)) {
            ParamAttr();
        }
    }

    //    ParamAttr
//	: Alignment
//	| Dereferenceable
//	| StringLit
//	| "byval"
//            | "inalloca"
//            | "inreg"
//            | "nest"
//            | "noalias"
//            | "nocapture"
//            | "nonnull"
//            | "readnone"
//            | "readonly"
//            | "returned"
//            | "signext"
//            | "sret"
//            | "swifterror"
//            | "swiftself"
//            | "writeonly"
//            | "zeroext"
//    ;
    private void ParamAttr() {
        return;
    }

    // 无任何用
    private void empty() {
        return;
    }

    // FunctionBody: "{" BasicBlockList UseListOrders "}"
    private void FunctionBody() {
        match("{");
        BasicBlockList();
        match("}");
    }

    // BasicBlockList: BasicBlock | BasicBlockList BasicBlock
    private void BasicBlockList() {
        //todo 不一定准确
        while (!symIs("}")) {
            BasicBlock();
        }
    }

    // BasicBlock: OptLabelIdent Instructions Terminator
    private void BasicBlock() {
        Instructions();
        Terminator();
    }

    // Instructions: empty | InstructionList
    private void Instructions() {
        if (newSymLine()) {
            return;
        }
        InstructionList();
    }

    //    InstructionList
//	: Instruction
//	| InstructionList Instruction
    private void InstructionList() {
        Instruction();
        while (matchInstr(sym)) {
            Instruction();
        }
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
        match("ret");
        if (symIs("void")) {
            getsym();
            return;
        }
        TypeC rettype = ConcreteType();

    }

    //    ConcreteType
//	: IntType
//    // Type ::= 'float' | 'void' (etc)
//	| FloatType
//    // Type ::= Type '*'
//    // Type ::= Type 'addrspace' '(' uint32 ')' '*'
//	| PointerType
//    // Type ::= '<' ... '>'
//	| VectorType
//	| LabelType
//    // Type ::= '[' ... ']'
//	| ArrayType
//    // Type ::= StructType
//	| StructType
//    // Type ::= %foo
//    // Type ::= %4
//	| NamedType
//	| MMXType
//	| TokenType
    private TypeC ConcreteType() {
        switch (sym.getTokenValue()) {
            case "void":
                return TypeC.V;
            case "int":
                return TypeC.I;
            case "float":
                return TypeC.F;
            default:
                return null;

        }
    }

    //    BrTerm	: "br" LabelType LocalIdent OptCommaSepMetadataAttachmentList
    private void BrTerm() {
        match("br");
        LabelType();
        Ident idn = LocalIdent();
    }

    private void LabelType() {
    }

    //
//    LocalIdent
//	: local_ident
    private Ident LocalIdent() {
        return local_ident();
    }

    //    local_ident
//	: _local_name
//	| _local_id
//    ;
//
//    _local_name
//	: '%' ( _name | _quoted_name )
//    ;
//
//    _local_id
//	: '%' _id
//    ;
    private Ident local_ident() {
        match("%");
        String value = sym.getTokenValue();

        if (Character.isDigit(value.charAt(0))) {
            // boolean isdigit = true;
            getsym();
            Ident idn = new Ident(Integer.parseInt(value));
            return idn;

        } else {
            // boolean isalpha = true;
            getsym();
            Ident idn = new Ident(value);
            return idn;
        }
    }

//    private void MainFuncDef() {
//
//    }


    private void LoadInst() {
        match("load");
        Type typ1 = Type();
        getsym();

        match(",");
        Type typ2 = Type();
    }

    // "store" OptVolatile Type Value "," Type Value
//    ex:store i32 0, i32* %1, align 4
    private void StoreInst() {
        match("store");
        Type typ1 = Type();
        getsym();
//        Value v = Value();

        match(",");
        Type typ2 = Type();
//        Value v = Value();
    }


    // Binary运算
    // 例如AddInst	: "add" OverflowFlags Type Value "," Value OptCommaSepMetadataAttachmentList
    // ex：%5 = add nsw i32 %3, %4
    private void ValueInstruction() {
        if(symIs("getelementptr")){
            GetElementPtrInst();
            return;
        }
        
        String instname = sym.getTokenValue();
        getsym();
        Type typeC = Type();
//        Value val = Value();
        Ident val = Value();
        match(",");
        Value();
    }

//    GetElementPtrInst
//	: "getelementptr" OptInBounds Type "," Type Value OptCommaSepMetadataAttachmentList
//	| "getelementptr" OptInBounds Type "," Type Value "," CommaSepTypeValueList OptCommaSepMetadataAttachmentList
    private void GetElementPtrInst() {
        match("getelementptr");
        OptInBounds();  // "inbounds"
        Type t1 = Type();
        match(",");
        Type t2 = Type();
        Value();
        if(symIs(",")){
            getsym();
            CommaSepTypeValueList();
        }
    }

    // CommaSepTypeValueList: TypeValue| CommaSepTypeValueList "," TypeValue    ;
    private void CommaSepTypeValueList() {
        TypeValue();
        while(symIs(",")){
            getsym();
            TypeValue();
        }
    }

    // TypeValue: Type Value
    private void TypeValue() {
        Type();
        Value();
    }


    //    Value
//	: Constant
//    // %42
//    // %foo
//	| LocalIdent
//	| InlineAsm
    private Ident Value() {
        if (symIs("%")) {
            return LocalIdent();
        }
        return Constant();
    }

    //    Constant
//	: BoolConst
//	| IntConst
//	| FloatConst
//	| NullConst
//	| NoneConst
//	| StructConst
//	| ArrayConst
//	| CharArrayConst
//	| VectorConst
//	| ZeroInitializerConst
//    // @42
//    // @foo
//	| GlobalIdent
//	| UndefConst
//	| BlockAddressConst
//	| ConstantExpr
    private Ident Constant() {
        String value = sym.getTokenValue();
        if(symIs("zeroinitializer")){
            match("zeroinitializer");   //	| ZeroInitializerConst
        }
        if (Character.isDigit(value.charAt(0))) {
            getsym();
            Ident idn = new Ident(Integer.parseInt(value));
            return idn;
        }
        return null;
    }

    // Type大类
    private Type Type() {
        TypeC ctype = null;
        if(symIs("[")){
            return ArrayType();
        }

        String typestr = sym.getTokenValue();


        switch (typestr) {
            case "void":
                ctype = TypeC.V;
                break;
            case "i32":
                ctype = TypeC.I;
                break;
            case "i32*":
                ctype = TypeC.IP;
                break;
            case "float":
                ctype = TypeC.F;
                break;
            case "float*":
                ctype = TypeC.FP;
                break;
            default:
                System.out.println("Error Type!");
                error();
                break;
        }
        getsym();
        Type rettype = new Type(ctype);
        return rettype;
    }

    // ArrayType : "[" int_lit "x" Type "]"
    private Type ArrayType() {
        match("[");
        int dim = Integer.parseInt(sym.getTokenValue());
        getsym();
        match("x");
        Type();         //todo
        match("]");

        Type retype = new Type(TypeC.A);
        return retype;
    }



    /***** 可选函数 *****/
//    OptLinkage
//	: empty
//	| Linkage
//    ;
//
//    Linkage
//	: "appending"
//            | "available_externally"
//            | "common"
//            | "internal"
//            | "linkonce"
//            | "linkonce_odr"
//            | "private"
//            | "weak"
//            | "weak_odr"
//    ;
    private void OptLinkage(){
        if(symIs("common")||symIs("private")){
            getsym();
        }
    }

    private void OptInBounds() {
        if(symIs("inbounds")){
            getsym();
        }
    }

//    OptUnnamedAddr
//	: empty
//	| UnnamedAddr
//    ;
//
//    UnnamedAddr
//	: "local_unnamed_addr"
//            | "unnamed_addr"
//    ;
    private void OptUnnamedAddr(){
        if(symIs("local_unnamed_addr")||symIs("unnamed_addr")){
            getsym();
        }
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

//    //    含判断sym
//    private void getsym(String str) {
////        扩展支持code+str
//        if (!symIs(str) && !symCodeIs(str)) {
//            System.err.println("Not Match!: " + str);
//        }
//
//        grammarList.add(sym.tostring());
//        if (index < tokenList.size() - 1) {
//            index += 1;
//            sym = tokenList.get(index);
//        } else {
//            //todo System.out.println("Token List to End.");
//        }
//    }

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
            error();
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

    //    判断是否新的行/文法结束
    private boolean newSymLine() {
        if (sym.getRow() > getLastToken().getRow()) {
            return true;
        }
        return false;
    }

    //    是否为Instruction中的几类
    private boolean matchInstr(Token sym) {
        String instr = sym.getTokenValue();
        if (symIs("@") || symIs("define") || symIs("}")) {
            return false;
        }

        return true;
    }

    //下一个是ParamAttr
    private boolean nextisParamAttr(Token sym) {
        return false;
    }

}
