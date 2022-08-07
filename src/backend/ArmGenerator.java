package backend;

import llvm.Block;
import llvm.Function;
import llvm.Ident;
import llvm.Instr.AllocaInst;
import llvm.Instr.AssignInstr;
import llvm.Instr.BrTerm;
import llvm.Instr.CallInst;
import llvm.Instr.IcmpInst;
import llvm.Instr.Instr;
import llvm.Instr.LoadInst;
import llvm.Instr.RetTerm;
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

public class ArmGenerator {
    private ArrayList<Function> aflist;
    private ArrayList<String> armlist;
    private Register register;
    private HashMap<IRCode, String> printstrMap;
    private static String OUTPUT_DIR = "testcase.s";


    private int tabcount = 0;
    private int printcount = 0;
    private final String tab = "\t";


    private boolean infuncdef = false;
    private boolean inmain = false;     //放到global主要用于main函数return 0时的判断

    private int spoffset = 0;           // 正数，统一度量衡了
    private boolean innerfunc = false;  // 标记此时在函数体内
    private int infuncoffset = 0;   // 正数, func内的偏移
    private Function curFunc;       // 当前函数

    //

    public ArmGenerator(ArrayList<Function> allfunclist) {
        this.aflist = allfunclist;
        this.armlist = new ArrayList<>();
        this.printstrMap = new HashMap<>();
        this.register = new Register();
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

//        collectPrintStr();

        for (Function f : aflist) {
            if (f.getFuncheader().getFname() != "GlobalContainer") {
                infuncdef = true;
                add(".text");
                add(tab + "b main");

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


//                    if (inmain) {
//                        addBranchStmt(i);    //todo 不包括常量、函数定义相关
//
//                    } else if (infuncdef) {
//                        addFuncdefStmt(i);
//
//                    } else {    //表明位于 indecl
//                        addDeclStmt(i);
//                    }

                    if(inmain || infuncdef){
                        addInstr(i);
                    }
                    else{
                        // global ident
                        addGlobalDef(i);
                    }

                }
            }
        }

        addProgramEnd();

        printout(1);
    }

    private void addGlobalDef(Instr i) {
        add("");
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

    private void addInstr(Instr instr) {
        String iname = instr.getInstrname();
        switch (iname) {
//            case "note":
//            case "label":
//                addNotes(instr);
//            暂未设计note
//                break;
            case "load":
                addLoad(instr);
                break;
            case "icmp":
                addIcmp(instr);
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
            case "alloca":
                addAlloca(instr);
//                addArrayDecl(code);
//                addIntDecl(code);
                break;

            // Terminal() 系列
            case "br":
                addBr(instr);
                break;
            case "condbr":
                addCondBr(instr);
//                addJump(code);
                break;
            case "ret":          //函数内return返回值
                addReturn(instr);
                break;
            default:
                break;
        }
    }

    private void addLoad(Instr instr) {
        Value loadvalue = ((LoadInst) instr).getV();
        if (loadvalue.isIdent()) {


        } else {    // digit

        }

    }

    private void addCondBr(Instr instr) {

    }

    private void addBr(Instr instr) {
        Ident bident = ((BrTerm) instr).getLi();
        String labelname = bident.getName();
        add("b " + labelname);
    }

    private void addAlloca(Instr instr) {
        Type t = ((AllocaInst) instr).getT();
        if (t.getTypec() == TypeC.A) {
            addAllocaArray(instr);
        } else if (t.getTypec() == TypeC.I) {
            addAllocaInt(instr);
        } else {
            error();
        }
    }

    private void addAllocaInt(Instr instr) {
//        String name = code.getName();
        String name = "";

        if (instr.isGlobal()) {  //全局int变量存.data段
            String intDeclWordInitStr = "Global_" + name + ": .word ";
            tabcount += 1;
            add(intDeclWordInitStr + "0:1");
            tabcount -= 1;

        } else {    //局部int变量分寄存器或存sp段
//            SymbolTable.Scope scope = code.getScope();  //修改后可获取到。！！！此处一定Main内也要这样处理！！！
//            scope.inblockoffset += 4;   //记录目前block内偏移

//            if (innerfunc) {   //在函数体内部定义intDecl表现不同
////                add("addi $sp, $sp, -4");
//                add("sub sp, sp, #4");
//
//                //todo 函数体内的decl不能与symbol绑定，最好不分寄存器，否则还得处理寄存器取入sp
//                infuncoffset += 4;
//                Symbol symbol = code.getSymbol();
//                Assert.check(symbol, "MIPSTranslator / addIntDecl()");  //todo 取symbol存疑
//                symbol.addrOffsetDec = -infuncoffset;    ///todo 记录的偏移量是此时相对于函数头的偏移，后续还要运算
//
//                if (code.init) {    //init则需要存数到$sp
//                    int regno = register.applyTmpRegister();
//                    String regname = register.getRegisterNameFromNo(regno);
//
//                    int num = code.getNum();        //todo 存疑
//                    add("li $" + regname + ", " + num);
//                    add("sw $" + regname + ", 0($sp)");
//
//                    register.freeTmpRegister(regno);
//                }   //todo 否则当0就行？不太对吧
//
//
//            } else {   //正常Main内部定义的intDecl
//                add("addi $sp, $sp, -4");
//                spoffset += 4;
//
//                Symbol symbol = code.getSymbol();
//                Assert.check(symbol, "MIPSTranslator / addIntDecl()");//todo 取symbol存疑
//                symbol.addrOffsetDec = -spoffset;    ///todo 记录地址【完成！】
//
//                /*if (register.hasSpareRegister()) {
//                    String regname = register.applyRegister(symbol);
//                    if (code.init) {
//                        add("li $" + regname + ", " + code.getNum());
//                    }
//
//                } else*/
//                //无多余寄存器
//                if (code.init) {    //init则需要存数到$sp
//                    int regno = register.applyTmpRegister();
//                    String regname = register.getRegisterNameFromNo(regno);
//
//                    int num = code.getNum();        //todo 存疑
//                    add("li $" + regname + ", " + num);
//                    add("sw $" + regname + ", 0($sp)");
//
//                    register.freeTmpRegister(regno);
//                }//否则当0就行
//
//            }
        }
    }

    private void addAllocaArray(Instr code) {
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
    private void addIcmp(Instr instr) {

        // neq, ne等类型
        String ipred = ((IcmpInst) instr).getIpred();
        Type t = ((IcmpInst) instr).getT();
        Value v1 = ((IcmpInst) instr).getV1();
        Value v2 = ((IcmpInst) instr).getV2();


        // 其实无用
        String cmpinstr = "";  //创建ircode时instr存进了operator

        Variable oper1 = new Variable("", "");
        Variable oper2 = new Variable("", "");


        String type1 = oper1.getType();
        String type2 = oper2.getType();

//        Variable dest = code.getDest();

        Variable dest = new Variable("", "");
        String destreg = searchRegName(dest);

        if (type1.equals("var") && type2.equals("var")) {
            String op1reg = "null_reg!!";
            String op2reg = "null_reg!!";
            boolean op1registmp = false;
            boolean op2registmp = false;

            int tmpregforop1 = 0;
            int tmpregforop2 = 0;

            //todo 判定有隐患
            if (oper1.isKindofsymbol()) {
                Symbol oper1symbol = oper1.getSymbol();
                if (innerfunc && !oper1symbol.isGlobal()) {    //函数内+symbol需要lw
                    tmpregforop1 = register.applyTmpRegister();
                    op1reg = register.getRegisterNameFromNo(tmpregforop1);
                    op1registmp = true;

                    loadWordOfInfuncVarFromSpToReg(oper1, op1reg);       //包装从函数体sp读取到reg过程

                    //register.freeTmpRegister(tmpregforop1);
                    // todo 有隐患，但理论上可以此时释放【答】不行，可能与oper2冲突。md，先不还了

                } else if (oper1symbol.isGlobal() && oper1symbol.getType() != Symbol.TYPE.F) {  //还要判断不是func返回值
                    String globalvarname = oper1symbol.getName();
                    op1reg = searchRegName(oper1);
                    add("lw $" + op1reg + ", Global_" + globalvarname);

                } else {
                    op1reg = searchRegName(oper1);
                    loadWordOfLocalMainfuncVarSymbolFromSpToReg(op1reg, oper1symbol);
                }
            } else {
                op1reg = searchRegName(oper1);
            }

            //todo 判定有隐患
            if (oper2.isKindofsymbol()) {
                Symbol oper2symbol = oper2.getSymbol();
                if (innerfunc && !oper2symbol.isGlobal()) {    //函数内+symbol需要lw
                    tmpregforop2 = register.applyTmpRegister();
                    op2reg = register.getRegisterNameFromNo(tmpregforop2);
                    op2registmp = true;

                    loadWordOfInfuncVarFromSpToReg(oper2, op2reg);       //包装从函数体sp读取到reg过程

                } else if (oper2symbol.isGlobal() && oper2symbol.getType() != Symbol.TYPE.F) {  //还要判断不是func返回值
                    String globalvarname = oper2symbol.getName();
                    op2reg = searchRegName(oper2);
                    add("lw $" + op2reg + ", Global_" + globalvarname);

                } else {
                    op2reg = searchRegName(oper2);
                    loadWordOfLocalMainfuncVarSymbolFromSpToReg(op2reg, oper2symbol);
                }
            } else {
                op2reg = searchRegName(oper2);
            }

            add(cmpinstr + " $" + destreg + ", $" + op1reg + ", $" + op2reg);

            if (op1registmp) {
                register.freeTmpRegister(tmpregforop1);
            } else {
                register.freeRegister(oper1);     //理论上需要判定活跃性，或是否为tmp
            }

            if (op2registmp) {
                register.freeTmpRegister(tmpregforop2);
            } else {
                register.freeRegister(oper2);     //理论上需要判定活跃性，或是否为tmp
            }

        } else if ((type1.equals("var") && type2.equals("num")) || (type1.equals("num") && type2.equals("var"))) {
            boolean reverse = false;
            if (type1.equals("num") && type2.equals("var")) {
                Variable opertmp = oper1;
                oper1 = oper2;
                oper2 = opertmp;
                reverse = true;
            }
            int op2num = oper2.getNum();

            String op1reg = "null_reg!!";
            boolean op1registmp = false;
            int tmpregforop1 = 0;

            //todo 判定有隐患
            if (oper1.isKindofsymbol()) {
                Symbol oper1symbol = oper1.getSymbol();
                if (innerfunc && !oper1symbol.isGlobal()) {    //函数内+symbol需要lw
                    tmpregforop1 = register.applyTmpRegister();
                    op1reg = register.getRegisterNameFromNo(tmpregforop1);
                    op1registmp = true;

                    loadWordOfInfuncVarFromSpToReg(oper1, op1reg);       //包装从函数体sp读取到reg过程

                    //register.freeTmpRegister(tmpregforop1);
                    // todo 有隐患，但理论上可以此时释放【答】不行，可能与oper2冲突。md，先不还了

                } else if (oper1symbol.isGlobal() && oper1symbol.getType() != Symbol.TYPE.F) {  //还要判断不是func返回值
                    String globalvarname = oper1symbol.getName();
                    op1reg = searchRegName(oper1);
                    add("lw $" + op1reg + ", Global_" + globalvarname);

                } else {
                    op1reg = searchRegName(oper1);
                    loadWordOfLocalMainfuncVarSymbolFromSpToReg(op1reg, oper1symbol);
                }
            } else {
                op1reg = searchRegName(oper1);
            }

            if (reverse) {
                cmpinstr = reverseCompareInstr(cmpinstr);
            }

            int tmpregforop2 = register.applyTmpRegister();
            String op2reg = register.getRegisterNameFromNo(tmpregforop2);

            add("li $" + op2reg + ", " + op2num);
            add(cmpinstr + " $" + destreg + ", $" + op1reg + ", $" + op2reg);

            if (op1registmp) {
                register.freeTmpRegister(tmpregforop1);
            } else {
                register.freeRegister(oper1);     //理论上需要判定活跃性，或是否为tmp
            }

            register.freeTmpRegister(tmpregforop2);

        } else {    //两个均为数字
            int num1 = oper1.getNum();
            int num2 = oper2.getNum();

            boolean reljudge = false;   //判定RelExp是否成立
            switch (cmpinstr) {
                case "sge":
                    if (num1 >= num2) {
                        reljudge = true;
                    }
                    break;
                case "sgt":
                    if (num1 > num2) {
                        reljudge = true;
                    }
                    break;
                case "sle":
                    if (num1 <= num2) {
                        reljudge = true;
                    }
                    break;
                case "slt":
                    if (num1 < num2) {
                        reljudge = true;
                    }
                    break;
                case "seq":
                    if (num1 == num2) {
                        reljudge = true;
                    }
                    break;
                case "sne": //暂无用
                    if (num1 != num2) {
                        reljudge = true;
                    }
                    break;
                default:
                    break;
            }

            if (reljudge) {
                add("# RelExp judge always true.");
                add("li $" + destreg + ", 1");
            } else {
                add("# RelExp judge always false.");
                add("li $" + destreg + ", 0");
            }
        }
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
        add("bx " + callfuncname);
    }


    // 标准printf, scanf等函数
    private void addStandardCall(Instr instr) {
        String callfuncname = ((CallInst) instr).getFuncname();
        ArrayList<TypeValue> args = ((CallInst) instr).getArgs();
        int argLength = args.size();

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
        add("bl "+callfuncname);
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
        add("bx lr");
    }

    private void addAssign(Instr instr) {
        Ident leftIdent = ((AssignInstr) instr).getIdent();
        Instr valueinstr = ((AssignInstr) instr).getValueinstr();

        //addBinary(valueinstr);
        String leftreg = register.applyRegister(leftIdent);

        //todo 右侧赋值给左

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
        add("bx lr");
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
            System.out.println(a);
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

    // 右值，必定有结果
    private String searchRegName(Ident i) {
        String regname;
        if (i.getNo() == -1) {
            System.err.println("Error! Not assign Reg No.");
            exit(0);
            return null;

        } else {
            regname = register.getRegisterNameFromNo(i.getNo());
            return regname;
        }
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
        int paraorder = curFunc.varnameOrderInFuncPara(name);   //范围是1-n共n个参数
        int parafullnum = curFunc.getParaNum();
        int funcparaoffset = infuncoffset + 4 + (parafullnum - paraorder) * 4;
        return funcparaoffset;
    }

    //计算一个函数内局部变量的偏移量
    private int calcuFuncLocalVarOffset(Symbol symbol) {
        int localvaraddr = infuncoffset + symbol.addrOffsetDec;
        return localvaraddr;
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

}


