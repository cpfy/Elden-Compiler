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

import static java.lang.Math.max;
import static java.lang.System.*;

public class ArmGenerator {
    private ArrayList<Function> aflist;
    private ArrayList<String> armlist;
    private Register reg;
    private HashMap<IRCode, String> printstrMap;
    private static String OUTPUT_DIR;
    private ArrayList<Instr> gbdeflist;


    private int tabcount = 0;
    private int printcount = 0;
    private final String tab = "\t";


    //    private boolean infuncdef = false;
//    private boolean inmain = false;     //放到global主要用于main函数return 0时的判断
    private boolean outGlobalDef = false;

    //    private int spoffset = 0;           // 正数，统一度量衡了
    private boolean innerfunc = false;  // 标记此时在函数体内
//    private int infuncoffset = 0;   // 正数, func内的偏移


    private Function curFunc;       // 当前函数，读取offset时用

    public ArmGenerator(ArrayList<Function> allfunclist, String outputfile) {
        this.aflist = allfunclist;
        this.armlist = new ArrayList<>();
        this.printstrMap = new HashMap<>();
        this.reg = new Register();
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
        add(".cpu cortex-a15");
        add(".align 4");
        add("");

//        collectPrintStr();

        for (Function f : aflist) {
            f.initOffsetTable();    // 初始化偏移计算
            curFunc = f;

            // GlobalDef特判
            if (f.getFuncheader().getFname() == "GlobalContainer") {
                for (Instr i : f.getBlocklist().get(0).getInblocklist()) {
                    addGlobalDef(i);
                }
                continue;
            }

            if (f.getFuncheader().getFname() != "GlobalContainer") {
                if (!outGlobalDef) {
                    outGlobalDef = true;
                    add(".text");
                    add(".global main");
                }

                addFuncDef(f);  // 加个label
                tabcount += 1;

            }
            for (Block b : f.getBlocklist()) {
                //if (b.isDirty()) continue;
                addBlockLabel(b);
                for (Instr i : b.getInblocklist()) {

                    if (outGlobalDef) {
                        addInstr(i);
                    } else {
                        // global ident
                        addGlobalDef(i);
                    }
                }
            }

            tabcount -= 1;
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

        String regd = reg.applyTmp();

        if (v.isIdent()) {
            String regt = reg.applyTmp();
            loadValue(regt, v.getIdent());
            add("mov " + regd + ", " + regt);
            reg.freeTmp(regt);


        } else {
            //todo 不可能是digit
        }

        storeValue(regd, dest);
        reg.freeTmp(regd);
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
            case "call":
                // e.g. getint()
                addCall(valueinstr, dest);
                break;
            case "sitofp":

                //todo
                addSIToFP(valueinstr, dest);
            case "fptosi":

                //todo
                addFPToSI(valueinstr, dest);
            default:
                break;
        }

    }

    private void addFPToSI(Instr valueinstr, Ident dest) {
        //todo
    }


    private void addSIToFP(Instr valueinstr, Ident dest) {
        //todo
    }


    // %2 = getelementptr inbounds [x x [y x i32]], [x x [y x i32]]* %1, i32 a, i32 b
    // %2 = %1 + a * (x * y * 4) + b * (y * 4)
    private void addGetelementptr(Instr instr, Ident dest) {
        Type t1 = ((GetElementPtrInst) instr).getT1();  // off1
        Type t2 = ((GetElementPtrInst) instr).getT2();  // off2
        Value v = ((GetElementPtrInst) instr).getV();   // %1
        Value v3 = ((GetElementPtrInst) instr).getV3(); // a
        int val3 = v3.getVal(), val4;

//        String regd = register.applyRegister(dest);
        String regd = reg.applyTmp();


        int off1 = t1.getOffset();
        int off2 = t2.getOffset();
        if (v3.isIdent()) {
            String rega = reg.applyTmp();
            String regt = reg.applyTmp();
            loadValue(rega, v3.getIdent());

            // mul a, a, off1
            // 封装 add("mov " + regt + ",   #" + off1);
            moveImm(regt, off1);
            add("mul " + rega + ", " + rega + ", " + regt);
            add("add " + regd + ", " + regd + ", " + rega);

            reg.freeTmp(regt);
            reg.freeTmp(rega);

        } else {
            int mulAOff1 = v3.getVal() * off1;
            selfAddImm(regd, mulAOff1);
        }

        if (((GetElementPtrInst) instr).hasFourth()) {
            Value v4 = ((GetElementPtrInst) instr).getV4();
            val4 = v4.getVal();

            if (v4.isIdent()) {
                String regb = reg.applyTmp();
                String regt = reg.applyTmp();
                loadValue(regb, v4.getIdent());

                // mul b, b, off2
                moveImm(regt, off2);
                add("mul " + regb + ", " + regb + ", " + regt);
                add("add " + regd + ", " + regd + ", " + regb);

                reg.freeTmp(regt);
                reg.freeTmp(regb);

            } else {
                int mulAOff2 = v4.getVal() * off2;
                selfAddImm(regd, mulAOff2);
            }
        }

        // 最后还要加上%1
        String regt = reg.applyTmp();
        loadValue(regt, v.getIdent());
        add("add " + regd + ", " + regd + ", " + regt);
        reg.freeTmp(regt);

        storeValue(regd, dest);
        reg.freeTmp(regd);
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
            case "srem":    // 取模
                addSrem(instr, dest);
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

    // 取模函数
    // a % b = a - (a/b)*b
    private void addSrem(Instr instr, Ident dest) {
        String op = ((BinaryInst) instr).getOp();   // must "srem"
        Type t = ((BinaryInst) instr).getT();
        Value v1 = ((BinaryInst) instr).getV1();
        Value v2 = ((BinaryInst) instr).getV2();

        String reg1, reg2;

        //v1
        reg1 = reg.applyTmp();
        if (v1.isIdent()) {
            loadValue(reg1, v1.getIdent());

        } else {
            moveImm(reg1, v1.getVal());
        }

        //v2
        reg2 = reg.applyTmp();
        if (v2.isIdent()) {
            loadValue(reg2, v2.getIdent());

        } else {
            moveImm(reg2, v2.getVal());
        }

        String destreg = reg.applyTmp();

        add("sdiv " + destreg + ", " + reg1 + ", " + reg2);
        add("mul " + destreg + ", " + destreg + ", " + reg2);
        add("sub " + destreg + ", " + reg1 + ", " + destreg);
        storeValue(destreg, dest);

        reg.freeTmp(reg1);
        reg.freeTmp(reg2);

        reg.freeTmp(destreg);

    }

    private void addOp(Instr instr, Ident dest) {
        String op = ((BinaryInst) instr).getOp();
        Type t = ((BinaryInst) instr).getT();
        Value v1 = ((BinaryInst) instr).getV1();
        Value v2 = ((BinaryInst) instr).getV2();

        String reg1, reg2;

        //v1
        reg1 = reg.applyTmp();
        if (v1.isIdent()) {
            loadValue(reg1, v1.getIdent());

        } else {
            moveImm(reg1, v1.getVal());
        }

        //v2
        reg2 = reg.applyTmp();
        if (v2.isIdent()) {
            loadValue(reg2, v2.getIdent());

        } else {
            moveImm(reg2, v2.getVal());
        }

//        String destreg = register.applyRegister(dest);
        String destreg = reg.applyTmp();

        add(op + " " + destreg + ", " + reg1 + ", " + reg2);
        storeValue(destreg, dest);

        reg.freeTmp(reg1);
        reg.freeTmp(reg2);

        reg.freeTmp(destreg);

    }

    // store i32 5, i32* %1
    // %1永远表示要存值的地址
    private void addStore(Instr instr) {
        Type t1 = ((StoreInstr) instr).getT1();
        Type t2 = ((StoreInstr) instr).getT2();
        Value v1 = ((StoreInstr) instr).getV1();
        Value v2 = ((StoreInstr) instr).getV2();

        String reg = this.reg.applyTmp();

        // v1存到v2里
        if (v1.isIdent()) {
            loadValue(reg, v1.getIdent());

        } else {
            moveImm(reg, v1.getVal());

        }

        String regt = this.reg.applyTmp();
        loadValue(regt, v2.getIdent());
        add("str " + reg + ", [" + regt + "]");

        this.reg.freeTmp(reg);
        this.reg.freeTmp(regt);
    }

    private void addGlobalDef(Instr i) {
        this.gbdeflist.add(i);

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

        // sp移动
        tabcount += 1;
//        add("sub sp, sp,  #" + f.getFuncSize());
        selfSubImm("sp", f.getFuncSize());
        tabcount -= 1;
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
//        String reg = register.applyRegister(dest);
        String destreg = reg.applyTmp();


        // 值存到dest reg里
        if (v.isIdent()) {
            loadValue(destreg, v.getIdent());

            // 从addr加载value
            add("ldr " + destreg + ", [" + destreg + "]");

        } else {
            moveImm(destreg, v.getVal());

        }

        storeValue(destreg, dest);
        reg.freeTmp(destreg);
    }

    // br i1 %7, label %8, label %27
    private void addCondBr(Instr instr) {
        IntType it = ((CondBrTerm) instr).getIt();
        Value v = ((CondBrTerm) instr).getV();
        Ident i1 = ((CondBrTerm) instr).getI1();
        Ident i2 = ((CondBrTerm) instr).getI2();

        String regt = reg.applyTmp();
        loadValue(regt, v.getIdent());

        String one = reg.applyTmp();
        add("mov " + one + ", #1");
        add("cmp " + regt + ", " + one);
        add("beq " + i1.getId());
        add("bne " + i2.getId());

        reg.freeTmp(regt);
        reg.freeTmp(one);

    }

    private void addBr(Instr instr) {
        Ident bident = ((BrTerm) instr).getLi();
//        String labelname = bident.getName();
        String label = String.valueOf(bident.getId());
        add("b " + label);
    }

    // %1 = alloca [4 x [2 x i32]]
    // 对于%1 = alloca i32，给%1赋的值是%1的绝对地址+4
    // %2 = alloca [4 * i32]就分配20字节的空间，%2这个变量里面存的是%2的绝对地址+4
    private void addAlloca(Instr instr, Ident dest) {
        Type t = ((AllocaInst) instr).getT();
//        if (t.getTypec() == TypeC.A) {
//            addAllocaArray(instr, dest);
//        } else if (t.getTypec() == TypeC.I) {
//            addAllocaInt(instr, dest);
//        } else {
//            error();
//        }

        // 地址 &1+4 存到%1 对应的cache里
        String regt = reg.applyTmp();
        int off = curFunc.getOffsetByName(dest.toString());
        add("mov " + regt + ", sp");

        selfAddImm(regt, (off + 4));

//        add("str " + regt + ", [sp,  #" + off + "]");
        addInstrRegSpOffset("str", regt, "sp", off);

        reg.freeTmp(regt);
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

//        String destreg = register.applyRegister(dest);
        String destreg = reg.applyTmp();


        String reg1 = reg.applyTmp();
        if (v1.isIdent()) {
            loadValue(reg1, v1.getIdent());

        } else {
            moveImm(reg1, v1.getVal());
        }

        String reg2 = reg.applyTmp();
        if (v2.isIdent()) {
            loadValue(reg2, v2.getIdent());

        } else {
            moveImm(reg2, v2.getVal());
        }

        String movestr = ((IcmpInst) instr).predToBr();
        String oppomovestr = ((IcmpInst) instr).predToOppoBr();
        add("cmp " + reg1 + ", " + reg2);
        add("mov" + movestr + " " + destreg + ", #1");
        add("mov" + oppomovestr + " " + destreg + ", #0");

        reg.freeTmp(reg1);
        reg.freeTmp(reg2);

        storeValue(destreg, dest);
        reg.freeTmp(destreg);

        //见：https://stackoverflow.com/questions/54237061/when-comparing-numbers-in-arm-assembly-is-there-a-correct-way-to-store-the-value
        //参考2：http://cration.rcstech.org/embedded/2014/03/02/arm-conditional-execution/
    }

    private void addGetint(Instr instr) {
    }

    private void addPush(Instr instr) {
    }

    // dest为可选参数
    private void addCall(Instr instr, Ident... dest) {
        String callfuncname = ((CallInst) instr).getFuncname();
        int argsnum = ((CallInst) instr).getArgsNum();  // 变量个数
        ArrayList<TypeValue> args = ((CallInst) instr).getArgs();

        if (((CallInst) instr).isStandardCall()) {
            addStandardCall(instr);
            return;
        }

        add("push {r0,r1,r2,r3,r4,r5,r6,r7,r8,r9,r10,r11,r12,lr}");
        int pushregs = 52;

        // 准备传参数r0-r3为前四个参数，[sp]开始为第5个及之后参数
        int pushargsnum = max(argsnum * 4 - 16, 0);
//        add("sub sp, sp,  #" + (pushregs + pushargsnum));
        selfSubImm("sp", (pushregs + pushargsnum));

        if (argsnum <= 4) {
            for (int i = argsnum - 1; i >= 0; i--) {
                TypeValue tv = args.get(i);
                if (tv.getType().getTypec() == TypeC.P) {

                } else {
                    Value v = tv.getValue();
                    if (v.isIdent()) {
                        String regt = reg.applyTmp();
                        loadValue(regt, v.getIdent());
                        add("mov r" + i + ", " + regt);
                        reg.freeTmp(regt);

                    } else {
                        moveImm("r" + i, v.getVal());
                    }
                }
            }
        }
        //todo
        add("bl " + callfuncname);

        // 若有dest，保存返回结果
        if (dest.length > 0) {
            storeValue("r0", dest[0]);
        }

//        add("add sp, sp,  #" + (pushregs + pushargsnum));

        selfAddImm("sp", (pushregs + pushargsnum));
        add("pop {r0,r1,r2,r3,r4,r5,r6,r7,r8,r9,r10,r11,r12,pc}");
    }


    // 标准printf, scanf等函数
    private void addStandardCall(Instr instr, Ident... dest) {
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
                        String regt = reg.applyTmp();
                        loadValue(regt, v.getIdent());
                        add("mov r0, " + regt);

                    } else {
                        moveImm("r0", v.getVal());
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

        // 若有dest，保存返回结果
        if (dest.length > 0) {
            storeValue("r0", dest[0]);
        }

        add("pop {r0,r1,r2,r3,r4,r5,r6,r7,r8,r9,r10,r11,r12,lr}");
    }

    private void addReturn(Instr instr) {
        Value vret = ((RetTerm) instr).getV();

        // ret void啥也不用管
        if (vret != null) {
            if (vret.isIdent()) {
                String regt = reg.applyTmp();
                loadValue(regt, vret.getIdent());
                add("mov r0, " + regt);
                reg.freeTmp(regt);

            } else {
                int num = vret.getVal();
                moveImm("r0", num);
            }
        }

//        add("add sp, sp,  #" + curFunc.getFuncSize());
        selfAddImm("sp", curFunc.getFuncSize());
        add("bx lr");
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
        tabcount -= 1;
        add("");
        for (Instr i : this.gbdeflist) {
            String name = ((GlobalDefInst) i).getGi().getName();
            add("addr_of_" + name + ": .word " + name);
        }

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

    // 一个10进制int类型地址转Hex格式的小函数
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
                regname = reg.applyRegister(symbol);

            } else {
                int regno = symbol.getCurReg();
                regname = reg.getRegisterNameFromNo(regno);
            }

        } else {  //临时的“阅后即焚”野鸡变量
            if (v.getCurReg() == -1) {  //仅初始化未分配
                regname = reg.applyRegister(v);

            } else {
                int regno = v.getCurReg();
                regname = reg.getRegisterNameFromNo(regno);
            }

            if (regname == null || regname.equals("")) {
                System.err.println("Null Reg :" + v.toString());

                //regname = register.applyRegister(v);
            }
        }
        return regname;
    }

    /**********Reg 处理**********/
    // 右值，必定有结果
    private String searchRegName(Ident i) {
        String regname;
        int no = reg.searchIdentRegNo(i);
        if (no == -1) {
            // global则加载进来
            if (i.isGlobal()) {
                regname = reg.applyRegister(i);
                add("ldr " + regname + ", addr_of_" + i.getName());
                add("ldr " + regname + ", [" + regname + "]");
                return regname;

            } else {
                String regt = reg.applyTmp();
                loadValue(regt, i);
                return regt;
            }

//            System.err.println("Error! Not assign Reg No.(" + i.toString() + ")");
//            exit(0);
//            return null;

        } else {
            regname = reg.getRegisterNameFromNo(no);
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
        return reg.allocated(name);

    }

    private void loadValue(String regName, Ident destIdent) {
        // global则加载进来
        if (destIdent.isGlobal()) {
            add("ldr " + regName + ", addr_of_" + destIdent.getName());
//            add("ldr " + regName + ", [" + regName + "]");

        } else {
//            String regt = register.applyTmp();
//            int off = curFunc.getOffsetByName(destIdent.toString());
//            add("ldr " + regt + ", [sp,  #" + off + "]");
//            add("ldr " + regName + ", [" + regt + "]");
//            register.freeTmp(regt);

            int off = curFunc.getOffsetByName(destIdent.toString());

//            add("ldr " + regName + ", [sp,  #" + off + "]");
            addInstrRegSpOffset("ldr", regName, "sp", off);
        }
    }

    private void storeValue(String regName, Ident destIdent) {
        // global则存储回去
        if (destIdent.isGlobal()) {
//            String regt = register.applyTmp();
//            add("ldr " + regt + ", addr_of_" + destIdent.getName());
//            add("str " + regName + ", [" + regt + "]");
//            register.freeTmp(regt);

            add("str " + regName + ", addr_of_" + destIdent.getName());

        } else {
//            int off = curFunc.getOffsetByName(destIdent.toString());
//            String regt = register.applyTmp();
//            add("ldr " + regt + ", [sp,  #" + off + "]");
//            add("str " + regName + ", [" + regt + "]");
//            register.freeTmp(regt);

            int off = curFunc.getOffsetByName(destIdent.toString());

//            add("str " + regName + ", [sp, #" + off + "]");
            addInstrRegSpOffset("str", regName, "sp", off);
        }
    }

    // 封装(16位)立即数移动
    private void moveImm(String regname, int num) {

        // 负数必会有问题
        if (num < 65536) {
            add("mov " + regname + ", #" + num);

        } else {
            int low = num & 0xffff;
            int high = (num >> 16) & 0xffff;
            add("mov " + regname + ", #" + low);
            add("movt " + regname + ", #" + high);

        }
    }

    // 封装 add("add " + regd + ", " + regd + ", #" + mulAOff1);
    private void selfAddImm(String regname, int num) {
        if (num < 4096) {
            add("add " + regname + ", " + regname + ", #" + num);

        } else {
            String regt = reg.applyTmp();
            moveImm(regt, num);
            add("add " + regname + ", " + regname + ", " + regt);
            reg.freeTmp(regt);

        }
    }

    // 一般封装 sub sp, sp, #xxxx
    private void selfSubImm(String regname, int num) {
        if (num < 4096) {
            add("sub " + regname + ", " + regname + ", #" + num);

        } else {
            String regt = reg.applyTmp();
            moveImm(regt, num);
            add("sub " + regname + ", " + regname + ", " + regt);
            reg.freeTmp(regt);

        }
    }

    // 封装形如 add("str " + regt + ", [sp, #" + off + "]");
    // 要求 off < 4096
    private void addInstrRegSpOffset(String instrname, String regname, String sp, int num) {
        if (num < 4096) {
            add(instrname + " " + regname + ", [sp, #" + num + "]");

        } else {
            String regt = reg.applyTmp();
            moveImm(regt, num);
            add("add " + "sp" + ", " + "sp" + ", " + regt);
            reg.freeTmp(regt);

            add(instrname + " " + regname + ", [sp, #0]");
        }
    }
}


