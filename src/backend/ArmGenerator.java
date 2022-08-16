package backend;

import backend.Arm.Arm;
import backend.Arm.HeadArm;
import backend.Arm.LabelArm;
import backend.Arm.OneArm;
import backend.Arm.ThreeArm;
import backend.Arm.TmpArm;
import backend.Arm.TwoArm;
import llvm.Block;
import llvm.Function;
import llvm.Ident;
import llvm.Instr.AllocaInst;
import llvm.Instr.AssignInstr;
import llvm.Instr.BinaryInst;
import llvm.Instr.BrTerm;
import llvm.Instr.CallInst;
import llvm.Instr.CondBrTerm;
import llvm.Instr.FCmpInst;
import llvm.Instr.FPToSIInst;
import llvm.Instr.GetElementPtrInst;
import llvm.Instr.GlobalDefInst;
import llvm.Instr.IcmpInst;
import llvm.Instr.Instr;
import llvm.Instr.LoadInst;
import llvm.Instr.RetTerm;
import llvm.Instr.SIToFPInst;
import llvm.Instr.StoreInstr;
import llvm.Instr.ZExtInst;
import llvm.Type.ArrayType;
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

import static java.lang.Math.floor;
import static java.lang.Math.max;
import static java.lang.System.*;

public class ArmGenerator {
    private ArrayList<Function> aflist;
    private ArrayList<Arm> armlist;
    private Register reg;
    private HashMap<IRCode, String> printstrMap;
    private static String OUTPUT_DIR;
    private ArrayList<Instr> gbdeflist;

    private String allRegs = "{r0,r1,r2,r3,r4,r5,r6,r8,r9,r10,r11,r12,lr}";
    private String allFloatRegs1 = "{s0,s1,s2,s3,s4,s5,s6,s7,s8,s9,s10,s11,s12,s13,s14,s15}";
    private String allFloatRegs2 = "{s16,s17,s18,s19,s20,s21,s22,s23,s24,s25,s26,s27,s28,s29,s30,s31}";

    private int tabcount = 0;
    private int printcount = 0;
    private final String tab = "\t";

    private boolean outMain = false;        // 倒着读取，已经离开main函数
    private boolean intoGlobalDef = false;  // 标记此时在函数体内

    private Function curFunc;       // 当前函数，读取offset时用

    // LPIC计数器
    private int lpiccount = 1;
    private int lcount = 1;
    private int lines = 0;  // 每500插入
    private static int lpicConst = 500; // 默认每500行插入
    private int lpicusecount = 0;
    private ArrayList<String> lpicUseList;
    private boolean interpolating = false;  // 是否正在插入，不触发500

    public ArmGenerator(ArrayList<Function> allfunclist, String outputfile) {
//        Function mainFunc = allfunclist.get(allfunclist.size() - 1);
//        allfunclist.remove(allfunclist.size() - 1);
//        allfunclist.add(0, mainFunc);
        for (Function function : allfunclist) {
            function.initOffsetTable();
        }
        this.aflist = allfunclist;
        this.armlist = new ArrayList<>();
        this.printstrMap = new HashMap<>();
        this.reg = new Register();
        this.gbdeflist = new ArrayList<>();
        OUTPUT_DIR = outputfile;

        this.lpicUseList = new ArrayList<>();
    }

    public void convertarm() {
        add(new HeadArm("/* -- testcase.s */"));
        add(new HeadArm(".arch armv7ve"));
        add(new HeadArm(".arm"));
        add(new HeadArm(".section .text"));

//        add(".cpu cortex-a15");
//        add(".align 4");
        add(new HeadArm(""));
        add(new HeadArm(".global main"));

//        collectPrintStr();

        for (int j = aflist.size() - 1; j >= 0; j--) {
            Function f = aflist.get(j);
            curFunc = f;

            // GlobalDef特判
            if (f.getFuncheader().getFname().equals("GlobalContainer")) {
                addinterpoL();  // addInterpol收尾

                add(new HeadArm(""));
                add(new HeadArm(".section .data"));
                add(new HeadArm(".align 2"));
                for (Instr i : f.getBlocklist().get(0).getInblocklist()) {
                    addGlobalDef(i);
                }
                continue;

            } else if (f.getFuncheader().getFname() != "main") {
                outMain = true;
                addFuncDef(f);  // 加个label
                tabcount += 1;
            }
            for (Block b : f.getBlocklist()) {
                //if (b.isDirty()) continue;
                addBlockLabel(b);
                for (Instr i : b.getInblocklist()) {
                    add(new HeadArm("@ " + i.toString()));
                    addInstr(i);
                    add(new HeadArm(""));
                }
            }
            tabcount -= 1;
        }

//        addProgramEnd();

        printout(1);
    }

    private void addInstr(Instr instr) {
        String iname = instr.getInstrname();
        switch (iname) {
            case "store":
                addStore(instr);
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
//            add("mov " + regd + ", " + regt);
            add(new TwoArm("mov", regd, regt));
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
                // e.g. %1 = getint()
                addCall(valueinstr, dest);
                break;
            case "sitofp":
                addSIToFP(valueinstr, dest);
                break;
            case "fptosi":
                addFPToSI(valueinstr, dest);
                break;
            case "fcmp":
                addFcmp(valueinstr, dest);
                break;
            default:
                break;
        }
    }

    // e.g. %9 = fcmp olt float %8, 0x358637bd
    private void addFcmp(Instr instr, Ident dest) {
        String fpred = ((FCmpInst) instr).getFpred();   // e.g. olt
        Type t = ((FCmpInst) instr).getT();     // must float
        Value v1 = ((FCmpInst) instr).getV1();
        Value v2 = ((FCmpInst) instr).getV2();

        String reg_d = reg.applyTmp();  // 0 or 1

        String reg1 = reg.applyFTmp();
        if (v1.isIdent()) {
            loadValue(reg1, v1.getIdent());

        } else {
//            moveImm(reg1, v1.getVal());
//            add("vm ov " + reg1 + ", #" + v1.hexToFloat());
            vmoveFloat(reg1, v1);
        }

        String reg2 = reg.applyFTmp();
        if (v2.isIdent()) {
            loadValue(reg2, v2.getIdent());

        } else {
//            moveImm(reg2, v2.getVal());
//            add("vm ov " + reg2 + ", #" + v2.hexToFloat());
            vmoveFloat(reg2, v2);

        }

        // 见：https://community.arm.com/cn/f/discussions/10002/neon-vcmp-f32
        // https://community.arm.com/arm-community-blogs/b/architectures-and-processors-blog/posts/condition-codes-4-floating-point-comparisons-using-vfp
        String movestr = ((FCmpInst) instr).predToBr();
        String oppomovestr = ((FCmpInst) instr).predToOppoBr();

//        add("vcmp.f32 " + reg1 + ", " + reg2);
//        add("vmrs APSR_nzcv, FPSCR");       //@ Get the flags into APSR.
//        add("mov" + movestr + " " + reg_d + ", #1");
//        add("mov" + oppomovestr + " " + reg_d + ", #0");

        add(new TwoArm("vcmp.f32 ", reg1, reg2));
        add(new HeadArm("vmrs APSR_nzcv, FPSCR"));       //@ Get the flags into APSR.
        add(new TwoArm("mov" + movestr, reg_d, "#1"));
        add(new TwoArm("mov" + oppomovestr, reg_d, "#0"));

        reg.freeFTmp(reg1);
        reg.freeFTmp(reg2);

        storeValue(reg_d, dest);
        reg.freeTmp(reg_d);

    }

    // e.g. %68 = fptosi float %67 to i32
    private void addFPToSI(Instr instr, Ident dest) {
        Type t1 = ((FPToSIInst) instr).getT1();  // must float
        Type t2 = ((FPToSIInst) instr).getT2();  // must i32
        Value v = ((FPToSIInst) instr).getV();

        if (v.isIdent()) {
            String regtf = reg.applyFTmp();
            loadValue(regtf, v.getIdent());
//            add("vcvt.s32.f32 " + regtf + ", " + regtf);    // Converts {$reg} signed integer value to a single-precision value and stores it in {$reg}(前)
            add(new TwoArm("vcvt.s32.f32", regtf, regtf));
            // 注意：vcvt的两个参数都必须是float reg

            String dreg = reg.applyTmp();     // 整数+目的寄存器
//            add("vmov " + dreg + ", " + regtf);
            add(new TwoArm("vmov", dreg, regtf));

            reg.freeFTmp(regtf);
            reg.freeTmp(dreg);

        } else {
            //todo 不确定对不对
            String regtf = reg.applyFTmp();
            String dreg = reg.applyTmp();     // 整数+目的寄存器
//            add("vmov " + regtf + ", #" + v.hexToFloat());
            vmoveFloat(regtf, v);

//            add("vcvt.s32.f32 " + regtf + ", " + regtf);     // (target, source)
//            add("vmov " + dreg + ", " + regtf);
            add(new TwoArm("vcvt.s32.f32", regtf, regtf));
            add(new TwoArm("vmov", dreg, regtf));

            reg.freeFTmp(regtf);
            reg.freeTmp(dreg);
        }
    }

    // e.g. %31 = sitofp i32 2 to float
    private void addSIToFP(Instr instr, Ident dest) {
        Type t1 = ((SIToFPInst) instr).getT1();  // must i32
        Type t2 = ((SIToFPInst) instr).getT2();  // must float
        Value v = ((SIToFPInst) instr).getV();

        if (v.isIdent()) {
            String regt = reg.applyTmp();
            loadValue(regt, v.getIdent());

            String dregf = reg.applyFTmp();     // 浮点+目的寄存器
            //add("vmov " + dregf + ", " + regt);
            add(new TwoArm("vmov", dregf, regt));

            //add("vcvt.f32.s32 " + dregf + ", " + dregf);    // Converts {$reg} signed integer value to a single-precision value and stores it in {$reg}(前)
            add(new TwoArm("vcvt.f32.s32", dregf, dregf));

            reg.freeTmp(regt);
            reg.freeFTmp(dregf);

        } else {

            String dregf = reg.applyFTmp();     // 浮点+目的寄存器

//            add("vmov " + dregf + ", #" + v.getVal());
//            vmoveFloat(dregf, v);   // 不再使用vmovefloat，需要加额外判断

            String regt = reg.applyTmp();
            moveImm(regt, v.getVal());
            add(new TwoArm("vmov", dregf, regt));
            add(new TwoArm("vcvt.f32.s32", dregf, dregf));

            reg.freeTmp(regt);
            reg.freeFTmp(dregf);

        }
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
//            add("mul " + regd + ", " + rega + ", " + regt);
            add(new ThreeArm("mul", regd, rega, regt));

            reg.freeTmp(regt);
            reg.freeTmp(rega);

        } else {
            int mulAOff1 = v3.getVal() * off1;
            moveImm(regd, mulAOff1);
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
//                add("mul " + regb + ", " + regb + ", " + regt);
//                add("add " + regd + ", " + regd + ", " + regb);

                add(new ThreeArm("mul", regb, regb, regt));
                add(new ThreeArm("add", regd, regd, regb));

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
//        add("add " + regd + ", " + regd + ", " + regt);
        add(new ThreeArm("add", regd, regd, regt));
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
                addFop(instr, dest);    // 浮点运算系列
                break;
            default:
                break;
        }
    }

    // 浮点运算系列
    private void addFop(Instr instr, Ident dest) {
        String op = ((BinaryInst) instr).getOp();   // fadd, fsub等
        Type t = ((BinaryInst) instr).getT();       // 一般为float
        Value v1 = ((BinaryInst) instr).getV1();    // 0xAAAA 或者 %x
        Value v2 = ((BinaryInst) instr).getV2();
        String reg1, reg2;

        reg1 = reg.applyFTmp();
        if (v1.isIdent()) {
            loadValue(reg1, v1.getIdent());

        } else {
            // 操作浮点时指令变为 vmov
//            moveImm(reg1, v1.getVal());
//            add("v  m   ov  " + reg1 + ", #" + v1.hexToFloat());
            vmoveFloat(reg1, v1);

        }

        reg2 = reg.applyFTmp();
        if (v2.isIdent()) {
            loadValue(reg2, v2.getIdent());

        } else {
            // 操作浮点时指令变为 vmov
            vmoveFloat(reg2, v2);
        }

        String reg_d = reg.applyFTmp();

        // e.g. vadd.f32 s1, s2, s3
        op = 'v' + op.substring(1, op.length());
//        add(op + ".f32 " + reg_d + ", " + reg1 + ", " + reg2);
        add(new ThreeArm(op + ".f32 ", reg_d, reg1, reg2));
        storeValue(reg_d, dest);

        reg.freeFTmp(reg1);
        reg.freeFTmp(reg2);
        reg.freeFTmp(reg_d);
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

        String reg_d = reg.applyTmp();

//        add("sdiv " + reg_d + ", " + reg1 + ", " + reg2);
//        add("mul " + reg_d + ", " + reg_d + ", " + reg2);
//        add("sub " + reg_d + ", " + reg1 + ", " + reg_d);
        add(new ThreeArm("sdiv", reg_d, reg1, reg2));
        add(new ThreeArm("mul", reg_d, reg_d, reg2));
        add(new ThreeArm("sub", reg_d, reg1, reg_d));
        storeValue(reg_d, dest);

        reg.freeTmp(reg1);
        reg.freeTmp(reg2);

        reg.freeTmp(reg_d);

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

        String reg_d = reg.applyTmp();

//        add(op + " " + reg_d + ", " + reg1 + ", " + reg2);
        add(new ThreeArm(op, reg_d, reg1, reg2));
        storeValue(reg_d, dest);

        reg.freeTmp(reg1);
        reg.freeTmp(reg2);
        reg.freeTmp(reg_d);
    }

    // store i32 5, i32* %1（%1永远表示要存值的地址）
    // e.g.2. store float %34, float* %44
    private void addStore(Instr instr) {
        Type t1 = ((StoreInstr) instr).getT1();     // 可为float或i32
        Type t2 = ((StoreInstr) instr).getT2();
        Value v1 = ((StoreInstr) instr).getV1();
        Value v2 = ((StoreInstr) instr).getV2();
        boolean f = (t1.getTypec() == TypeC.F);   // true表示指令为float

        // 更新：增加支持float
        String regs;
        if (f) regs = reg.applyFTmp();
        else regs = reg.applyTmp();

        // v1存到v2里
        if (v1.isIdent()) {
            loadValue(regs, v1.getIdent());

        } else {
//            if (f) add("vm     ov " + regs + ", #" + v1.hexToFloat());
            if (f) vmoveFloat(regs, v1);
            else moveImm(regs, v1.getVal());  // 仅支持int

        }

        String regt = reg.applyTmp();
        loadValue(regt, v2.getIdent());     // float v2的地址仍为int
        if (f) add(new TmpArm("vstr " + regs + ", [" + regt + "]"));
        else add(new TmpArm("str " + regs + ", [" + regt + "]"));
        reg.freeTmp(regt);

        reg.free(regs);

    }

    private void addGlobalDef(Instr i) {
        this.gbdeflist.add(i);

        Ident gi = ((GlobalDefInst) i).getGi();
        Type t = ((GlobalDefInst) i).getT();
        Value value = ((GlobalDefInst) i).getV();

        if (t.getTypec() == TypeC.I) {
            add(new HeadArm(gi.getName() + ": .word " + value.toString()));

        } else if (t.getTypec() == TypeC.F) {
            add(new HeadArm(gi.getName() + ": .float " + value.hexToFloat()));

        } else if (t.getTypec() == TypeC.A) {

            // zeroinitializer
            // comm用法：https://stackoverflow.com/questions/501105/what-does-comm-mean
            if (value.isKeys()) {
                add(new HeadArm(".comm " + gi.getName() + ", " + t.getSpace() + ", 4"));

            } else {
                add(new HeadArm(".global " + gi.getName()));
                add(new HeadArm(".size " + gi.getName() + ", " + t.getSpace()));
                add(new LabelArm(gi.getName()));
                tabcount += 1;
                int usedSpace = 0;

//                System.out.println(t.toString());

                for (Value v : value.getTCValuePackage()) {

                    // float写法见：https://stackoverflow.com/questions/6970438/arm-assembly-float-variables

                    if (((ArrayType) t).getCoreType().getTypec() == TypeC.F) {
                        add(new HeadArm(".single 0e" + v.toString()));

                    } else {
                        add(new HeadArm(".word " + v.toString()));
                    }
                    usedSpace += 4;

//                    todo 目前写法float数组还不行（已完成）

                }
                // 不会出现，llvm ir时已经所有0显式写了
                if (t.getSpace() - usedSpace > 0) add(new HeadArm(".space " + (t.getSpace() - usedSpace)));
                tabcount -= 1;
            }

//            add(gi.getName() + ": .skip " + t.getSpace());
        }
        //todo other format

    }

    private void addFuncDef(Function f) {
        add(new LabelArm(f.getFuncheader().getFname()));

        // sp移动
        tabcount += 1;
//        add("sub sp, sp,  #" + f.getFuncSize());

        if (curFunc.getFuncheader().getFname().equals("main")) {
            selfSubImm("sp", f.getFuncSize());
        }

//        add("mov r7, sp");
        add(new TwoArm("mov", "r7", "sp"));
        tabcount -= 1;
    }

    // block的标签
    private void addBlockLabel(Block b) {
        tabcount -= 1;
        add(new LabelArm(getLable(b.getLabel())));
        tabcount += 1;
    }

    // %2 = load i32, i32* %1
    // %3 = load i32, i32* @b
    private void addLoad(Instr instr, Ident dest) {
        Type t1 = ((LoadInst) instr).getT1();
        Type t2 = ((LoadInst) instr).getT2();
        Value v = ((LoadInst) instr).getV();
        boolean f = (t1.getTypec() == TypeC.F);   // true表示指令为float

        // 更新：增加支持float
        String dreg;
        if (f) dreg = reg.applyFTmp();
        else dreg = reg.applyTmp();

        // 值存到dest reg里
        if (v.isIdent()) {
            String addrreg = reg.applyTmp();
            loadValue(addrreg, v.getIdent());

            // 从addr加载value
            if (f) add(new TmpArm("vldr " + dreg + ", [" + addrreg + "]"));
            else add(new TmpArm("ldr " + dreg + ", [" + addrreg + "]"));
            reg.freeTmp(addrreg);

        } else {
//            if (f) add("vm  ov " + dreg + ", #" + v.hexToFloat());
            if (f) vmoveFloat(dreg, v);
            else moveImm(dreg, v.getVal());
        }

        storeValue(dreg, dest);

//        if (f) reg.freeFTmp(dreg);
//        else reg.free Tmp(dreg);
        reg.free(dreg);
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
//        add("mov " + one + ", #1");
//        add("cmp " + regt + ", " + one);
//        add("beq " + getLable(i1.getId()));
//        add("bne " + getLable(i2.getId()));

        add(new TwoArm("mov", one, "#1"));
        add(new TwoArm("cmp", regt, one));
        add(new OneArm("beq", getLable(i1.getId())));
        add(new OneArm("bne", getLable(i2.getId())));

        reg.freeTmp(regt);
        reg.freeTmp(one);

    }

    private void addBr(Instr instr) {
        Ident bident = ((BrTerm) instr).getLi();
//        String labelname = bident.getName();
        String label = getLable(bident.getId());
        add(new OneArm("b", label));
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
//        add("mov " + regt + ", r7");
        add(new TwoArm("mov", regt, "r7"));

        selfAddImm(regt, (off + 4));

//        add("str " + regt + ", [sp,  #" + off + "]");
        addInstrRegSpOffset("str", regt, "r7", off);

        reg.freeTmp(regt);
    }

    private void addPrints(Instr instr) {
    }

    // icmp xx
    private void addIcmp(Instr instr, Ident dest) {
        // neq, ne等类型
        String ipred = ((IcmpInst) instr).getIpred();
        Type t = ((IcmpInst) instr).getT();
        Value v1 = ((IcmpInst) instr).getV1();
        Value v2 = ((IcmpInst) instr).getV2();

//        String destreg = register.applyRegister(dest);
        String reg_d = reg.applyTmp();

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
//        add("cmp " + reg1 + ", " + reg2);
//        add("mov" + movestr + " " + reg_d + ", #1");
//        add("mov" + oppomovestr + " " + reg_d + ", #0");

        add(new TwoArm("cmp", reg1, reg2));
        add(new TwoArm("mov" + movestr, reg_d, "#1"));
        add(new TwoArm("mov" + oppomovestr, reg_d, "#0"));

        reg.freeTmp(reg1);
        reg.freeTmp(reg2);

        storeValue(reg_d, dest);
        reg.freeTmp(reg_d);

        //见：https://stackoverflow.com/questions/54237061/when-comparing-numbers-in-arm-assembly-is-there-a-correct-way-to-store-the-value
        //参考2：http://cration.rcstech.org/embedded/2014/03/02/arm-conditional-execution/
    }

    // dest为可选参数
    private void addCall(Instr instr, Ident... dest) {
        String callfuncname = ((CallInst) instr).getFuncname();
        int argsnum = ((CallInst) instr).getArgsNum();  // 变量个数
        ArrayList<TypeValue> args = ((CallInst) instr).getArgs();

        if (((CallInst) instr).isStandardCall()) {
            addStandardCall(instr, dest);
            return;
        }

        pushRegs();
        // 准备传参数r0-r3为前四个参数，[sp]开始为第5个及之后参数
        int pushargsnum = max(argsnum * 4 - 16, 0);
//        add("sub sp, sp,  #" + (pushregs + pushargsnum));
        add(new OneArm("push", "{r7}"));
        selfSubImm("sp", getFuncSize(callfuncname));

//        if (argsnum <= 4) {
//            for (int i = argsnum - 1; i >= 0; i--) {
//                TypeValue tv = args.get(i);
//                if (tv.getType().getTypec() == TypeC.P) {
//
//                } else {
//                    Value v = tv.getValue();
//                    if (v.isIdent()) {
//                        String regt = reg.applyTmp();
//                        loadValue(regt, v.getIdent());
//                        add("mov r" + i + ", " + regt);
//                        reg.freeTmp(regt);
//
//                    } else {
//                        moveImm("r" + i, v.getVal());
//                    }
//                }
//            }
//        }
        //暂时全用内存传参
        for (int i = 0; i < argsnum; i++) {
            TypeValue tv = args.get(i);
            Value v = tv.getValue();
            if (v.isIdent()) {
                String regt = reg.applyTmp();
                loadValue(regt, v.getIdent());
//                add("mov r" + i + ", " + regt);
                add(new TmpArm("str " + regt + ", [sp, #" + i * 4 + "]"));
                reg.freeTmp(regt);
            } else {
                String regt = reg.applyTmp();
                moveImm(regt, v.getVal());
                add(new TmpArm("str " + regt + ", [sp, #" + i * 4 + "]"));
                reg.freeTmp(regt);
            }

        }

        //todo
//        add("bl " + callfuncname);
        add(new OneArm("bl", callfuncname));

        // 若有dest，保存返回结果


//        add("add sp, sp,  #" + (pushregs + pushargsnum));

        selfAddImm("sp", getFuncSize(callfuncname));
//        add("pop {r7}");
        add(new OneArm("pop", "{r7}"));
        if (dest.length > 0) {
            storeValue("r0", dest[0]);
        }
        popRegs();
    }

    // 标准printf, scanf等函数
    private void addStandardCall(Instr instr, Ident... dest) {
        String callfuncname = ((CallInst) instr).getFuncname();
        int argsnum = ((CallInst) instr).getArgsNum();  // 变量个数
        ArrayList<TypeValue> args = ((CallInst) instr).getArgs();

        pushRegs();

        // 准备传参数r0-r3为前四个参数，[sp]开始为第5个及之后参数
        int pushargsnum = max(argsnum * 4 - 16, 0);
//        add("sub sp, sp,  #" + (pushregs + pushargsnum));
        add(new OneArm("push", "{r7}"));


//        if (argsnum <= 4) {
        for (int i = 0; i < argsnum; i++) {
            TypeValue tv = args.get(i);
            Type t = tv.getType();
            Value v = tv.getValue();

            if (t.getTypec() == TypeC.F) {

            }
            // todo 数组等情况
            else {
                if (v.isIdent()) {
                    String regt = reg.applyTmp();
                    loadValue(regt, v.getIdent());
//                    add("mov r" + i + ", " + regt);
                    add(new TwoArm("mov", "r" + i, regt));
                    reg.freeTmp(regt);

                } else {
                    moveImm("r" + i, v.getVal());
                }
            }


        }
//        }
        //暂时全用内存传参

        //todo
//        add("bl " + callfuncname);
        add(new OneArm("bl", callfuncname));

//        add("add sp, sp,  #" + (pushregs + pushargsnum));

//        add("pop {r7}");
        add(new OneArm("pop", "{r7}"));

        // 若有dest，保存返回结果
        if (dest.length > 0) {
            storeValue("r0", dest[0]);
        }
        popRegs();
    }

    private void addReturn(Instr instr) {
        Value vret = ((RetTerm) instr).getV();

        // ret void啥也不用管
        if (vret != null) {
            if (vret.isIdent()) {
                String regt = reg.applyTmp();
                loadValue(regt, vret.getIdent());
//                add("mov r0, " + regt);
                add(new TwoArm("mov", "r0", regt));
                reg.freeTmp(regt);

            } else {
                int num = vret.getVal();
                moveImm("r0", num);
            }
        }

//        add("add sp, sp,  #" + curFunc.getFuncSize());
        if (curFunc.getFuncheader().getFname().equals("main")) {
            selfAddImm("sp", curFunc.getFuncSize());
        }

        add(new OneArm("bx", "lr"));
    }

    private void addProgramEnd() {
        // return from main
        tabcount -= 1;

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
        for (Arm a : armlist) {
            writer.write(a.toString() + "\n");
//            System.out.println(a);
        }
        writer.flush();
        writer.close();
    }

//    private void addCode(String arm) {
//        this.armlist.add(arm);
//    }

    //辅助用函数
    private void add(Arm arm) {
        if (tabcount == 1) {
            arm.setWithtab(true);
        }
        armlist.add(arm);

//        System.out.println(armstr);

        // 大于lpic截断时插入.L
        if (lines > lpicConst && !interpolating) {
            addinterpoL();
            lines = 0;

        } else {
            lines += 1;
        }
    }

    // 插入.L
    private void addinterpoL() {
        interpolating = true;
        add(new OneArm("b", ".L_auto_Generate_No_" + lcount));
        tabcount -= 1;

        add(new LabelArm(".L" + lcount));
        tabcount += 1;
        for (String l_use : lpicUseList) {
            add(new HeadArm(".word " + l_use));
        }
        tabcount -= 1;

        // clear
        lpicusecount = 0;
        lpicUseList.clear();

        add(new LabelArm(".L_auto_Generate_No_" + lcount + ":"));
        lcount += 1;    // important!
        tabcount += 1;
        interpolating = false;
    }


    private void error() {
        System.err.println("Error!");
        System.out.println("目前输出：");
        for (Arm a : armlist) {
            System.out.println(a.toString());
        }
//        System.out.println("Error at next!");
        exit(0);
    }

    // 一个10进制int类型地址转Hex格式的小函数
    private String convertIntAddrToHex(int intaddr) {
        return "0x" + Integer.toHexString(intaddr);
    }

    /**********Reg 处理**********/
    // 右值，必定有结果（废弃）
    private String searchRegName(Ident i) {
        String regname;
        int no = reg.searchIdentRegNo(i);
        if (no == -1) {
            // global则加载进来
            if (i.isGlobal()) {
                regname = reg.applyRegister(i);
//                add("ldr " + regname + ", addr_of_" + i.getName());
                add(new TmpArm("ldr " + regname + ", =" + i.getName()));
//                add("ldr " + regname + ", [" + regname + "]");
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
            regname = reg.getRegnameFromNo(no);
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

    // 当vop=true时操作浮点，指令变为vldr, vstr
    private void loadValue(String regName, Ident destIdent/*, boolean... vop*/) {
        boolean isfreg = regName.charAt(0) == 's';

        // global则加载进来
        if (destIdent.isGlobal()) {
            if (isfreg) {
                interpolating = true;
                add(new TmpArm("vldr " + regName + ", .L" + lcount + "+" + lpicusecount * 4));
                addLpic(regName, destIdent.getName());
                interpolating = false;

            } else {
                // 测试新写法
                interpolating = true;
//              add("ldr " + regName + ", =" + destIdent.getName());
                add(new TmpArm("ldr " + regName + ", .L" + lcount + "+" + lpicusecount * 4));
                addLpic(regName, destIdent.getName());
                interpolating = false;
            }

        } else {
            int off = curFunc.getOffsetByName(destIdent.toString());

            if (isfreg) {
                addInstrRegSpOffset("vldr", regName, "r7", off);

            } else {
                // 封装 add("ldr " + regName + ", [sp,  #" + off + "]");
                addInstrRegSpOffset("ldr", regName, "r7", off);
            }
        }
    }

    // 当regName的首位为s时操作浮点，指令变为vldr, vstr
    private void storeValue(String regname, Ident destIdent/*, boolean... vop*/) {
        boolean isfreg = regname.charAt(0) == 's';

        // global则存储回去
        if (destIdent.isGlobal()) {

            // 测试新写法
            if (isfreg) {
                String regt = reg.applyTmp();

                interpolating = true;
//                add("ldr " + regt + ", =" + destIdent.getName());
                add(new TmpArm("ldr " + regt + ", .L" + lcount + "+" + lpicusecount * 4));
                addLpic(regt, destIdent.getName());
                interpolating = false;

                add(new TmpArm("vstr " + regname + ", [" + regt + "]"));
                reg.freeTmp(regt);

            } else {
                String regt = reg.applyTmp();

                interpolating = true;
//                add("ldr " + regt + ", =" + destIdent.getName());
                add(new TmpArm("ldr " + regt + ", .L" + lcount + "+" + lpicusecount * 4));
                addLpic(regt, destIdent.getName());
                interpolating = false;

                add(new TmpArm("str " + regname + ", [" + regt + "]"));
                reg.freeTmp(regt);
            }

        } else {
            int off = curFunc.getOffsetByName(destIdent.toString());

            if (isfreg) {
                addInstrRegSpOffset("vstr", regname, "r7", off);

            } else {
                // 封装 add("str " + regName + ", [sp, #" + off + "]");
                addInstrRegSpOffset("str", regname, "r7", off);
            }
        }
    }

    // 添加LPIC
    private void addLpic(String reg, String name) {
        tabcount -= 1;
//        add(".LPIC" + lpiccount + ":");
        add(new LabelArm(".LPIC" + lpiccount + ":"));
        tabcount += 1;

//        add("add " + reg + ", " + reg + ", pc");
        add(new ThreeArm("add", reg, reg, "pc"));

        lpicUseList.add(name + "-(.LPIC" + lpiccount + "+8)");
        lpiccount += 1;
        lpicusecount += 1;
    }

    // 封装(16位)立即数移动
    private void moveImm(String regname, int num) {
        // 负数必会有问题
        if (num < 65536 && num >= 0) {
//            add("movw " + regname + ", #" + num);
            add(new TwoArm("movw", regname, "#" + num));

        } else {
            int low = num & 0xffff;
            int high = (num >> 16) & 0xffff;
//            add("movw " + regname + ", #" + low);
//            add("movt " + regname + ", #" + high);
            add(new TwoArm("movw", regname, "#" + low));
            add(new TwoArm("movt", regname, "#" + high));

        }
    }

    // 封装 add("add " + regd + ", " + regd + ", #" + mulAOff1);
    private void selfAddImm(String regname, int num) {
        if (num < 256) {
//            add("add " + regname + ", " + regname + ", #" + num);
            add(new ThreeArm("add", regname, regname, "#" + num));

        } else {
            String regt = reg.applyTmp();
            moveImm(regt, num);
//            add("add " + regname + ", " + regname + ", " + regt);
            add(new ThreeArm("add", regname, regname, regt));
            reg.freeTmp(regt);

        }
    }

    // 一般封装 sub sp, sp, #xxxx
    private void selfSubImm(String regname, int num) {
        if (num < 256) {
//            add("sub " + regname + ", " + regname + ", #" + num);
            add(new ThreeArm("sub", regname, regname, "#" + num));

        } else {
            String regt = reg.applyTmp();
            moveImm(regt, num);
//            add("sub " + regname + ", " + regname + ", " + regt);
            add(new ThreeArm("sub", regname, regname, regt));
            reg.freeTmp(regt);

        }
    }

    // 封装形如 add("str " + regt + ", [sp, #" + off + "]");
    // 要求 off < 4096；目前来看并未出现 <0 的报错
    private void addInstrRegSpOffset(String instrname, String regname, String sp, int num) {
        if (num < 4096) {
            add(new TmpArm(instrname + " " + regname + ", [" + sp + ", #" + num + "]"));

        } else {
            String regt = reg.applyTmp();
            moveImm(regt, num);
//            add("add " + regt + ", " + sp + ", " + regt);
            add(new ThreeArm("add", regt, "sp", regt));
            add(new TmpArm(instrname + " " + regname + ", [" + regt + ", #0]"));
            reg.freeTmp(regt);
        }
    }

    private String getLable(int s) {
        return curFunc.getFuncheader().getFname() + "_label" + s;
    }

    private String getLable(String s) {
        return curFunc.getFuncheader().getFname() + "_label" + s;
    }

    private int getFuncSize(String name) {
        for (Function function : aflist) {
            if (function.getFuncheader().getFname().equals(name)) {
                return function.getFuncSize();
            }
        }
        return -10086;
    }

    // 封装如：add("vmov " + reg1 + ", #" + v1.hexToFloat());
    private void vmoveFloat(String regname, Value vfloat) {

        // 修改，sitofp不使用这个函数了

//        float floatnum;
//        if (vfloat.isHex()) floatnum = vfloat.hexToFloat();
//        else floatnum = vfloat.getVal();    // sitofp

        float floatnum = vfloat.hexToFloat();

        if (floatnum == 0) {
            // 相当于清零，arm不支持#0
            // 见：https://stackoverflow.com/questions/11205652/why-does-vmov-f64-not-allow-me-to-load-zero
            // add("eor " + regname + ", " + regname + ", " + regname);（不好使）

            String regt = reg.applyTmp();
//            add("mov " + regt + ", #0");
//            add("vmov " + regname + ", " + regt);
//            add("vcvt.f32.s32 " + regname + ", " + regname);
            add(new TwoArm("mov", regt, "#0"));
            add(new TwoArm("vmov", regname, regt));
            add(new TwoArm("vcvt.f32.s32", regname, regname));
            reg.freeTmp(regt);

        } else {
//            add("vmovw " + regname + ", #" + vfloat.hexToIntLow());
//            add("vmovt " + regname + ", #" + vfloat.hexToIntHigh());
            add(new TwoArm("vmovw", regname, "#" + vfloat.hexToIntLow()));
            add(new TwoArm("vmovt", regname, "#" + vfloat.hexToIntHigh()));
        }
    }

    // push浮点寄存器
    private void pushRegs() {
//        add("push " + allRegs);
        add(new OneArm("push", allRegs));

//        add("vpush " + allFloatRegs1);
//        add("vpush " + allFloatRegs2);
    }

    // pop浮点寄存器
    private void popRegs() {
//        add("vpop " + allFloatRegs2);
//        add("vpop " + allFloatRegs1);

//        add("pop " + allRegs);
        add(new OneArm("pop", allRegs));
    }

}
