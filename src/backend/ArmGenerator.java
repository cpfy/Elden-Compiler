package backend;

import backend.Arm.*;
import backend.Reg.LiveIntervals;
import backend.Reg.RegisterOld;
import llvm.Block;
import llvm.Function;
import llvm.Ident;
import llvm.Instr.*;
import llvm.Type.*;
import llvm.TypeValue;
import llvm.Value;
import tool.OutputControl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import static java.lang.Math.max;
import static java.lang.System.*;

public class ArmGenerator {
    private ArrayList<Function> aflist;
    private ArrayList<Arm> armlist;
    private ArrayList<Instr> gbdeflist;
    private RegisterOld reg;           // 管理寄存器分配
    private static String OUTPUT_DIR;

    private String allRegs = "{r4,r5,r6,r7,r8,r9,r10,r11,r12,lr}";
    private String allFloatRegs1 = "{s0,s1,s2,s3,s4,s5,s6,s7,s8,s9,s10,s11,s12,s13,s14,s15}";
    private String allFloatRegs2 = "{s16,s17,s18,s19,s20,s21,s22,s23,s24,s25,s26,s27,s28,s29,s30,s31}";

    private int tabcount = 0;

    private boolean outMain = false;        // 倒着读取，已经离开main函数
    private boolean intoGlobalDef = false;  // 标记此时在函数体内

    private Function curFunc;       // 当前函数，读取offset时用

    /*** !!! ***/
//    private RegAllocBase RA;    // 寄存器分配
//    private VirtRegMap VRM;
//    private LiveRegMatrix Matrix;
    private LiveIntervals LIS;

    // LPIC计数器
    private int lpiccount = 1;
    private int lcount = 1;
    private int lines = 0;  // 每500插入
    private static int lpicConst = 500; // 默认每500行插入
    private int lpicusecount = 0;
    private ArrayList<String> lpicUseList;
    private boolean interpolating = false;  // 是否正在插入，不触发500


    public ArmGenerator(ArrayList<Function> allfunclist, String outputfile) {
        for (Function function : allfunclist) {
            function.initOffsetTable();
        }
        this.aflist = allfunclist;
        this.armlist = new ArrayList<>();
        this.gbdeflist = new ArrayList<>();
        this.reg = new RegisterOld();
        OUTPUT_DIR = outputfile;

        this.lpicUseList = new ArrayList<>();

        // 寄存器等初始化
        this.LIS = new LiveIntervals();
        this.reg.setLIS(this.LIS);  // 固定一个instance，每个function更新其内容
//        this.VRM = new VirtRegMap();
//        this.Matrix = new LiveRegMatrix();
//        this.RA = new RegAllocBase(this.VRM, this.LIS, this.Matrix);

    }

    public void convertarm() {
        add(new HeadArm("/* -- testcase.s */"));
        add(new HeadArm(".arch armv7ve"));
        add(new HeadArm(".arm"));
        add(new HeadArm(".fpu vfpv3-d16"));
        add(new HeadArm(".section .text"));

        add(new HeadArm(""));
        add(new HeadArm(".global main"));

        for (int j = aflist.size() - 1; j >= 0; j--) {
            Function f = aflist.get(j);
            curFunc = f;

            LIS.scanIntervals(f);
            reg.RegAllocScan();

            // GlobalDef特判
            if (f.getFuncheader().getFname().equals("GlobalContainer")) {
                addinterpoL();  // addInterpol收尾

                interpolating = true;   // 保证.data中必不会出现中转块

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
                    OutputControl.printMessage("\t" + i.toString());
                    add(new HeadArm("@ " + i.toString()));
                    addInstr(i);
                    add(new HeadArm(""));
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

        String reg_d = reg.T0;   // 代替原写法：String regd = reg.applyTmp();

        if (v.isIdent()) {
//            String regt = reg.T1;
//            loadValue(regt, v.getIdent());
////            add("mov " + regd + ", " + regt);
//            add(new TwoArm("mov", regd, regt));

            loadValue(reg_d, v.getIdent());

        } else {
            //todo 不可能是digit
        }

        storeValue(reg_d, dest);
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
                // todo!! 注意！所有assign都要storeValue
                break;
        }
    }

    // e.g. %9 = fcmp olt float %8, 0x358637bd
    // todo 考虑寄存器+优化
    private void addFcmp(Instr instr, Ident dest) {
        String fpred = ((FCmpInst) instr).getFpred();   // e.g. olt
        Type t = ((FCmpInst) instr).getT();     // must float
        Value v1 = ((FCmpInst) instr).getV1();
        Value v2 = ((FCmpInst) instr).getV2();

        String reg_d = reg.T0;  // 0 or 1

        String reg1 = reg.F1;
        if (v1.isIdent()) {
            loadValue(reg1, v1.getIdent());

        } else {
            vmoveFloat(reg1, v1);
        }

        String reg2 = reg.F2;
        if (v2.isIdent()) {
            loadValue(reg2, v2.getIdent());

        } else {
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

//        reg.freeFT  mp(reg1);
//        reg.freeFT  mp(reg2);

        storeValue(reg_d, dest);
//        reg.freeTmp(re g_d);

    }

    // e.g. %68 = fptosi float %67 to i32
    private void addFPToSI(Instr instr, Ident dest) {
        Type t1 = ((FPToSIInst) instr).getT1();  // must float
        Type t2 = ((FPToSIInst) instr).getT2();  // must i32
        Value v = ((FPToSIInst) instr).getV();

        String reg_d = reg.T0;     // 整数+目的寄存器
        String f_regt = reg.F0;  //TODO 存疑

        if (v.isIdent()) {
            loadValue(f_regt, v.getIdent());

        } else {
            // todo 不确定对不对
            vmoveFloat(f_regt, v);
        }

        // add("vcvt.s32.f32 " + f_regt + ", " + f_regt);    // Converts {$reg} signed integer value to a single-precision value and stores it in {$reg}(前)
        add(new TwoArm("vcvt.s32.f32", f_regt, f_regt));  // 注意：vcvt的两个参数都必须是float reg

        // add("vmov " + reg_d + ", " + f_regt);
        add(new TwoArm("vmov.f32", reg_d, f_regt));

//        reg.freeFT  mp(f_regt);

        storeValue(reg_d, dest);
//        reg.freeTmp(r eg_d);
    }

    // e.g. %31 = sitofp i32 2 to float
    private void addSIToFP(Instr instr, Ident dest) {
        Type t1 = ((SIToFPInst) instr).getT1();  // must i32
        Type t2 = ((SIToFPInst) instr).getT2();  // must float
        Value v = ((SIToFPInst) instr).getV();

        String f_reg_d = reg.F0;     // 浮点+目的寄存器  //TODO 存疑
        String regt = reg.T1;

        if (v.isIdent()) {
            loadValue(regt, v.getIdent());

        } else {
            moveImm(regt, v.getVal());
        }

        //add("vmov " + f_reg_d + ", " + regt);
        add(new TwoArm("vmov.f32", f_reg_d, regt));

        //add("vcvt.f32.s32 " + f_reg_d + ", " + f_reg_d);    // Converts {$reg} signed integer value to a single-precision value and stores it in {$reg}(前)
        add(new TwoArm("vcvt.f32.s32", f_reg_d, f_reg_d));

        storeValue(f_reg_d, dest);   //todo 这个可优化，不从f_reg_d存

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
        String reg_d = reg.T0;

        int off1 = t1.getOffset();
        int off2 = t2.getOffset();
        if (v3.isIdent()) {
            String regt = reg.T1;
            loadValue(reg_d, v3.getIdent());

            // 封装 add("mov " + regt + ",   #" + off1);
            moveImm(regt, off1);

            // add("mul " + regd + ", " + rega + ", " + regt);
            add(new ThreeArm("mul", reg_d, reg_d, regt));


        } else {
            int mulAOff1 = v3.getVal() * off1;
            moveImm(reg_d, mulAOff1);
        }

        if (((GetElementPtrInst) instr).hasFourth()) {
            Value v4 = ((GetElementPtrInst) instr).getV4();
            val4 = v4.getVal();

            if (v4.isIdent()) {
                String regt = reg.T1;
                String regx = reg.T2;

                loadValue(regx, v4.getIdent());

                // mul b, b, off2
                moveImm(regt, off2);
//                add("mul " + regb + ", " + regb + ", " + regt);
//                add("add " + regd + ", " + regd + ", " + regb);

                add(new ThreeArm("mul", regx, regx, regt));
                add(new ThreeArm("add", reg_d, reg_d, regx));

            } else {
                int mulAOff2 = v4.getVal() * off2;
                selfAddImm(reg_d, mulAOff2);
            }
        }

        // 最后还要加上%1
        String regt = reg.T1;
        loadValue(regt, v.getIdent());
//        add("add " + regd + ", " + regd + ", " + regt);
        add(new ThreeArm("add", reg_d, reg_d, regt));

        storeValue(reg_d, dest);

    }


    private void addBinary(Instr instr, Ident dest) {
        String op = ((BinaryInst) instr).getOp();

        switch (op) {
            case "add":
            case "sub":
                addOp(instr, dest);
                break;
            case "mul":
                addMultOptimize(instr, dest);
                break;
            case "sdiv":
                addSdivOptimize(instr, dest);   // 有bug，暂时关闭（已修好）
                break;
            case "srem":    // 取模
                addSremOptimize(instr, dest);
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
        String f_reg1, f_reg2;

        f_reg1 = reg.F1;
        if (v1.isIdent()) {
            loadValue(f_reg1, v1.getIdent());

        } else {
            // 操作浮点时指令变为 vmov
//            moveImm(reg1, v1.getVal());
//            add("v  m   ov  " + reg1 + ", #" + v1.hexToFloat());
            vmoveFloat(f_reg1, v1);

        }

        f_reg2 = reg.F2;
        if (v2.isIdent()) {
            loadValue(f_reg2, v2.getIdent());

        } else {
            // 操作浮点时指令变为 vmov
            vmoveFloat(f_reg2, v2);
        }

        String f_reg_d = reg.F0;

        // e.g. vadd.f32 s1, s2, s3
        op = 'v' + op.substring(1, op.length());
//        add(op + ".f32 " + reg_d + ", " + reg1 + ", " + reg2);
        add(new ThreeArm(op + ".f32 ", f_reg_d, f_reg1, f_reg2));
        storeValue(f_reg_d, dest);

//        reg.freeFTm p(f_reg1);
//        reg.freeFT mp(f_reg2);
//        reg.free FTm  p(f_reg_d);
    }

    // 取模函数
    // a % b = a - (a/b)*b
    private void addSremOptimize(Instr instr, Ident dest) {
        String op = ((BinaryInst) instr).getOp();   // must "srem"
        Type t = ((BinaryInst) instr).getT();
        Value v1 = ((BinaryInst) instr).getV1();
        Value v2 = ((BinaryInst) instr).getV2();

        String reg1 = reg.T1;
        String reg_d = reg.T0;

        if (v1.isIdent() && !v2.isIdent()) {
            loadValue(reg1, v1.getIdent());
            addSremOperation(reg_d, reg1, v2.getVal(), false);
            storeValue(reg_d, dest);
//            reg.freeTmp(reg1);
//            reg.freeTmp(re  g_d);
            return;

        } else if (!v1.isIdent() && v2.isIdent()) {
            loadValue(reg1, v2.getIdent());
            addSremOperation(reg_d, reg1, v1.getVal(), true);
            storeValue(reg_d, dest);
//            reg.freeTmp(reg1);
//            reg.freeTmp(re  g_d);
            return;
        }

        String reg2 = reg.T2;

        //v1
        //todo: can more optimize
        if (v1.isIdent()) {
            loadValue(reg1, v1.getIdent());

        } else {
            moveImm(reg1, v1.getVal());
        }

        //v2
        if (v2.isIdent()) {
            loadValue(reg2, v2.getIdent());

        } else {
            moveImm(reg2, v2.getVal());
        }

        add(new ThreeArm("sdiv", reg_d, reg1, reg2));
        add(new ThreeArm("mul", reg_d, reg_d, reg2));
        add(new ThreeArm("sub", reg_d, reg1, reg_d));
        storeValue(reg_d, dest);

//        reg.freeTmp(reg1);
//        reg.freeTmp(reg2);
//        reg.freeTmp(re  g_d);

    }

    private void addOp(Instr instr, Ident dest) {
        String op = ((BinaryInst) instr).getOp();
        Type t = ((BinaryInst) instr).getT();
        Value v1 = ((BinaryInst) instr).getV1();
        Value v2 = ((BinaryInst) instr).getV2();

        String reg1, reg2;

        // 两个均为常数，消Phi后出现
        if (!v1.isIdent() && !v2.isIdent()) {
            int res;
            switch (op) {
                case "add":
                    res = v1.getVal() + v2.getVal();
                    break;
                case "sub":
                    res = v1.getVal() - v2.getVal();
                    break;
                case "mul":
                    res = v1.getVal() * v2.getVal();
                    break;
                case "sdiv":
                    res = v1.getVal() / v2.getVal();
                    break;
                default:
                    error();
                    res = -142857;
                    break;
            }

            // 考虑add float / add i32 两种情况
            if (t.getTypec() == TypeC.I) {
                reg1 = reg.T1;
                moveImm(reg1, res);

            } else if (t.getTypec() == TypeC.F) {
                // todo 繁琐 有待优化 可优化
                reg1 = reg.F0;
                String regt = reg.T1;

                add(new TwoArm("mov", regt, "#0"));
                add(new TwoArm("vmov.f32", reg1, regt));
                add(new TwoArm("vcvt.f32.s32", reg1, reg1));

            } else {
                reg1 = "nnwhqkwjfkfqf...";
                exit(1);
            }

            storeValue(reg1, dest);
            return;

        }

        // 操作数中一个是0（还要再区分Float、int32情况）
        if (v1.isIdent() && !v2.isIdent() && v2.getVal() == 0 && (op.equals("add") || op.equals("sub"))) {
            if (t.getTypec() == TypeC.F) {
                reg1 = reg.F1;
            }
            //todo 不确定是否必为int
            else {
                reg1 = reg.T1;
            }
            loadValue(reg1, v1.getIdent());
            storeValue(reg1, dest);
            return;

        } else if (v2.isIdent() && !v1.isIdent() && v1.getVal() == 0 && op.equals("add")) {
            if (t.getTypec() == TypeC.F) {
                reg1 = reg.F1;
            }
            //todo 不确定是否必为int
            else {
                reg1 = reg.T1;
            }
            loadValue(reg1, v2.getIdent());
            storeValue(reg1, dest);
            return;
        }

        //v1
        reg1 = reg.T1;
        if (v1.isIdent()) {
            loadValue(reg1, v1.getIdent());

        } else {
            moveImm(reg1, v1.getVal());
        }

        //v2
        reg2 = reg.T2;
        if (v2.isIdent()) {
            loadValue(reg2, v2.getIdent());

        } else {
            moveImm(reg2, v2.getVal());
        }

        String reg_d = reg.T0;

//        add(op + " " + reg_d + ", " + reg1 + ", " + reg2);
        add(new ThreeArm(op, reg_d, reg1, reg2));
        storeValue(reg_d, dest);

//        reg.freeTmp(reg1);
//        reg.freeTmp(reg2);
//        reg.freeTmp(r  eg_d);
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
        String regx;
        if (f) regx = reg.F0;     //TODO 存疑
        else regx = reg.T2;

        // v1存到v2里
        if (v1.isIdent()) {
            loadValue(regx, v1.getIdent());

        } else {
            if (f) vmoveFloat(regx, v1);
            else moveImm(regx, v1.getVal());  // 仅支持int

        }

        String regt = reg.T1;
        loadValue(regt, v2.getIdent());     // float v2的地址仍为int
        if (f) add(new TmpArm("vstr.f32 " + regx + ", [" + regt + "]"));
        else add(new TmpArm("str " + regx + ", [" + regt + "]"));

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

//                OutputControl.printMessage(t.toString());

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
        String regx;
        if (f) regx = reg.F0;  //TODO 存疑
        else regx = reg.T2;

        // 值存到dest reg里
        if (v.isIdent()) {
            String regt = reg.T1;
            loadValue(regt, v.getIdent());

            // 从addr加载value
            if (f) add(new TmpArm("vldr.f32 " + regx + ", [" + regt + "]"));
            else add(new TmpArm("ldr " + regx + ", [" + regt + "]"));

        } else {
//            if (f) add("vm  ov " + reg_d + ", #" + v.hexToFloat());
            if (f) vmoveFloat(regx, v);
            else moveImm(regx, v.getVal());
        }

        storeValue(regx, dest);

    }

    // br i1 %7, label %8, label %27
    private void addCondBr(Instr instr) {
        IntType it = ((CondBrTerm) instr).getIt();
        Value v = ((CondBrTerm) instr).getV();
        Ident i1 = ((CondBrTerm) instr).getI1();
        Ident i2 = ((CondBrTerm) instr).getI2();

        String regt = reg.T1;
        loadValue(regt, v.getIdent());

        String one = reg.T2;
//        add("mov " + one + ", #1");
//        add("cmp " + regt + ", " + one);
//        add("beq " + getLable(i1.getId()));
//        add("bne " + getLable(i2.getId()));

        add(new TwoArm("mov", one, "#1"));
        add(new TwoArm("cmp", regt, one));
        add(new OneArm("beq", getLable(i1.getId())));
        add(new OneArm("bne", getLable(i2.getId())));

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

        // 地址 &1+4 存到%1 对应的cache里
        String regt = reg.T1;
        int off = curFunc.getOffsetByName(dest.toString());

//        add("mov " + regt + ", r7");
        add(new TwoArm("mov", regt, "r7"));

        selfAddImm(regt, (off + 4));

//        add("str " + regt + ", [sp,  #" + off + "]");
//        addInstrRegSpOffset("str", regt, "r7", off);    // dest存内存了，通过查找offset的方式
        storeValue(regt, dest);

    }

    // icmp xx
    private void addIcmp(Instr instr, Ident dest) {
        // neq, ne等类型
        String ipred = ((IcmpInst) instr).getIpred();
        Type t = ((IcmpInst) instr).getT();
        Value v1 = ((IcmpInst) instr).getV1();
        Value v2 = ((IcmpInst) instr).getV2();

//        String destreg = register.applyRegister(dest);
        String reg_d = reg.T0;

        String reg1 = reg.T1;
        if (v1.isIdent()) {
            loadValue(reg1, v1.getIdent());

        } else {
            moveImm(reg1, v1.getVal());
        }

        String reg2 = reg.T2;
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

//        reg.freeTmp(reg1);
//        reg.freeTmp(reg2);

        storeValue(reg_d, dest);
//        reg.freeTmp(re  g_d);

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
        selfSubImm("sp", getFuncSize(callfuncname));

//        if (argsnum <= 4) {
//            for (int i = argsnum - 1; i >= 0; i--) {
//                TypeValue tv = args.get(i);
//                if (tv.getType().getTypec() == TypeC.P) {
//
//                } else {
//                    Value v = tv.getValue();
//                    if (v.isIdent()) {
//                        String regt = reg.apply     Tmp();
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
                String regt = reg.T1;
                loadValue(regt, v.getIdent());
//                add("mov r" + i + ", " + regt);
                add(new TmpArm("str " + regt + ", [sp, #" + i * 4 + "]"));

            } else {
                String regt = reg.T1;
                if (v.isHex()) {//todo 可能会出问题！！！！！ by sjz
                    int low = v.hexToIntLow();
                    int high = v.hexToIntHigh();
                    add(new TwoArm("movw", regt, "#" + low));
                    add(new TwoArm("movt", regt, "#" + high));
                    add(new TmpArm("str " + regt + ", [sp, #" + i * 4 + "]"));
                } else {
                    moveImm(regt, v.getVal());
                    add(new TmpArm("str " + regt + ", [sp, #" + i * 4 + "]"));
                }
            }
        }

        add(new OneArm("bl", callfuncname));    // add("bl " + callfuncname);

        // 若有dest，保存返回结果
//        add("add sp, sp,  #" + (pushregs + pushargsnum));

        selfAddImm("sp", getFuncSize(callfuncname));

        // add("pop {r7}");
        popRegs();
        if (dest.length > 0) {
            storeValue("r0", dest[0]);
        }

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


//        if (argsnum <= 4) {
        for (int i = 0; i < argsnum; i++) {
            TypeValue tv = args.get(i);
            Type t = tv.getType();
            Value v = tv.getValue();

            if (t.getTypec() == TypeC.F) {
                if (v.isIdent()) {
                    loadValue("s" + i, v.getIdent());

                } else {
                    moveImm("r" + i, v.getVal());
                }
            }
            // todo 数组等情况
            else {
                if (v.isIdent()) {
                    loadValue("r" + i, v.getIdent());

                } else {
                    moveImm("r" + i, v.getVal());
                }
            }
        }

        //todo
        add(new OneArm("bl", callfuncname));

//        add("add sp, sp,  #" + (pushregs + pushargsnum));

        popRegs();
        // 若有dest，保存返回结果
        if (dest.length > 0) {
            if (callfuncname.equals("getfloat")) {
                storeValue("s0", dest[0]);
            } else {
                storeValue("r0", dest[0]);
            }
        }
    }

    private void addReturn(Instr instr) {
        Value vret = ((RetTerm) instr).getV();

        // ret void啥也不用管
        if (vret != null) {
            if (vret.isIdent()) {
//                String regt = reg.T1;
//                loadValue(regt, vret.getIdent());
////                add("mov r0, " + regt);
//                add(new TwoArm("mov", "r0", regt));

                loadValue("r0", vret.getIdent());   // todo 存疑

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
//            OutputControl.printMessage(a);
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

//        OutputControl.printMessage(armstr);

        // 大于lpic截断时插入.L
        if (lines > lpicConst && !interpolating) {
            addinterpoL();
            lines = 0;

        } else {
            lines += 1;
        }
    }

    // 也支持str类型。转成TmpArm再调用add
    private void add(String str) {
        add(new TmpArm(str));
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

        add(new LabelArm(".L_auto_Generate_No_" + lcount));
        lcount += 1;    // important!
        tabcount += 1;
        interpolating = false;
    }


    private void error() {
        System.err.println("Error!");
        OutputControl.printMessage("目前输出：");
        for (Arm a : armlist) {
            OutputControl.printMessage(a.toString());
        }
//        OutputControl.printMessage("Error at next!");
        exit(0);
    }

    // 一个10进制int类型地址转Hex格式的小函数
    private String convertIntAddrToHex(int intaddr) {
        return "0x" + Integer.toHexString(intaddr);
    }

    // 小函数：是否浮点寄存器
    private boolean isFloatReg(String regname) {
        return (regname.charAt(0) == 's') && (!regname.equals("sp"));
    }

    /**********Reg 处理**********/

    // 当vop=true时操作浮点，指令变为vldr, vstr
    private void loadValue(String regname, Ident destIdent) {
        boolean destIsF = isFloatReg(regname);

        // spill，para，global等无寄存器情况用传统的ldr
        if (!reg.hasPhysReg(destIdent.toString())) {
            // global则加载进来
            if (destIdent.isGlobal()) {
                if (destIsF) {
                    interpolating = true;
                    add(new TmpArm("vldr.f32 " + regname + ", .L" + lcount + "+" + lpicusecount * 4));
                    addLpic(regname, destIdent.getName());
                    interpolating = false;

                } else {
                    // 测试新写法
                    interpolating = true;
//              add("ldr " + regName + ", =" + destIdent.getName());
                    add(new TmpArm("ldr " + regname + ", .L" + lcount + "+" + lpicusecount * 4));
                    addLpic(regname, destIdent.getName());
                    interpolating = false;
                }

            } else {
                int off = curFunc.getOffsetByName(destIdent.toString());

                if (destIsF) {
                    addInstrRegSpOffset("vldr.f32", regname, "r7", off);

                } else {
                    // 封装 add("ldr " + regName + ", [sp,  #" + off + "]");
                    addInstrRegSpOffset("ldr", regname, "r7", off);
                }
            }

        }
        // 听说村里发寄存器了？！
        else {
            String physReg = reg.searchPhysReg(destIdent.toString());

            // 全局一律不分
            // if (destIdent.isGlobal()) {}

            if (destIsF) {
                if (isFloatReg(physReg)) add("vmov.f32 " + regname + ", " + physReg);    // Float. <--- Float.
                else {
                    // Float. <--- Int.
                    add(new TwoArm("vmov", regname, physReg));
                    add(new TwoArm("vcvt.f32.s32", regname, regname));
                }

            } else {
                // 应该必保证reg里有值
                if (!isFloatReg(physReg)) add("mov " + regname + ", " + physReg);   // Int. <--- Int.
                else {
                    // Int. <--- Float.
                    add("vcvt.s32.f32 " + physReg + ", " + physReg);
                    add("vmov " + regname + ", " + physReg);
                    add("vcvt.f32.s32 " + physReg + ", " + physReg);    // 还要转换回去，不可破坏
                }
            }
        }
    }

    // 当regName的首位为s时操作浮点，指令变为vldr, vstr
    private void storeValue(String regname, Ident destIdent) {
        boolean originIsF = isFloatReg(regname);

        // spill，para，global等无寄存器情况用传统的ldr
        if (!reg.hasPhysReg(destIdent.toString())) {

            // global则存储回去
            if (destIdent.isGlobal()) {

                String regsb = reg.SB;   // todo 可能与regname冲突，只能用TX，看看咋优化一下

                // 测试新写法
                if (originIsF) {
                    interpolating = true;
//                add("ldr " + regt + ", =" + destIdent.getName());
                    add(new TmpArm("ldr " + regsb + ", .L" + lcount + "+" + lpicusecount * 4));
                    addLpic(regsb, destIdent.getName());
                    interpolating = false;

                    add(new TmpArm("vstr.f32 " + regname + ", [" + regsb + "]"));

                } else {
                    interpolating = true;
                    // add("ldr " + regt + ", =" + destIdent.getName());
                    add(new TmpArm("ldr " + regsb + ", .L" + lcount + "+" + lpicusecount * 4));
                    addLpic(regsb, destIdent.getName());
                    interpolating = false;

                    add(new TmpArm("str " + regname + ", [" + regsb + "]"));

                }

            } else {
                int off = curFunc.getOffsetByName(destIdent.toString());

                if (originIsF) {
                    addInstrRegSpOffset("vstr.f32", regname, "r7", off);

                } else {
                    // 封装 add("str " + regName + ", [sp, #" + off + "]");
                    addInstrRegSpOffset("str", regname, "r7", off);
                }
            }

        }
        // 听说村里发寄存器了？！
        else {
            String physReg = reg.searchPhysReg(destIdent.toString());

            // 全局一律不分（也应当保证不会出现此情况）
            // if (destIdent.isGlobal()) {}

            if (originIsF) {
                // 考虑physReg与regname种类是否一致
                if (isFloatReg(physReg)) add("vmov.f32 " + physReg + ", " + regname);   // Float. <--- Float.
                else {
                    // Int. <--- Float.
                    add("vcvt.s32.f32 " + regname + ", " + regname);
                    add("vmov " + physReg + ", " + regname);
                }

            } else {
                // physReg里可为空
                if (!isFloatReg(physReg)) add("mov " + physReg + ", " + regname);   // Int. <--- Int.
                else {
                    // 参见：https://stackoverflow.com/questions/22510201/how-to-use-vmov-to-set-value-from-s0-to-r0
                    // Float. <--- Int.
                    add("vmov " + physReg + ", " + regname);
                    add("vcvt.f32.s32 " + physReg + ", " + physReg);

                }

            }
        }
    }

    // 添加LPIC
    private void addLpic(String reg, String name) {
        tabcount -= 1;

        add(new LabelArm(".LPIC" + lpiccount));     // add(".LPIC" + lpiccount + ":");
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
            String regsb = reg.SB;    //todo 可能与regname，勉强TX
            moveImm(regsb, num);
//            add("add " + regname + ", " + regname + ", " + regt);
            add(new ThreeArm("add", regname, regname, regsb));

        }
    }

    // 一般封装 sub sp, sp, #xxxx
    private void selfSubImm(String regname, int num) {
        if (num < 256) {
//            add("sub " + regname + ", " + regname + ", #" + num);
            add(new ThreeArm("sub", regname, regname, "#" + num));

        } else {
            String regsb = reg.SB;    //todo 可能与regname冲突，勉强TX
            moveImm(regsb, num);
//            add("sub " + regname + ", " + regname + ", " + regt);
            add(new ThreeArm("sub", regname, regname, regsb));

        }
    }

    // 封装形如 add("str " + regt + ", [sp, #" + off + "]");
    // 要求 off < 4096；目前来看并未出现 <0 的报错
    private void addInstrRegSpOffset(String instrname, String regname, String sp, int num) {
        if (instrname.charAt(0) == 'v') {       //浮点数寄存器offset范围更小
            if (num < 1024) {
                add(new TmpArm(instrname + " " + regname + ", [" + sp + ", #" + num + "]"));

            } else {
                String regsb = reg.SB;    //todo 可能与regname冲突，勉强TX
                moveImm(regsb, num);
//            add("add " + regt + ", " + sp + ", " + regt);
                add(new ThreeArm("add", regsb, sp, regsb));
                add(new TmpArm(instrname + " " + regname + ", [" + regsb + ", #0]"));

            }
        } else {
            if (num < 4096) {
                add(new TmpArm(instrname + " " + regname + ", [" + sp + ", #" + num + "]"));

            } else {
                String regsb = reg.SB;    //todo 可能与regname冲突，勉强TX
                moveImm(regsb, num);
//            add("add " + regt + ", " + sp + ", " + regt);
                add(new ThreeArm("add", regsb, sp, regsb));
                add(new TmpArm(instrname + " " + regname + ", [" + regsb + ", #0]"));

            }
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

        // 修改：sitofp不使用这个函数了

        float floatnum = vfloat.hexToFloat();
        String regsb = reg.SB;    //todo 可能与regname冲突，勉强TX

        if (floatnum == 0) {
            // 相当于清零，arm不支持#0
            // 见：https://stackoverflow.com/questions/11205652/why-does-vmov-f64-not-allow-me-to-load-zero
            // add("eor " + regname + ", " + regname + ", " + regname);（不好使）

//            add("mov " + regt + ", #0");
//            add("vmov " + regname + ", " + regt);
//            add("vcvt.f32.s32 " + regname + ", " + regname);
            add(new TwoArm("mov", regsb, "#0"));
            add(new TwoArm("vmov.f32", regname, regsb));
            add(new TwoArm("vcvt.f32.s32", regname, regname));

        } else {
//            add("vmovw " + regname + ", #" + vfloat.hexToIntLow());
//            add("vmovt " + regname + ", #" + vfloat.hexToIntHigh());
            add(new TwoArm("movw", regsb, "#" + vfloat.hexToIntLow()));
            add(new TwoArm("movt", regsb, "#" + vfloat.hexToIntHigh()));
            add(new TwoArm("vmov.f32", regname, regsb));

        }
    }

    // push浮点寄存器
    private void pushRegs() {
//        add("push " + allRegs);
        add(new OneArm("push", allRegs));
    }

    // pop浮点寄存器
    private void popRegs() {
        add(new OneArm("pop", allRegs));
    }


    /************** 之后部分为乘除优化 ****************/
    // 乘法优化（含instr）
    private void addMultOptimize(Instr instr, Ident dest) {
        String op = ((BinaryInst) instr).getOp();   // 必为mul
        Type t = ((BinaryInst) instr).getT();
        Value v1 = ((BinaryInst) instr).getV1();
        Value v2 = ((BinaryInst) instr).getV2();

        String reg_d = reg.T0;
        String reg1 = reg.T1;

        if (v1.isIdent() && v2.isIdent()) {
            addOp(instr, dest);     // todo: 最好不这么用
            return;

        } else if (v1.isIdent() && !v2.isIdent()) {
            loadValue(reg1, v1.getIdent());
            addMulOperation(reg_d, reg1, v2.getVal());

        } else if (!v1.isIdent() && v2.isIdent()) {
            loadValue(reg1, v2.getIdent());
            addMulOperation(reg_d, reg1, v1.getVal());

        } else {
            error();
        }

        storeValue(reg_d, dest);

//        reg.freeTmp(reg1);
//        reg.freeTmp(r eg_d);

    }

    // 注意!须保证regd与reg1不同
    private void addMulOperation(String reg_d, String reg1, int num) {
        // 统一申请公用，不保证所以SB
        String regsb = reg.SB;

        if (num == 0) {
            add("mov " + reg_d + ", #0");

        } else if (num == 1) {
            add("mov " + reg_d + ", " + reg1);

        } else if (isPowerOfTwo(num)) {
            int mi = (int) (Math.log(num) / Math.log(2));
            add("lsl " + reg_d + ", " + reg1 + ", #" + mi);

        } else if (isPowerOfTwo(num - 1)) {
            int mi = (int) (Math.log(num - 1) / Math.log(2));
            add("lsl " + reg_d + ", " + reg1 + ", #" + mi);
            add("add " + reg_d + ", " + reg_d + ", " + reg1);

        } else if (isPowerOfTwo(num + 1)) {
            int mi = (int) (Math.log(num + 1) / Math.log(2));
            add("lsl " + reg_d + ", " + reg1 + ", #" + mi);
            add("sub " + reg_d + ", " + reg_d + ", " + reg1);

        } else if (isPowerOfTwo(num - 2)) {
            int mi = (int) (Math.log(num - 2) / Math.log(2));
            add("lsl " + reg_d + ", " + reg1 + ", #" + mi);
            add("add " + reg_d + ", " + reg_d + ", " + reg1);
            add("add " + reg_d + ", " + reg_d + ", " + reg1);

        } else if (isPowerOfTwo(num + 2)) {
            int mi = (int) (Math.log(num + 2) / Math.log(2));
            add("lsl " + reg_d + ", " + reg1 + ", #" + mi);
            add("sub " + reg_d + ", " + reg_d + ", " + reg1);
            add("sub " + reg_d + ", " + reg_d + ", " + reg1);

        } else if (isPowerOfTwo(num - 3)) {
            int mi = (int) (Math.log(num - 3) / Math.log(2));
            add("lsl " + reg_d + ", " + reg1 + ", #" + mi);
            add("add " + reg_d + ", " + reg_d + ", " + reg1);
            add("add " + reg_d + ", " + reg_d + ", " + reg1);
            add("add " + reg_d + ", " + reg_d + ", " + reg1);

        } else if (isPowerOfTwo(num + 3)) {
            int mi = (int) (Math.log(num + 3) / Math.log(2));
            add("lsl " + reg_d + ", " + reg1 + ", #" + mi);
            add("sub " + reg_d + ", " + reg_d + ", " + reg1);
            add("sub " + reg_d + ", " + reg_d + ", " + reg1);
            add("sub " + reg_d + ", " + reg_d + ", " + reg1);

        } else {
            moveImm(regsb, num);
            add("mul " + reg_d + ", " + reg1 + ", " + regsb);
        }

    }

    //判断是否2的幂次
    private boolean isPowerOfTwo(int n) {
        return n > 0 && (n & (n - 1)) == 0;
    }

    // 除法优化，仅寄存器
    private void addSdivOperation(String reg_d, String reg1, int num, boolean reverse) {
        // 专门给大家当临时寄存器，todo 会冲突所以换成sb
        String regsb = reg.SB;

        //除法？狗都不用？
        if (reverse) {  // d÷x
            if (num == 0) {
                add("mov " + reg_d + ", #0");

            } else {
                moveImm(regsb, num);
                add("sdiv " + reg_d + ", " + regsb + ", " + reg1);
            }

        } else {    //x÷d
            DivMShL msl = ChooseMultiplier(Math.abs(num), 31);
            long m = msl.m_high;
            int sh_post = msl.sh_post;

            if (Math.abs(num) == 1) {
                add("mov " + reg_d + ", " + reg1);

            } else if (isPowerOfTwo(Math.abs(num))) {
                int mi = (int) (Math.log(num) / Math.log(2));
                add("asr " + reg_d + ", " + reg1 + ", #" + (mi - 1));
                add("lsr " + reg_d + ", " + reg_d + ", #" + (32 - mi));
                add("add " + reg_d + ", " + reg_d + ", " + reg1);
                add("asr " + reg_d + ", " + reg_d + ", #" + mi);

            } else if (m < Math.pow(2, 31)) {  // q = SRA(MULSH(m, n), shpost) − XSIGN(n)

                moveImm(regsb, (int) m);
                add(new FourArm("smull", regsb, reg_d, reg1, regsb));    // !!这里应为mfhi
                add("asr " + reg_d + ", " + reg_d + ", #" + sh_post);

                // add("slti $v1, $" + reg1 + ", 0");    //若x<0, v1 = 1
                add("mov " + regsb + ", #0");
                add("cmp " + reg1 + ", " + regsb);
                add("movlt " + regsb + ", #1");
                add("add " + reg_d + ", " + reg_d + ", " + regsb);


            } else {    //q = SRA(n + MULSH(m − 2^N, n), shpost) − XSIGN(n)

//                add("mov " + regt + ", " + (int) (m - Math.pow(2, 32)));
//                add("mult $" + reg1 + ", $v1");
//                add("mfhi $" + reg_d);

                moveImm(regsb, (int) (m - Math.pow(2, 32)));
                add("smull " + regsb + ", " + reg_d + ", " + reg1 + ", " + regsb);
                add("add " + reg_d + ", " + reg_d + ", " + reg1);
                add("asr " + reg_d + ", " + reg_d + ", #" + sh_post);

                add("mov " + regsb + ", #0");
                add("cmp " + reg1 + ", " + regsb);
                add("movlt " + regsb + ", #1");
//                add("slti $v1, $" + reg1 + ", 0");    //若x<0, v1 = 1
                add("add " + reg_d + ", " + reg_d + ", " + regsb);

            }

            if (num < 0) {
                add("mov " + regsb + ", #0");
                add("sub " + reg_d + ", " + regsb + ", " + reg_d);
            }
        }

    }

    // 除法优化（含instr）
    private void addSdivOptimize(Instr instr, Ident dest) {
        String op = ((BinaryInst) instr).getOp();   // 必为sdiv
        Type t = ((BinaryInst) instr).getT();
        Value v1 = ((BinaryInst) instr).getV1();
        Value v2 = ((BinaryInst) instr).getV2();

        String reg_d = reg.T0;
        String reg1 = reg.T1;

        if (v1.isIdent() && v2.isIdent()) {
            addOp(instr, dest);     // todo 最好不用；另外记得store
            return;

        } else if (v1.isIdent() && !v2.isIdent()) {
            loadValue(reg1, v1.getIdent());
            addSdivOperation(reg_d, reg1, v2.getVal(), false);

        } else if (!v1.isIdent() && v2.isIdent()) {
            loadValue(reg1, v2.getIdent());
            addSdivOperation(reg_d, reg1, v1.getVal(), true);

        } else {
            error();
        }

        storeValue(reg_d, dest);

//        reg.freeTmp(r  eg_d);
//        reg.freeTmp(reg1);
    }

    private DivMShL ChooseMultiplier(int d, int prec /*=31*/) {
        final int N = 32;
        int l = (int) Math.ceil(Math.log(d) / Math.log(2));
        int sh_post = l;
        long m_low = (long) Math.floor(Math.pow(2, N + l) / d);
        long m_high = (long) Math.floor((Math.pow(2, N + l) + Math.pow(2, N + l - prec)) / d);

        while ((Math.floor(m_low / 2) < Math.floor(m_high / 2)) && sh_post > 0) {
            m_low = (long) Math.floor(m_low / 2);
            m_high = (long) Math.floor(m_high / 2);
            sh_post -= 1;
        }

        return new DivMShL(m_high, sh_post, l);
    }

    //取余数优化: a % b 翻译为 a - a / b * b
    // 调用时均为reg_d=T0, reg1=T1！因此可自由使用reg2
    private void addSremOperation(String reg_d, String reg1, int num, boolean reverse) {

        // 统一申请公用
        String reg2 = reg.T2;
//        String regsb = reg.SB;貌似用不到

        if (reverse) {
            if (num == 0) {
                add("mov " + reg_d + ", #0");

            } else {
//                add("mov v1, " + num);
//                add("div $v1, $" + reg1);
//                add("mfhi $" + reg_d);

                moveImm(reg2, num);
                add(new ThreeArm("sdiv", reg_d, reg2, reg1));
            }

        }
        // 此后！必不为reverse
        else {
            if (num == 1) {
                add("mov " + reg_d + ", #0");

            } else {

                //todo 完全展开！addSdivOperation(regsb, reg1, num, reverse);    // 不能用"v1"当reg_d！
                {

                    DivMShL msl = ChooseMultiplier(Math.abs(num), 31);
                    long m = msl.m_high;
                    int sh_post = msl.sh_post;

                    if (Math.abs(num) == 1) {
                        add("mov " + reg_d + ", " + reg1);

                    } else if (isPowerOfTwo(Math.abs(num))) {
                        int mi = (int) (Math.log(num) / Math.log(2));
                        add("asr " + reg_d + ", " + reg1 + ", #" + (mi - 1));
                        add("lsr " + reg_d + ", " + reg_d + ", #" + (32 - mi));
                        add("add " + reg_d + ", " + reg_d + ", " + reg1);
                        add("asr " + reg_d + ", " + reg_d + ", #" + mi);

                    } else if (m < Math.pow(2, 31)) {  // q = SRA(MULSH(m, n), shpost) − XSIGN(n)

                        moveImm(reg2, (int) m);
                        add(new FourArm("smull", reg2, reg_d, reg1, reg2));    // !!这里应为mfhi
                        add("asr " + reg_d + ", " + reg_d + ", #" + sh_post);

                        // add("slti $v1, $" + reg1 + ", 0");    //若x<0, v1 = 1
                        add("mov " + reg2 + ", #0");
                        add("cmp " + reg1 + ", " + reg2);
                        add("movlt " + reg2 + ", #1");
                        add("add " + reg_d + ", " + reg_d + ", " + reg2);


                    } else {    //q = SRA(n + MULSH(m − 2^N, n), shpost) − XSIGN(n)

                        moveImm(reg2, (int) (m - Math.pow(2, 32)));
                        add("smull " + reg2 + ", " + reg_d + ", " + reg1 + ", " + reg2);
                        add("add " + reg_d + ", " + reg_d + ", " + reg1);
                        add("asr " + reg_d + ", " + reg_d + ", #" + sh_post);

                        add("mov " + reg2 + ", #0");
                        add("cmp " + reg1 + ", " + reg2);
                        add("movlt " + reg2 + ", #1");
                        add("add " + reg_d + ", " + reg_d + ", " + reg2);

                    }

                    if (num < 0) {
                        add("mov " + reg2 + ", #0");
                        add("sub " + reg_d + ", " + reg2 + ", " + reg_d);
                    }
                }
//
//                add("mov v1, " + num);
//                add("mult $" + reg_d + ", $v1");
//                add("mflo $v1");
//                add("sub $" + reg_d + ", " + reg1 + ", $v1");

                addMulOperation(reg2, reg_d, num);     // 注意!须保证regd与reg1不同
                add(new ThreeArm("sub", reg_d, reg1, reg2));
            }
        }

    }
}
