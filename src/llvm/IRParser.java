package llvm;

import backend.SymbolTable;
import llvm.Instr.AllocaInst;
import llvm.Instr.AssignInstr;
import llvm.Instr.BinaryInst;
import llvm.Instr.BrTerm;
import llvm.Instr.CallInst;
import llvm.Instr.CondBrTerm;
import llvm.Instr.GetElementPtrInst;
import llvm.Instr.IcmpInst;
import llvm.Instr.Instr;
import llvm.Instr.LoadInst;
import llvm.Instr.RetTerm;
import llvm.Instr.StoreInstr;
import llvm.Instr.ZExtInst;
import llvm.Type.ArrayType;
import llvm.Type.IntType;
import llvm.Type.PointerType;
import llvm.Type.Type;
import llvm.Type.TypeC;
import llvm.Type.VoidType;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class IRParser {
    private Token sym;
    private int index;
    private final ArrayList<Token> tokenList;
    private ArrayList<String> grammarList;
    private SymbolTable table;

    //private Symbol curFunc = null;  //当前调用的函数
    private int funcParaIndex;      //对照参数是否匹配时的index
    //    private int curDimen = 0;           //当前变量数组维度

    private boolean isGlobal = true;    //是否为顶层

    private final String OUTPUT_DIR = "output.txt";


    //
    private int blockcount = 1;     // 基本块计数
    private Block curBlock = null;  // 当前block
    private ArrayList<Block> allblocklist;
    private ArrayList<Function> allfunctionlist;

    //
    private Function curFunction = null;   // 当前function
    private static HashMap<String, Block> blockmap;    // label name -> Block

    public static Block searchBlockmapByLabel(String label) {
        return blockmap.get(label);
    }

    public IRParser(ArrayList<Token> tokenList) {
        this.index = 0;
        this.sym = tokenList.get(0);
        this.tokenList = tokenList;
        this.grammarList = new ArrayList<>();

        this.allblocklist = new ArrayList<>();
        this.allfunctionlist = new ArrayList<>();

        //
        this.blockmap = new HashMap<>();
    }


    // 外层使用的api
    public ArrayList<Block> parseBlock(int output) {
        CompUnit();
        if (output == 1) {
            try {
                writefile(OUTPUT_DIR);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return this.allblocklist;
    }

    public ArrayList<Function> parseFunc(int output) {
        CompUnit();
        if (output == 1) {
            try {
                writefile(OUTPUT_DIR);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return this.allfunctionlist;
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
        createFunction("GlobalContainer");
        createBlock("GlobalContainer");
        while (symCodeIs("AT")) {
            GlobalDef();
        }

        isGlobal = false;

        while (symCodeIs("DEFINETK")) {
            FunctionDef();
        }

        // 事实需要

//        while (symCodeIs("DECLARETK")) {
//            FunctionDecl();
//        }


        allblocklist.add(curBlock);  // 塞进去最后一个块
        blockmap.put(curBlock.getLabel(), curBlock);
//        allfunctionlist.add(curFunction);

//        if (curBlock != null) {
//            allblocklist.add(curBlock);  // 塞进去最后一个块
//            blockmap.put(curBlock.getLabel(), curBlock);
//        }
//        if (curFunction != null) {
//            allfunctionlist.add(curFunction);
//        }
//>>>>>>> Stashed changes
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

    // Alignment : "align" int_lit    ;
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

    // Immutable : "constant" | "global"
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
        g_idn.setGlobal(true);
        return g_idn;
    }

    //直接空
    private void FuncAttrs() {
        if (symIs("#")) {
            getsym();
            getsym();
        }
        return;
    }

    // FunctionDef: "define" OptLinkage FunctionHeader MetadataAttachments FunctionBody
    // example: define dso_local i32 @main() #0 {
    private void FunctionDef() {
        match("define");
        FuncHeader hd = FunctionHeader();   // dso_local可在内处理

//        createBlock(hd.getFname());     // 函数首个Block，以函数为名
        // 函数头自带一个label，不需要

        this.curFunction = new Function(hd);
        FunctionBody();
        this.allfunctionlist.add(this.curFunction);
        this.curFunction = null;
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
        FuncHeader funcheader = new FuncHeader(g_idn, t, paras);
        return funcheader;
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
        ArrayList<Ident> plist = new ArrayList<>();
        if (symIs(")")) {
            empty();
            return plist;
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
        Ident ident = LocalIdent();
        ident.setType(t);   // 设置type
        return ident;
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
        OptLabelIdent();

        // 与llvm文法不一致，允许跳Instruction
        if (symIs("ret") || symIs("br")) {
            Terminator();
        } else {
            Instructions();
            Terminator();
        }
    }

    //
//    OptLabelIdent
//	: empty
//	| LabelIdent
    private void OptLabelIdent() {
        if (symPeek("COLON", 1)) {
            LabelIdent();
        }
    }

    // LabelIdent : label_ident
    private void LabelIdent() {
        label_ident();
    }

    //    label_ident
//	: ( _letter | _decimal_digit ) { _letter | _decimal_digit } ':'
//            | _quoted_string ':'
    private void label_ident() {
        String labelstr = sym.getTokenValue();
        getsym();
        match(":");

        //创建Block
        createBlock(labelstr);
    }


    // Instructions: empty | InstructionList
    private void Instructions() {
//        if (!newSymLine()) {     // 判断是否新的行/文法结束
//            return;
//        }
        InstructionList();
    }

    // InstructionList : Instruction | InstructionList Instruction
    private void InstructionList() {
        Instruction();
        while (matchInstr(sym)) {   // 是否为Instruction中的几类
            Instruction();
        }
    }


    // Instruction : StoreInst [ | FenceInst | CmpXchgInst | AtomicRMWInst]
//	| LocalIdent "=" ValueInstruction | ValueInstruction
    private void Instruction() {
        if (symIs("store")) {
            Instr i = StoreInst();    // curBlock.addInstr(vi)函数内已完成
            curBlock.addInstr(i);

        } else if (symIs("%")) {
            Ident li = LocalIdent();
            match("=");
            Instr vi = ValueInstruction();

            Instr asi = new AssignInstr("assign", li, vi);
            curBlock.addInstr(asi);

        } else {
            Instr vi = ValueInstruction();
            curBlock.addInstr(vi);
        }
    }

    // "store" OptVolatile Type Value "," Type Value
    // ex:store i32 0, i32* %1, align 4
    private Instr StoreInst() {
        match("store");
        Type t1 = Type();
        Value v1 = Value();
        match(",");
        Type t2 = Type();
        Value v2 = Value();

        // 创建
        Instr instr = new StoreInstr("store", t1, t2, v1, v2);
        return instr;
    }


    //    ValueInstruction
//    // Binary instructions
//	: AddInst
//	| FAddInst
//	| SubInst
//	| FSubInst
//	| MulInst
//	| FMulInst
//	| UDivInst
//	| SDivInst
//	| FDivInst
//	| URemInst
//	| SRemInst
//	| FRemInst
//    // Bitwise instructions
//	| ShlInst
//	| LShrInst
//	| AShrInst
//	| AndInst
//	| OrInst
//	| XorInst
//    // Vector instructions
//	| ExtractElementInst
//	| InsertElementInst
//	| ShuffleVectorInst
//    // Aggregate instructions
//	| ExtractValueInst
//	| InsertValueInst
//    // Memory instructions
//	| AllocaInst
//	| LoadInst
//	| GetElementPtrInst
//    // Conversion instructions
//	| TruncInst
//	| ZExtInst
//	| SExtInst
//	| FPTruncInst
//	| FPExtInst
//	| FPToUIInst
//	| FPToSIInst
//	| UIToFPInst
//	| SIToFPInst
//	| PtrToIntInst
//	| IntToPtrInst
//	| BitCastInst
//	| AddrSpaceCastInst
//    // Other instructions
//	| ICmpInst
//	| FCmpInst
//	| PhiInst
//	| SelectInst
//	| CallInst
//	| VAArgInst
//	| LandingPadInst
//	| CatchPadInst
//	| CleanupPadInst
    private Instr ValueInstruction() {
        String str = sym.getTokenValue();
        switch (str) {
            case "getelementptr":
                return GetElementPtrInst();
            case "call":
                return CallInst();
            case "icmp":
                return ICmpInst();
            case "alloca":
                return AllocaInst();
            case "load":
                return LoadInst();
            case "zext":
                return ZExtInst();
            default:
                return BinaryInst();
        }
    }

    // ZExtInst : "zext" Type Value "to" Type OptCommaSepMetadataAttachmentList
    private Instr ZExtInst() {
        match("zext");
        Type t1 = Type();
        Value v = Value();
        match("to");
        Type t2 = Type();

        Instr zei = new ZExtInst("zext", t1, t2, v);
        return zei;
    }

    // LoadInst
//	: "load" OptVolatile Type "," Type Value OptCommaSepMetadataAttachmentList
//	| "load" OptVolatile Type "," Type Value "," Alignment OptCommaSepMetadataAttachmentList
//	// Atomic load.
//	| "load" "atomic" OptVolatile Type "," Type Value OptSyncScope AtomicOrdering OptCommaSepMetadataAttachmentList
//	| "load" "atomic" OptVolatile Type "," Type Value OptSyncScope AtomicOrdering "," Alignment OptCommaSepMetadataAttachmentList
    private Instr LoadInst() {
        match("load");
        Type t1 = Type();
        match(",");
        Type t2 = Type();
        Value v = Value();

        Instr li = new LoadInst("load", t1, t2, v);
        return li;
    }

    //    AllocaInst
//	: "alloca" OptInAlloca OptSwiftError Type OptCommaSepMetadataAttachmentList
//	| "alloca" OptInAlloca OptSwiftError Type "," Alignment OptCommaSepMetadataAttachmentList
//	| "alloca" OptInAlloca OptSwiftError Type "," Type Value OptCommaSepMetadataAttachmentList
//	| "alloca" OptInAlloca OptSwiftError Type "," Type Value "," Alignment OptCommaSepMetadataAttachmentList
//	| "alloca" OptInAlloca OptSwiftError Type "," AddrSpace OptCommaSepMetadataAttachmentList
//	| "alloca" OptInAlloca OptSwiftError Type "," Alignment "," AddrSpace OptCommaSepMetadataAttachmentList
//	| "alloca" OptInAlloca OptSwiftError Type "," Type Value "," AddrSpace OptCommaSepMetadataAttachmentList
//	| "alloca" OptInAlloca OptSwiftError Type "," Type Value "," Alignment "," AddrSpace OptCommaSepMetadataAttachmentList
//            ;
    private Instr AllocaInst() {
        match("alloca");
        Type t = Type();

        Instr ai = new AllocaInst("alloca", t);
        return ai;
    }

    // Binary运算
    // 例如AddInst	: "add" OverflowFlags Type Value "," Value [OptCommaSepMetadataAttachmentList]
    // ex：%5 = add nsw i32 %3, %4
    private Instr BinaryInst() {
        String instrname = sym.getTokenValue();
        getsym();
        Type t = Type();
        Value v1 = Value();
        match(",");
        Value v2 = Value();

        Instr bi = new BinaryInst("binary", instrname, t, v1, v2);
        return bi;
    }

    // ICmpInst : "icmp" IPred Type Value "," Value OptCommaSepMetadataAttachmentList
    private Instr ICmpInst() {
        match("icmp");
        String ipred = IPred();
        Type t = Type();
        Value v1 = Value();
        match(",");
        Value v2 = Value();

        Instr icmpi = new IcmpInst("icmp", ipred, t, v1, v2);
        return icmpi;
    }

    private String IPred() {
        String p = sym.getTokenValue();
        getsym();
        return p;
    }

    //    CallInst : OptTail "call" FastMathFlags OptCallingConv ReturnAttrs Type Value "(" Args ")" FuncAttrs OperandBundles OptCommaSepMetadataAttachmentList
    // call void @putarray(i32 %6, i32* %8)
    private Instr CallInst() {
        match("call");
        ReturnAttrs();
        Type t = Type();
        Value v = Value();  // 如@putarray
        match("(");
        ArrayList<TypeValue> args = Args();
        match(")");
        FuncAttrs();    // 杂鱼处理

        Instr instr = new CallInst("call", t, v.getIdent(), args);
        return instr;
    }

    // 函数的参数
//    Args
//	: empty
//	| "..."
//            | ArgList
//	| ArgList "," "..."
    private ArrayList<TypeValue> Args() {
        if (isConcreteType(sym)) {
            return ArgList();
        }
        return new ArrayList<>();
    }


    //    ArgList
//	: Arg
//	| ArgList "," Arg
//    ;
    private ArrayList<TypeValue> ArgList() {
        ArrayList<TypeValue> arglist = new ArrayList<>();
        TypeValue arg = Arg();
        arglist.add(arg);
        while (symIs(",")) {
            getsym();
            arg = Arg();
            arglist.add(arg);
        }
        return arglist;
    }

    //    Arg
//	: ConcreteType ParamAttrs Value
//	| MetadataType Metadata
    private TypeValue Arg() {
        Type t = ConcreteType();
        ParamAttrs();
        Value v = Value();
        return new TypeValue(t, v);     // Type+Value组合
    }


    //    Terminator
//	: RetTerm
//	| BrTerm
//	| CondBrTerm
//	| SwitchTerm
//	| IndirectBrTerm
//	| InvokeTerm
//	| ResumeTerm
//	| CatchSwitchTerm
//	| CatchRetTerm
//	| CleanupRetTerm
//	| UnreachableTerm
    private void Terminator() {
        Instr term;
        switch (sym.getTokenValue()) {
            case "ret":
                term = RetTerm();
                curBlock.addInstr(term);

                //block插入function
                curFunction.addBlock(curBlock);
                break;
            case "br":
                if (symPeek("LABELTK", 1)) {
                    term = BrTerm();

                } else {
                    term = CondBrTerm();
                }
                curBlock.addInstr(term);

                //block插入function
                curFunction.addBlock(curBlock);
                break;
            default:
                //todo
                error();
                break;
        }

    }

    //    RetTer	: "ret" VoidType OptCommaSepMetadataAttachmentList
    //	| "ret" ConcreteType Value OptCommaSepMetadataAttachmentList
    private Instr RetTerm() {
        match("ret");
        if (symIs("void")) {
            getsym();
            Instr i = new RetTerm("ret", new Type(TypeC.V));
            return i;

        } else {
            Type retype = ConcreteType();
            Value v = Value();

            Instr i = new RetTerm("ret", retype, v);
            return i;
        }
    }

    // ConcreteType
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
    private Type ConcreteType() {
        String v = sym.getTokenValue();
        if (assignPeek("*", ",", "%", "@")) {
            return PointerType();
        }
        switch (v) {
            //todo 补充完整
            case "[":
                return ArrayType();
            case "i":
//                if (assignPeek("*", ",")) {
//                    return PointerType();
//                }
                return IntType();
            case "float":
                getsym();
                return new Type(TypeC.F);
            default:
                error();
                return null;
        }
    }

    // BrTerm	: "br" LabelType LocalIdent OptCommaSepMetadataAttachmentList
    private Instr BrTerm() {
        match("br");
        LabelType();
        Ident ident = LocalIdent();

        Instr bti = new BrTerm("br", ident);
        return bti;
    }

    // CondBrTerm "br" IntType Value "," LabelType LocalIdent "," LabelType LocalIdent OptCommaSepMetadataAttachmentList ;
    private Instr CondBrTerm() {
        match("br");
        Type it = IntType();
        Value v = Value();
        match(",");
        LabelType();
        Ident l1 = LocalIdent();
        match(",");
        LabelType();
        Ident l2 = LocalIdent();

        Instr i = new CondBrTerm("condbr", (IntType) it, v, l1, l2);
        return i;
    }

    // IntType: int_type
    // int_type: 'i' _decimals
    private Type IntType() {
        getsym();
        int decimal = Integer.parseInt(sym.getTokenValue());
        getsym();
        return new IntType(TypeC.I, decimal);
    }

    // LabelType : "label"
    private void LabelType() {
        match("label");
    }

    // LocalIdent : local_ident
    private Ident LocalIdent() {
        return local_ident();
    }

    // local_ident : _local_name | _local_id ;
    // _local_name : '%' ( _name | _quoted_name ) ;
    // _local_id : '%' _id ;
    private Ident local_ident() {
        match("%");
        String value = sym.getTokenValue();

        if (Character.isDigit(value.charAt(0))) {
            // boolean isdigit = true;
            getsym();
            Ident idn = new Ident(Integer.parseInt(value));
            idn.setGlobal(false);
            return idn;

        } else {
            // boolean isalpha = true;
            getsym();
            Ident idn = new Ident(value);
            idn.setGlobal(false);
            return idn;
        }
    }

    //    GetElementPtrInst
//	: "getelementptr" OptInBounds Type "," Type Value OptCommaSepMetadataAttachmentList
//	| "getelementptr" OptInBounds Type "," Type Value "," CommaSepTypeValueList OptCommaSepMetadataAttachmentList
    private Instr GetElementPtrInst() {
        match("getelementptr");
        OptInBounds();  // "inbounds"
        Type t1 = Type();
        match(",");
        Type t2 = Type();
        Value v = Value();
        if (symIs(",")) {
            getsym();
            CommaSepTypeValueList();
        }

        Instr gepi = new GetElementPtrInst("getelementptr", t1, t2, v);
        return gepi;
    }

    // CommaSepTypeValueList: TypeValue| CommaSepTypeValueList "," TypeValue    ;
    private void CommaSepTypeValueList() {
        TypeValue();
        while (symIs(",")) {
            getsym();
            TypeValue();
        }
    }

    // TypeValue: Type Value
    private void TypeValue() {
        Type();
        Value();
    }


    // Value: Constant| LocalIdent| InlineAsm
    private Value Value() {
        if (symIs("%")) {
            return new Value(LocalIdent());
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
    private Value Constant() {
        String value = sym.getTokenValue();
        if (symIs("zeroinitializer")) {
            match("zeroinitializer");   //	| ZeroInitializerConst
        }
        if (Character.isDigit(value.charAt(0))) {
            getsym();
            //todo
//            idn.setGlobal()
            return new Value(value);
        }
        // @foo GlobalIdent
        else if (symIs("@")) {
            Ident gi = GlobalIdent();
            return new Value(gi);
        }

        //todo

        error();
        return null;
    }

    // Type大类
//    Type
//	: VoidType
//	| FuncType
//	| FirstClassType
    private Type Type() {
        String typestr = sym.getTokenValue();
        if (symIs("void")) {
            getsym();
            return new VoidType(TypeC.V);
        }
        return FirstClassType();
    }

    // FirstClassType : ConcreteType | MetadataType
    private Type FirstClassType() {
        return ConcreteType();
    }

    // PointerType : Type OptAddrSpace "*"
    // todo 假装i_pointer
    private Type PointerType() {
//        getsym();
//        int decimal = Integer.parseInt(sym.getTokenValue());
//        getsym();
//        Type t = new IntType(TypeC.I, decimal);

        Type t = Type();

        match("*");

        return new PointerType(TypeC.IP, t);
    }

    // ArrayType : "[" int_lit "x" Type "]"
    private Type ArrayType() {
        match("[");

        int dim = Integer.parseInt(sym.getTokenValue());
        getsym();
        match("x");
        Type t = Type();         //todo
        match("]");

        Type retype = new ArrayType(TypeC.A, dim, t);
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
    private void OptLinkage() {
        if (symIs("common") || symIs("private")) {
            getsym();
        }
    }

    private void OptInBounds() {
        if (symIs("inbounds")) {
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
    private void OptUnnamedAddr() {
        if (symIs("local_unnamed_addr") || symIs("unnamed_addr")) {
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

    private boolean assignPeek(String s, String end, String end2, String end3) {   // 查找本行结束前/end前是否有某个符号
        int offset = 1;
        int curRow = tokenList.get(index).getRow();
        while (index + offset < tokenList.size()) {
            Token newsym = tokenList.get(index + offset);
            if (newsym.getTokenValue().equals(end) ||
                    newsym.getTokenValue().equals(end2) ||
                    newsym.getTokenValue().equals(end3) ||
                    newsym.getRow() > curRow) {
                break;
            } else if (newsym.getTokenValue().equals(s)) {
                if (!newsym.isPointerused()) {
                    newsym.setPointerused(true);
                    return true;
                } else {
                    break;  // *已经用过，必然超过
                }
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

    // 判断是否新的行/文法结束
    private boolean newSymLine() {
        if (sym.getRow() > getLastToken().getRow()) {
            return true;
        }
        return false;
    }

    // 是否为Instruction中的几类
    private boolean matchInstr(Token sym) {
        String instr = sym.getTokenValue();
        if (symIs("@") || symIs("define") || symIs("}") || symIs("br") || symIs("ret")) {
            return false;
        }

        return true;
    }

    //下一个是ParamAttr
    private boolean nextisParamAttr(Token sym) {
//        if(symPeek("COMMA",1)||symPeek("RIGHT",1)){
//            return
//        }
        return false;
    }

    //创建container函数块
    private Function createFunction(String fname) {
        FuncHeader fh = new FuncHeader(fname, new Type(TypeC.V), new ArrayList<>());

        Function nf = new Function(fh);
        allfunctionlist.add(nf);
        curFunction = nf;

        return nf;
    }

    //创建基本块
    private Block createBlock(String labelstr) {
        if (curBlock != null) {
            allblocklist.add(curBlock);
            blockmap.put(curBlock.getLabel(), curBlock);
            if (curFunction != null) {
                //curFunction.addBlock(curBlock);
            }
        }

        Block nb = new Block();
        nb.setNum(blockcount);
        nb.setLabel(labelstr);
        curBlock = nb;

        return nb;
    }

    // 是否属于ConcreteType
    private boolean isConcreteType(Token sym) {
        String t = sym.getTokenValue();
        if (t.equals("i") || t.equals("float") || t.equals("void") || t.equals("[")) {
            return true;
        }
        return false;
    }

    /* ########################## */
    public void printllvmOutputs() {
        System.out.println("【LLVM Print Start.】");
        for (Function function : allfunctionlist) {

            boolean needprintfunc = !function.getFuncheader().getFname().equals("GlobalContainer");
            if (needprintfunc) {
                System.out.println(function.toString() + "{");
            }

            for (Block block : function.getBlocklist()) {
                System.out.println(block.getLabel() + ":");
                for (Instr instr : block.getInblocklist()) {
                    System.out.println(instr.toString());
                }
            }
            if (needprintfunc) {
                System.out.println("}");
            }

        }
        System.out.println("【LLVM Print End.】");
    }

}
