package backend;

import llvm.Block;
import llvm.Function;
import llvm.Ident;
import llvm.Instr.AllocaInst;
import llvm.Instr.AssignInstr;
import llvm.Instr.BinaryInst;
import llvm.Instr.BrTerm;
import llvm.Instr.CallInst;
import llvm.Instr.CondBrTerm;
import llvm.Instr.GetElementPtrInst;
import llvm.Instr.GlobalDefInst;
import llvm.Instr.IcmpInst;
import llvm.Instr.Instr;
import llvm.Instr.LoadInst;
import llvm.Instr.RetTerm;
import llvm.Instr.StoreInstr;
import llvm.Instr.ZExtInst;
import llvm.Type.IntType;
import llvm.Type.Type;
import llvm.Type.TypeC;
import llvm.TypeValue;
import llvm.Value;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import static java.lang.System.exit;
import static java.lang.System.in;

public class ArmGenerator {
    private ArrayList<Function> aflist;
    private ArrayList<String> armlist;
    private Register register;
    private HashMap<IRCode, String> printstrMap;
    private static String OUTPUT_DIR;
    private ArrayList<Instr> gbdeflist;


    private int tabcount = 0;
    private int printcount = 0;
    private final String tab = "\t";


    private boolean infuncdef = false;
    private boolean inmain = false;     //放到global主要用于main函数return 0时的判断

    //    private int spoffset = 0;           // 正数，统一度量衡了
    private boolean innerfunc = false;  // 标记此时在函数体内
//    private int infuncoffset = 0;   // 正数, func内的偏移


    private Function curFunc;       // 当前函数，读取offset时用

    public ArmGenerator(ArrayList<Function> allfunclist, String outputfile) {
        this.aflist = allfunclist;
        this.armlist = new ArrayList<>();
        this.printstrMap = new HashMap<>();
        this.register = new Register();
        this.gbdeflist = new ArrayList<>();
        OUTPUT_DIR = outputfile;
    }

    public void convertarm() {
        add("/* -- testcase.s */");
        add(".data");
        add(".extern getint");
        add(".extern getch");
        add(".extern getfloat");
        add(".extern getarray");
        add(".extern getfarray");
        add(".extern putint");
        add(".extern putch");
        add(".extern putfloat");
        add(".extern putarray");
        add(".extern putfarray");
        add("");

//        collectPrintStr();

        for (Function f : aflist) {
            f.initOffsetTable();    // 初始化偏移计算

            // GlobalDef特判
            if (f.getFuncheader().getFname() == "GlobalContainer") {
                for (Instr i : f.getBlocklist().get(0).getInblocklist()) {
                    addGlobalDef(i);
                }
                add("return: .word 0");
                continue;
            }

            if (f.getFuncheader().getFname() != "GlobalContainer") {
                infuncdef = true;
                add(".text");
                //add(tab + "b main");

                if (f.getFuncheader().getFname().equals("main")) {
                    inmain = true;
                    add(".global main");
                    add("main:");
                    tabcount += 1;

                } else {
                    addFuncDef(f);  // 就是个label
                }
            }
            for (Block b : f.getBlocklist()) {
                addBlockLabel(b);
                for (Instr i : b.getInblocklist()) {

                    if (inmain || infuncdef) {
                        addInstr(i);
                    } else {
                        // global ident
                        addGlobalDef(i);
                        this.gbdeflist.add(i);
                    }
                }
            }
        }

        addProgramEnd();

        printout(1);
    }

    private void addInstr(Instr instr) {
        String iname = instr.getInstrname();
        switch (iname) {
            case "store":
                addStore(instr);
                break;

            case "push":
                addPush(instr);
                break;
            case "call":
                addCall(instr);
                break;
            case "assign":
                addAssign(instr);
                break;

            // Terminal() 系列
            case "br":
                addBr(instr);
                break;
            case "condbr":
                addCondBr(instr);
                break;
            case "ret":          //函数内return返回值
                addReturn(instr);
                break;
            default:
                break;
        }
    }

    private void addZext(Instr instr, Ident dest) {
        Type t1 = ((ZExtInst) instr).getT1();
        Type t2 = ((ZExtInst) instr).getT2();
        Value v = ((ZExtInst) instr).getV();

        String destreg = register.applyRegister(dest);

        if (v.isIdent()) {
            Ident i = v.getIdent();
            String reg = searchRegName(i);
            add("mov " + destreg + ", " + reg);

        } else {
            //todo 应该不可能是digit
        }

//        register.freeTmp(reg);
    }

    private void addAssign(Instr instr) {
        Ident dest = ((AssignInstr) instr).getIdent();
        Instr valueinstr = ((AssignInstr) instr).getValueinstr();
        String iname = valueinstr.getInstrname();

        switch (iname) {
            case "alloca":
                addAlloca(valueinstr, dest);
                break;
            case "load":
                addLoad(valueinstr, dest);
                break;
            case "binary":
                addBinary(valueinstr, dest);
                break;
            case "icmp":
                addIcmp(valueinstr, dest);
                break;
            case "zext":
                addZext(valueinstr, dest);
                break;
            case "getelementptr":
                addGetelementptr(valueinstr, dest);
                break;
            default:
                break;
        }
    }

    // %2 = getelementptr inbounds [x x [y x i32]], [x x [y x i32]]* %1, i32 a, i32 b
    // %2 = %1 + a * x * y * 4 + b * y * 4
    private void addGetelementptr(Instr instr, Ident dest) {
        Type t1 = ((GetElementPtrInst) instr).getT1();
        Type t2 = ((GetElementPtrInst) instr).getT2();
        Value v = ((GetElementPtrInst) instr).getV();


        Value v3 = ((GetElementPtrInst) instr).getV3();
        int val3 = v3.getVal();
        int val4;

        if (((GetElementPtrInst) instr).hasFourth()) {
            Value v4 = ((GetElementPtrInst) instr).getV4();
            val4 = v4.getVal();
        } else {
            val4 = 0;
        }


    }

    private void addBinary(Instr instr, Ident dest) {
        String op = ((BinaryInst) instr).getOp();

        switch (op) {
            case "add":
            case "sub":
            case "mul":
            case "sdiv":
                addOp(instr, dest);
                break;

            //todo
            case "fadd":
            case "fsub":
            case "fmul":
            case "fdiv":
//                addFop(instr,dest);
                break;
            default:
                break;
        }
    }

    private void addOp(Instr instr, Ident dest) {
        String op = ((BinaryInst) instr).getOp();
        Type t = ((BinaryInst) instr).getT();
        Value v1 = ((BinaryInst) instr).getV1();
        Value v2 = ((BinaryInst) instr).getV2();

        String reg1;
        String reg2;
        //v1
        if (v1.isIdent()) {
            Ident i = v1.getIdent();
            reg1 = searchRegName(i);

        } else {
            reg1 = register.applyTmp();
            add("mov " + reg1 + ", #" + v1.getVal());
        }

        //v2
        if (v2.isIdent()) {
            Ident i = v2.getIdent();
            reg2 = searchRegName(i);

        } else {
            reg2 = register.applyTmp();
            add("mov " + reg2 + ", #" + v2.getVal());
        }

        String destreg = register.applyRegister(dest);

        add(op + " " + destreg + ", " + reg1 + ", " + reg2);
        if (!v1.isIdent()) register.freeTmp(reg1);
        if (!v2.isIdent()) register.freeTmp(reg2);

    }

    // store i32 5, i32* %1
    private void addStore(Instr instr) {
        Type t1 = ((StoreInstr) instr).getT1();
        Type t2 = ((StoreInstr) instr).getT2();
        Value v1 = ((StoreInstr) instr).getV1();
        Value v2 = ((StoreInstr) instr).getV2();

        // v2是否分配
        Ident i2 = v2.getIdent();
        String reg2;
        if (checkReg(i2)) {
            reg2 = searchRegName(i2);
        } else {
            reg2 = register.applyRegister(i2);
        }

        // v1存到v2里
        if (v1.isIdent()) {
            Ident i1 = v1.getIdent();
            String reg1 = searchRegName(i1);
            add("mov " + reg2 + ", " + reg1);

        } else {
            int val = v1.getVal();
            add("mov " + reg2 + ", #" + val);

        }
    }

    private void addGlobalDef(Instr i) {
        Ident gi = ((GlobalDefInst) i).getGi();
        Type t = ((GlobalDefInst) i).getT();
        Value value = ((GlobalDefInst) i).getV();

        if (t.getTypec() == TypeC.I) {
            add(gi.getName() + ": .word " + value.toString());

        } else if (t.getTypec() == TypeC.A) {
            add(gi.getName() + ": .skip " + t.getSpace());
        }
        //todo other format

    }

    private void addFuncDef(Function f) {
        add(f.getFuncheader().getFname() + ":");
    }

    // block的标签
    private void addBlockLabel(Block b) {
        tabcount -= 1;
        add(b.getLabel() + ":");
        tabcount += 1;
    }

    private void addDeclStmt(Instr i) {
    }

    private void addFuncdefStmt(Instr i) {
    }

    // %2 = load i32, i32* %1
    // %3 = load i32, i32* @b
    private void addLoad(Instr instr, Ident dest) {
        Type t1 = ((LoadInst) instr).getT1();
        Type t2 = ((LoadInst) instr).getT2();
        Value v = ((LoadInst) instr).getV();

        // dest是否分配（必然未分配）
        String reg = register.applyRegister(dest);

        // reg存到dest里
        if (v.isIdent()) {
            Ident i = v.getIdent();
            String reg_i = searchRegName(i);
            add("mov " + reg + ", " + reg_i);

        } else {
            int val = v.getVal();
            add("mov " + reg + ", #" + val);

        }
    }

    // br i1 %7, label %8, label %27
    private void addCondBr(Instr instr) {
        IntType it = ((CondBrTerm) instr).getIt();
        Value v = ((CondBrTerm) instr).getV();
        Ident i1 = ((CondBrTerm) instr).getI1();
        Ident i2 = ((CondBrTerm) instr).getI2();

        Ident i = v.getIdent();
        String reg = searchRegName(i);
        add("cmp " + reg + ", #1");
        add("beq " + i1.getId());
        add("bne " + i2.getId());

    }

    private void addBr(Instr instr) {
        Ident bident = ((BrTerm) instr).getLi();
        String labelname = bident.getName();
        add("b " + labelname);
    }

    // %1 = alloca [4 x [2 x i32]]
    // 对于%1 = alloca i32，给%1赋的值是%1的绝对地址+4
    private void addAlloca(Instr instr, Ident dest) {
        Type t = ((AllocaInst) instr).getT();
        if (t.getTypec() == TypeC.A) {
            addAllocaArray(instr, dest);
        } else if (t.getTypec() == TypeC.I) {
            addAllocaInt(instr, dest);
        } else {
            error();
        }
    }

    private void addAllocaInt(Instr instr, Ident dest) {

    }

    private void addAllocaArray(Instr instr, Ident dest) {
    }

    private void addPrints(Instr instr) {
    }

    private void addJump(Instr instr) {
        //原始 addJump


        //中的jump
    }

    private void addCompareBranch(Instr instr) {
    }

    // icmp xx
    private void addIcmp(Instr instr, Ident dest) {
        // neq, ne等类型
        String ipred = ((IcmpInst) instr).getIpred();
        Type t = ((IcmpInst) instr).getT();
        Value v1 = ((IcmpInst) instr).getV1();
        Value v2 = ((IcmpInst) instr).getV2();

        String destreg = register.applyRegister(dest);

        String reg1, reg2;
        if (v1.isIdent()) {
            Ident i1 = v1.getIdent();
            reg1 = searchRegName(i1);

        } else {
            reg1 = "#" + v1.getVal();
        }

        if (v2.isIdent()) {
            Ident i2 = v2.getIdent();
            reg2 = searchRegName(i2);

        } else {
            reg2 = "#" + v2.getVal();
        }

        String movestr = ((IcmpInst) instr).predToBr();
        String oppomovestr = ((IcmpInst) instr).predToOppoBr();
        add("cmp " + reg1 + ", " + reg2);
        add("mov" + movestr + " " + destreg + ", #1");
        add("mov" + oppomovestr + " " + destreg + ", #0");

        //见：https://stackoverflow.com/questions/54237061/when-comparing-numbers-in-arm-assembly-is-there-a-correct-way-to-store-the-value
        //参考2：http://cration.rcstech.org/embedded/2014/03/02/arm-conditional-execution/
    }

    private void addGetint(Instr instr) {
    }

    private void addPush(Instr instr) {
    }

    private void addCall(Instr instr) {

        String callfuncname = ((CallInst) instr).getFuncname();

        if (((CallInst) instr).isStandardCall()) {
            addStandardCall(instr);
            return;
        }

        add("push {r0,r1,r2,r3,r4,r5,r6,r7,r8,r9,r10,r11,r12,lr}");
        add("bl " + callfuncname);
        add("pop {r0,r1,r2,r3,r4,r5,r6,r7,r8,r9,r10,r11,r12,pc}");
    }


    // 标准printf, scanf等函数
    private void addStandardCall(Instr instr) {
        String callfuncname = ((CallInst) instr).getFuncname();
        ArrayList<TypeValue> args = ((CallInst) instr).getArgs();
        int argLength = args.size();

        add("push {r0,r1,r2,r3,r4,r5,r6,r7,r8,r9,r10,r11,r12,lr}");

        switch (callfuncname) {
            //todo 补充array处理

            case "getint":

                break;
            case "getch":

                break;
            case "getfloat":


                break;
            case "getarray":

                break;
            case "getfarray":

                break;
            case "putint":
                for (TypeValue tv : args) {
                    //todo more than r0

                    assert tv.getType().getTypec() == TypeC.I;
                    Value v = tv.getValue();
                    if (v.isIdent()) {
                        String reg = searchRegName(v.getIdent());
                        add("mov r0, " + reg);

                    } else {
                        add("mov r0, #" + v.getVal());
                    }
                }
                // add("bl putint");        //最后统一弄

                break;
            case "putch":


                break;
            case "putfloat":


                break;
            case "putarray":

                break;
            case "putfarray":

                break;
            case "default":

                break;
        }

        //统一
        add("bl " + callfuncname);

        add("pop {r0,r1,r2,r3,r4,r5,r6,r7,r8,r9,r10,r11,r12,pc}");
    }

    private void addReturn(Instr instr) {
        Value vret = ((RetTerm) instr).getV();
        if (vret.isIdent()) {
            Ident vident = vret.getIdent();
            String targetReg = searchRegName(vident);

            add("mov r0, " + targetReg);
            // todo load

        } else {
            int num = vret.getVal();
            add("mov r0, #" + num);
            //todo
        }
//        add("bx lr");
        add("mov pc, lr");
    }

    private void addAssignRet(Instr instr) {
    }

    private void addArrayDecl(Instr instr) {
    }

    private void addIntDecl(Instr instr) {
    }


//    private void addNotes(Instr instr) {
//    }


    private void addProgramEnd() {
        // return from main
        add("ldr lr, address_of_return");
        add("ldr lr, [lr]");
        add("bx lr");

        tabcount -= 1;
        add("");
        for (Instr i : this.gbdeflist) {
            String name = ((GlobalDefInst) i).getGi().getName();
            add("addr_of_" + name + ": .word " + name);
        }
        add("address_of_return : .word return");

    }


    // todo 后续可能有用
//    private void collectPrintStr() {
//    }

    private void printout(int output) {
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
        for (String a : armlist) {
            writer.write(a + "\n");
//            System.out.println(a);
        }
        writer.flush();
        writer.close();
    }

    // 增加列表
    private void addCode(ArrayList<String> armlist) {
        this.armlist.addAll(armlist);
    }

    private void addCode(String arm) {
        this.armlist.add(arm);
    }

    //辅助用函数
    private void add(String armstr) {
        if (tabcount == 1) {
            armstr = tab + armstr;
        }
        armlist.add(armstr);
        System.out.println(armstr);
    }


    private void error() {
        System.err.println("Error!");
        System.out.println("目前输出：");
        for (String s : armlist) {
            System.out.println(s);
        }
        System.out.println("Error at next!");
        exit(0);
    }

    private String reverseCompareInstr(String instr) {
        switch (instr) {
            case "beq":
                return "beq";
            case "bne":
                return "bne";
            case "bge":
                return "blt";
            case "ble":
                return "bgt";
            case "bgt":
                return "ble";
            case "blt":
                return "bge";
            case "sge":
                return "slt";
            case "sgt":
                return "sle";
            case "sle":
                return "sgt";
            case "slt":
                return "sge";
            case "seq":
                return "seq";
            case "sne":
                return "sne";
            default:
                break;
        }
        System.err.println("MIPSTranslator / reverseCompareInstr() ???instr type = " + instr);
        return null;
    }

    //一个10进制int类型地址转Hex格式的小函数
    private String convertIntAddrToHex(int intaddr) {
        return "0x" + Integer.toHexString(intaddr);
    }

    // 应废弃
    private String searchRegName(Variable v) {
        String regname;

        if (v.isKindofsymbol()) {  //是一个蛮重要的变量
            //System.out.println("v's name = " + v.getName());

            Symbol symbol = v.getSymbol();
            if (symbol.getCurReg() == -1) {  //仅初始化未分配
                regname = register.applyRegister(symbol);

            } else {
                int regno = symbol.getCurReg();
                regname = register.getRegisterNameFromNo(regno);
            }

        } else {  //临时的“阅后即焚”野鸡变量
            if (v.getCurReg() == -1) {  //仅初始化未分配
                regname = register.applyRegister(v);

            } else {
                int regno = v.getCurReg();
                regname = register.getRegisterNameFromNo(regno);
            }

            if (regname == null || regname.equals("")) {
                System.err.println("Null Reg :" + v.toString());

                //regname = register.applyRegister(v);
            }
        }
        return regname;
    }

    //计算一个函数参数的偏移量
    private int calcuFuncParaOffset(String name) {
//        int paraorder = curFunc.varnameOrderInFuncPara(name);   //范围是1-n共n个参数
//        int parafullnum = curFunc.getParaNum();
//        int funcparaoffset = infuncoffset + 4 + (parafullnum - paraorder) * 4;
//        return funcparaoffset;
        return 0;
    }

    //计算一个函数内局部变量的偏移量
    private int calcuFuncLocalVarOffset(Symbol symbol) {
//        int localvaraddr = infuncoffset + symbol.addrOffsetDec;
//        return localvaraddr;
        return 0;
    }

    //将函数 存在sp的内容加载到寄存器
    private void loadWordOfInfuncVarFromSpToReg(Variable var, String regname) {
        String name = var.getName();
        if (curFunc.varnameIsFuncPara(name)) {    //函数内+para需要lw
            int paraspoffset = calcuFuncParaOffset(name);
            add("lw $" + regname + ", " + paraspoffset + "($sp)");

        } else {    //函数内+local var需要lw
            Symbol symbol = var.getSymbol();
            int localvarspoffset = calcuFuncLocalVarOffset(symbol);
            add("lw $" + regname + ", " + localvarspoffset + "($sp)");
        }
    }

    //将函数 存在寄存器放回sp
    private void saveWordOfInfuncVarFromRegToSp(Variable var, String regname) {
        String name = var.getName();
        if (curFunc.varnameIsFuncPara(name)) {    //函数内+para需要lw
            int paraspoffset = calcuFuncParaOffset(name);
            add("sw $" + regname + ", " + paraspoffset + "($sp)");

        } else {    //函数内+local var需要lw
            Symbol symbol = var.getSymbol();
            int localvarspoffset = calcuFuncLocalVarOffset(symbol);
            add("sw $" + regname + ", " + localvarspoffset + "($sp)");
        }
    }

    //将 局部变量 存在sp的内容加载到寄存器
    private void loadWordOfLocalMainfuncVarSymbolFromSpToReg(String regname, Symbol symbol) {
        int symboladdr = symbol.spBaseHex + symbol.addrOffsetDec;
        //String hexaddr = "0x" + Integer.toHexString(symboladdr);
        String hexaddr = convertIntAddrToHex(symboladdr);
        add("lw $" + regname + ", " + hexaddr);
    }

    //将 局部变量 存在寄存器放回sp
    private void saveWordOfLocalMainfuncVarSymbolFromSpToReg(String regname, Symbol symbol) {
        int symboladdr = symbol.spBaseHex + symbol.addrOffsetDec;
        //String hexaddr = "0x" + Integer.toHexString(symboladdr);
        String hexaddr = convertIntAddrToHex(symboladdr);
        add("sw $" + regname + ", " + hexaddr);
    }

    //将任意variable加载到指定寄存器，oper1、2、dest等均可用; 优先给offset用
    private String loadWordOfAnyVariableToRegName(Variable oper0) {
        String op0reg = "null_reg!!";

        if (oper0.isKindofsymbol()) {       //todo 判定、分类有隐患?
            Symbol oper0symbol = oper0.getSymbol();
            if (innerfunc && !oper0symbol.isGlobal()) {    //函数内+symbol需要lw
                int tmpregforop0 = register.applyTmpRegister();
                op0reg = register.getRegisterNameFromNo(tmpregforop0);

                loadWordOfInfuncVarFromSpToReg(oper0, op0reg);       //包装从函数体sp读取到reg过程

                register.freeTmpRegister(tmpregforop0);

            } else if (oper0symbol.isGlobal() && oper0symbol.getType() != Symbol.TYPE.F) {  //还要判断不是func返回值
                String globalvarname = oper0symbol.getName();
                op0reg = searchRegName(oper0);
                add("lw $" + op0reg + ", Global_" + globalvarname);

            } else {
                op0reg = searchRegName(oper0);
                loadWordOfLocalMainfuncVarSymbolFromSpToReg(op0reg, oper0symbol);
            }
        } else {
            op0reg = searchRegName(oper0);
        }

        return op0reg;
    }

    //将函数 存在sp的 int 值加载到寄存器 [此处push时使用]
    private void loadWordOfInfuncVarFromSpToReg(Variable var, String regname, int pushsign, Instrs pushinstrs) {
        String name = var.getName();
        if (curFunc.varnameIsFuncPara(name)) {    //函数内+para需要lw
            int paraspoffset = calcuFuncParaOffset(name);

//            Instr lwinstr = new NewInstr("lw $" + regname + ", ", paraspoffset, "($sp)", "actreg");  //特意用一个Instr包装处理
            //lwinstr.setAddroffset(true);
//            pushinstrs.addInstr(lwinstr);

        } else {    //函数内+local var需要lw
            Symbol symbol = var.getSymbol();
            int localvarspoffset = calcuFuncLocalVarOffset(symbol);

//            Instr lwinstr = new NewInstr("lw $" + regname + ", ", localvarspoffset, "($sp)", "actreg");  //特意用一个Instr包装处理
            //lwinstr.setAddroffset(true);
//            pushinstrs.addInstr(lwinstr);
        }
    }

    //将 局部变量 存在sp的 int 加载到寄存器 [此处push时使用]
    private void loadWordOfLocalMainfuncVarSymbolFromSpToReg(String regname, Symbol symbol, int pushsign, Instrs pushinstrs) {
        int symboladdr = symbol.spBaseHex + symbol.addrOffsetDec;
        //String hexaddr = "0x" + Integer.toHexString(symboladdr);
        String hexaddr = convertIntAddrToHex(symboladdr);

        NewInstr hexinstr = new NewInstr("lw $" + regname + ", " + hexaddr);
        pushinstrs.addInstr(hexinstr);
    }

    //将函数 存在sp的 array 地址加载到寄存器 [此处push时使用]
    //todo 正确性存疑
    private void loadAddressOfInfuncArrayVarFromSpToReg(Variable var, String regname, int pushsign, Instrs pushinstrs) {
        String name = var.getName();
        Symbol arraysymbol = var.getSymbol();

        if (curFunc.varnameIsFuncPara(name)) {    //函数内+para需要lw
            int paraspoffset = calcuFuncParaOffset(name);

            if (var.getVar() != null) {     //处理如b[1], b[i]情况
                Variable offset = var.getVar();
                String offsetType = offset.getType();

                //分类arr[1]或arr[i]处理
                if (offsetType.equals("num")) {    //offset = 数字
                    int arroffset = offset.getNum() * arraysymbol.getDimen2() * 4;    //偏移量=index * dimen2 * 4
                    paraspoffset += arroffset;
                    //若para，原样lw传地址
//                    Instr lwinstr = new NewInstr("lw $" + regname + ", ", paraspoffset, "($sp)", "actreg");  //特意用一个Instr包装处理
//                    pushinstrs.addInstr(lwinstr);

                } else {    //offset = var变量
                    int tmpregno = register.applyTmpRegister();
                    String tmpregname = register.getRegisterNameFromNo(tmpregno);   //申请临时寄存器

                    String offsetregname = loadWordOfAnyVariableToRegName(offset, 1, pushinstrs);
                    pushinstrs.addInstr(new NewInstr("sll $" + offsetregname + ", $" + offsetregname + ", 2"));   //！！！需要乘以4
                    pushinstrs.addInstr(new NewInstr("li $" + tmpregname + ", " + arraysymbol.getDimen2()));
                    pushinstrs.addInstr(new NewInstr("mult $" + offsetregname + ", $" + tmpregname));
                    pushinstrs.addInstr(new NewInstr("mflo $" + tmpregname));

                    //先把函数参数中array首地址加载到regname
//                    pushinstrs.addInstr(new NewInstr("lw $" + regname + ", ", paraspoffset, "($sp)", "actreg"));

                    //之后将regname中的地址增加偏移量(即$tmpregname)
                    pushinstrs.addInstr(new NewInstr("add $" + regname + ", $" + regname + ", $" + tmpregname));

                    //以下处理： register.freeRegister(offset);
                    if (offset.getCurReg() != -1) {
                        //register.freeRegister(offset);  //统一释放存数组偏移量的reg.此处不能放

                        NewInstr last = new NewInstr("#push an array end.");  //用一个#标签包装处理
//                        last.hasRetReg = true;        //最后一个语句，附加一个归还offsetReg操作
//                        last.setFreeRegNumber(offset.getCurReg());  //todo getCurReg方法存疑
                        pushinstrs.addInstr(last);

                    } else {
                        NewInstr last = new NewInstr("#push an array end.");  //用一个#标签包装处理
                        pushinstrs.addInstr(last);
                    }
                }

            } else {
                //若para，原样lw传地址; 若local variable，正常la传地址
//                Instr lwinstr = new NewInstr("lw $" + regname + ", ", paraspoffset, "($sp)", "actreg");  //特意用一个Instr包装处理
//                pushinstrs.addInstr(lwinstr);
            }

        } else {    //函数内+局部变量需要la

            int localvarspoffset = calcuFuncLocalVarOffset(arraysymbol);

            if (var.getVar() != null) {     //处理如b[1], b[i]情况
                Variable offset = var.getVar();
                String offsetType = offset.getType();

                //分类arr[1]或arr[i]处理
                if (offsetType.equals("num")) {    //offset = 数字
                    int arroffset = offset.getNum() * arraysymbol.getDimen2() * 4;    //偏移量=index * dimen2 * 4
                    localvarspoffset += arroffset;
                    //local variable，正常la传地址
//                    Instr lwinstr = new NewInstr("la $" + regname + ", ", localvarspoffset, "($sp)", "actreg");  //特意用一个Instr包装处理
//                    pushinstrs.addInstr(lwinstr);

                } else {    //offset = var变量
                    int tmpregno = register.applyTmpRegister();
                    String tmpregname = register.getRegisterNameFromNo(tmpregno);   //申请临时寄存器

                    String offsetregname = loadWordOfAnyVariableToRegName(offset, 1, pushinstrs);
                    pushinstrs.addInstr(new NewInstr("sll $" + offsetregname + ", $" + offsetregname + ", 2"));   //！！！需要乘以4
                    pushinstrs.addInstr(new NewInstr("li $" + tmpregname + ", " + arraysymbol.getDimen2()));
                    pushinstrs.addInstr(new NewInstr("mult $" + offsetregname + ", $" + tmpregname));
                    pushinstrs.addInstr(new NewInstr("mflo $" + tmpregname));

                    pushinstrs.addInstr(new NewInstr("add $" + tmpregname + ", $" + tmpregname + ", $sp"));
//                    pushinstrs.addInstr(new NewInstr("la $" + regname + ", ", localvarspoffset, "($" + tmpregname + ")", "actreg"));

                    //以下处理： register.freeRegister(offset);
                    if (offset.getCurReg() != -1) {
                        //register.freeRegister(offset);  //统一释放存数组偏移量的reg.此处不能放

                        //todo la有点问题，好像本质就是move
                        NewInstr last = new NewInstr("#push an array end.");  //用一个#标签包装处理
//                        last.hasRetReg = true;        //最后一个语句，附加一个归还offsetReg操作
//                        last.setFreeRegNumber(offset.getCurReg());  //todo getCurReg方法存疑
                        pushinstrs.addInstr(last);

                    } else {
                        NewInstr last = new NewInstr("#push an array end.");  //用一个#标签包装处理
                        pushinstrs.addInstr(last);
                    }
                }

            } else {
                //若para，原样lw传地址; 若local variable，正常la传地址
//                Instr lwinstr = new NewInstr("la $" + regname + ", ", localvarspoffset, "($sp)", "actreg");  //特意用一个Instr包装处理
//                pushinstrs.addInstr(lwinstr);
            }
        }
    }

    //将 局部变量 存在sp的 array 地址 加载到寄存器 [此处push时使用]
    private void loadAddressOfLocalMainfuncArrayVarSymbolFromSpToReg(String regname, Symbol symbol, int pushsign, Instrs pushinstrs, Variable var) {
        int symboladdr = symbol.spBaseHex + symbol.addrOffsetDec;

        if (var.getVar() != null) {     //处理如array[1]或array[i]情况
            Variable offset = var.getVar();
            String offsetType = offset.getType();

            //分类arr[1]或arr[i]处理
            if (offsetType.equals("num")) {    //offset = 数字
                int arroffset = offset.getNum() * var.getSymbol().getDimen2() * 4;    //偏移量=index * dimen2 * 4
                symboladdr += arroffset;

                String hexaddr = convertIntAddrToHex(symboladdr);
                NewInstr hexinstr = new NewInstr("li $" + regname + ", " + hexaddr);
                pushinstrs.addInstr(hexinstr);

            } else {    //offset = var变量
                int tmpregno = register.applyTmpRegister();
                String tmpregname = register.getRegisterNameFromNo(tmpregno);   //申请临时寄存器

                String offsetregname = loadWordOfAnyVariableToRegName(offset, 1, pushinstrs);
                pushinstrs.addInstr(new NewInstr("sll $" + offsetregname + ", $" + offsetregname + ", 2"));   //！！！需要乘以4
                pushinstrs.addInstr(new NewInstr("li $" + tmpregname + ", " + var.getSymbol().getDimen2()));
                pushinstrs.addInstr(new NewInstr("mult $" + offsetregname + ", $" + tmpregname));
                pushinstrs.addInstr(new NewInstr("mflo $" + tmpregname));

                //tmpregname是此时算出的偏移量
                pushinstrs.addInstr(new NewInstr("addi $" + regname + ", $" + tmpregname + ", " + symboladdr));

                //以下处理： register.freeRegister(offset);
                if (offset.getCurReg() != -1) {
                    //register.freeRegister(offset);  //统一释放存数组偏移量的reg.此处不能放
                    //todo la有点问题，好像本质就是move
                    NewInstr last = new NewInstr("#push an local array end.");  //用一个#标签包装处理
//                    last.hasRetReg = true;        //最后一个语句，附加一个归还offsetReg操作
//                    last.setFreeRegNumber(offset.getCurReg());  //todo getCurReg方法存疑
//                    pushinstrs.addInstr(last);

                } else {
                    NewInstr last = new NewInstr("#push an local array end.");  //用一个#标签包装处理
                    pushinstrs.addInstr(last);
                }
            }

        } else {  //无偏移量
            String hexaddr = convertIntAddrToHex(symboladdr);
            NewInstr hexinstr = new NewInstr("li $" + regname + ", " + hexaddr);
            pushinstrs.addInstr(hexinstr);
        }
    }

    //将任意variable加载到指定寄存器，oper1、2、dest等均可用; 优先给offset用 [此处push时使用]
    private String loadWordOfAnyVariableToRegName(Variable oper0, int pushsign, Instrs pushinstrs) {
        String op0reg = "null_reg!!";

        if (oper0.isKindofsymbol()) {       //todo 判定、分类有隐患?
            Symbol oper0symbol = oper0.getSymbol();
            if (innerfunc && !oper0symbol.isGlobal()) {    //函数内+symbol需要lw
                int tmpregforop0 = register.applyTmpRegister();
                op0reg = register.getRegisterNameFromNo(tmpregforop0);

                loadWordOfInfuncVarFromSpToReg(oper0, op0reg, 1, pushinstrs);       //包装从函数体sp读取到reg过程

                //register.freeTmpRegister(tmpregforop0);
                //todo tmpregforop0没放

            } else if (oper0symbol.isGlobal() && oper0symbol.getType() != Symbol.TYPE.F) {  //还要判断不是func返回值
                String globalvarname = oper0symbol.getName();
                op0reg = searchRegName(oper0);
                pushinstrs.addInstr(new NewInstr("lw $" + op0reg + ", Global_" + globalvarname));

            } else {
                op0reg = searchRegName(oper0);
                loadWordOfLocalMainfuncVarSymbolFromSpToReg(op0reg, oper0symbol, 1, pushinstrs);
            }
        } else {
            op0reg = searchRegName(oper0);
        }

        return op0reg;
    }


    /**********Reg 处理**********/
    // 右值，必定有结果
    private String searchRegName(Ident i) {
        String regname;
        int no = register.searchIdentRegNo(i);
        if (no == -1) {
            // global则加载进来
            if (i.isGlobal()) {
                regname = register.applyRegister(i);
                add("ldr " + regname + ", addr_of_" + i.getName());
                add("ldr " + regname + ", [" + regname + "]");
                return regname;
            }

            System.err.println("Error! Not assign Reg No.(" + i.toString() + ")");
            exit(0);
            return null;

        } else {
            regname = register.getRegisterNameFromNo(no);
            return regname;
        }
    }

    private boolean checkReg(Ident i) {
        String name;
        if (i.isGlobal()) {
            name = i.getName();
        } else {
            name = String.valueOf(i.getId());
        }
        return register.allocated(name);

    }

}


