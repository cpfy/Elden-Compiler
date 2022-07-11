package mipsCode;

import midCode.MidCodeItem;
import midCode.MidCodeList;
import symbolTable.IntegerTable;
import symbolTable.Table;
import symbolTable.items.FunctionItem;
import symbolTable.items.IntegerItem;

import java.io.PrintStream;
import java.nio.file.Watchable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Stack;

public class MipsCodeGenerator {
    private Table table = new Table();
    private MidCodeList midCodeList;
    private ArrayList<String> strings;
    private MipsCodeList mipsCodeList = new MipsCodeList();
    private IntegerTable globalTable = new IntegerTable(null);
    private int globalAddr = 0;

    private boolean isFirstLun = true;

    public MipsCodeGenerator(MidCodeList midCodeList, ArrayList<String> strings) {
        this.midCodeList = midCodeList;
        this.strings = strings;
        generateMipsCode();
    }

    private void addMipsCode(MipsCodeType type, String x, String y, String z) {
        if (isFirstLun) {
            return;
        }
        mipsCodeList.addMipsCode(type, x, y, z);
    }

    private void addMipsCode(MipsCodeType type, String x, String y, String z, int i) {
        if (isFirstLun) {
            return;
        }
        mipsCodeList.addMipsCode(type, x, y, z, i);
    }

    private void storeValue(String name, String regName) {
        int addr;
        addr = table.getAddr(name);
        if (addr == -1) {
            if (globalTable.searchInteger(name) == null) {
                table.addInteger(new IntegerItem(name, false, new ArrayList<>(), null));
                addr = table.getAddr(name);
                addMipsCode(MipsCodeType.sw, regName, "$fp", null, -4 * addr);
                System.out.println("0001");
            }
            else {
                addr = globalTable.searchInteger(name).getAddr();
                addMipsCode(MipsCodeType.sw, regName, "$gp", null, 4 * addr);
            }
        }
        else {
            addr = table.getAddr(name);
            addMipsCode(MipsCodeType.sw, regName, "$fp", null, -4 * addr); //todo
            System.out.println("0002");
        }
    }

    private boolean loadValue(String name, String regName) {
        int addr;
        addr = table.getAddr(name);
        if (addr != -1) {
            addMipsCode(MipsCodeType.lw, regName, "$fp", null, -4 * addr); //todo
            return true;
        }
        else {
            if (globalTable.searchInteger(name) != null) {
                addr = globalTable.searchInteger(name).getAddr();
                addMipsCode(MipsCodeType.lw, regName, "$gp", null, 4 * addr);
                return true;
            }
        }
        return false;
    }

    private void generateMipsCode() {
        mipsCodeList.addMipsCode(MipsCodeType.dataSeg, null, null, null);

        for (int i = 0; i < strings.size(); i++) {
            mipsCodeList.addMipsCode(MipsCodeType.asciizSeg, "s_" + i, strings.get(i), null);
        }
        //todo

        mipsCodeList.addMipsCode(MipsCodeType.textSeg, null, null, null);
        genPre();
        isFirstLun = false;
        gen();

    }

    private void genPre() {
        boolean get1, get2, isFirstFunc = true;
        LinkedList<MidCodeItem> midCodeItems = midCodeList.getMidCodeItems();
        for (int i = 0; i < midCodeItems.size(); i++) {
            MidCodeItem mc = midCodeItems.get(i);
            switch (mc.getType()) {
                case FUNC: {
                    isFirstFunc = false;
                    table.addFunc(new FunctionItem(mc.getZz(), mc.getXx(), null));
                    table.newFunc();
                    addMipsCode(MipsCodeType.label, null, null, mc.getZz());
                    if (mc.getZz().equals("main")) {
                        //int len = table.getFuncLen(mc.getZz());
                        addMipsCode(MipsCodeType.moveop, "$sp", null, "$fp");
                        //addMipsCode(MipsCodeType.addi, "$sp", String.valueOf(-4 * len - 8), "$sp");
                    }
                    break;
                }
                case LABEL: {
                    if (mc.getYy().equals("in")) {
                        table.newBlock();
                    }
                    else if (mc.getYy().equals("out")) {
                        table.deleteBlock();
                        //todo
                    }
                    break;
                }
                case CONST: {
                    if (isFirstFunc) {
                        globalTable.addInteger(new IntegerItem(mc.getZz(), true, new ArrayList<>(), null));
                    }
                    else {
                        table.addInteger(new IntegerItem(mc.getZz(), true, new ArrayList<>(), null));
                    }
                    if (mc.getXx() != null) {
                        get1 = loadValue(mc.getXx(), "$t0");
                        if (get1) {
                            storeValue(mc.getZz(), "$t0");
                        }
                        else {
                            addMipsCode(MipsCodeType.li, mc.getXx(), null, "$t0");
                            storeValue(mc.getZz(), "$t0");
                        }
                    }
                    System.out.println(123);
                    break;
                }
                case VAR: {
                    if (isFirstFunc) {
                        globalTable.addInteger(new IntegerItem(mc.getZz(), false, new ArrayList<>(), null));
                    }
                    else {
                        table.addInteger(new IntegerItem(mc.getZz(), false, new ArrayList<>(), null));
                    }
                    if (mc.getXx() != null) {
                        get1 = loadValue(mc.getXx(), "$t0");
                        if (get1) {
                            storeValue(mc.getZz(), "$t0");
                        }
                        else {
                            addMipsCode(MipsCodeType.li, mc.getXx(), null, "$t0");
                            storeValue(mc.getZz(), "$t0");
                        }
                    }
                    break;
                }
                case ARRAY: {
                    ArrayList<Integer> dims = new ArrayList<>();
                    dims.add(Integer.parseInt(mc.getXx()));
                    if (mc.getYy() != null) {
                        dims.add(Integer.parseInt(mc.getYy()));
                    }
                    if (isFirstFunc) {
//                        globalTable.addInteger(new IntegerItem(mc.getZz(), false, dims, null));
                    }
                    else {
                        table.addInteger(new IntegerItem(mc.getZz(), false, dims, null));
                    }
                    break;
                }
                case PARAM: {
                    if (mc.getXx().equals("0")) {
                        table.addInteger(new IntegerItem(mc.getZz(), false, new ArrayList<>(), null));
                    }
                    else if (mc.getXx().equals("1")){
                        table.addInteger(new IntegerItem(mc.getZz(), false, new ArrayList<>(), null, true));
                        //todo 数组传参
                    }
                    else if (mc.getXx().equals("2")){
                        table.addInteger(new IntegerItem(mc.getZz(), false, new ArrayList<>(), null, true));
                        //todo 数组传参
                    }
                    break;
                }
                case PLUSOP: {
                    storeValue(mc.getZz(), "$t2");
                    break;
                }
                case MINUOP: {
                    storeValue(mc.getZz(), "$t2");
                    break;
                }
                case NEQOP: {
                    get1 = loadValue(mc.getXx(), "$t0");
                    get2 = loadValue(mc.getYy(), "$t1");
                    if (get1 && get2) {
                        addMipsCode(MipsCodeType.sne, "$t0", "$t1", "$t2");
                    }
                    else  if (get1 && !get2) {
                        addMipsCode(MipsCodeType.sne, "$t0", String.valueOf(Integer.parseInt(mc.getYy())), "$t2");
                    }
                    else if (!get1 && get2) {
                        addMipsCode(MipsCodeType.sne, "$t1", String.valueOf(Integer.parseInt(mc.getXx())), "$t2");
                    }
                    else {
                        int ans = 0;
                        if (Integer.parseInt(mc.getXx()) != Integer.parseInt(mc.getYy())) {
                            ans = 1;
                        }
                        addMipsCode(MipsCodeType.li, String.valueOf(ans), null, "$t2");
                    }
                    storeValue(mc.getZz(), "$t2");
                    break;
                }
                case EQLOP: {
                    get1 = loadValue(mc.getXx(), "$t0");
                    get2 = loadValue(mc.getYy(), "$t1");
                    if (get1 && get2) {
                        addMipsCode(MipsCodeType.seq, "$t0", "$t1", "$t2");
                    }
                    else  if (get1 && !get2) {
                        addMipsCode(MipsCodeType.seq, "$t0", String.valueOf(Integer.parseInt(mc.getYy())), "$t2");
                    }
                    else if (!get1 && get2) {
                        addMipsCode(MipsCodeType.seq, "$t1", String.valueOf(Integer.parseInt(mc.getXx())), "$t2");
                    }
                    else {
                        int ans = 0;
                        if (Integer.parseInt(mc.getXx()) == Integer.parseInt(mc.getYy())) {
                            ans = 1;
                        }
                        addMipsCode(MipsCodeType.li, String.valueOf(ans), null, "$t2");
                    }
                    storeValue(mc.getZz(), "$t2");
                    break;
                }
                case LEQOP: {
                    get1 = loadValue(mc.getXx(), "$t0");
                    get2 = loadValue(mc.getYy(), "$t1");
                    if (get1 && get2) {
                        addMipsCode(MipsCodeType.sle, "$t0", "$t1", "$t2");
                    }
                    else  if (get1 && !get2) {
                        addMipsCode(MipsCodeType.sle, "$t0", String.valueOf(Integer.parseInt(mc.getYy())), "$t2");
                    }
                    else if (!get1 && get2) {
                        addMipsCode(MipsCodeType.sgt, "$t1", String.valueOf(Integer.parseInt(mc.getXx())), "$t2");
                    }
                    else {
                        int ans = 0;
                        if (Integer.parseInt(mc.getXx()) <= Integer.parseInt(mc.getYy())) {
                            ans = 1;
                        }
                        addMipsCode(MipsCodeType.li, String.valueOf(ans), null, "$t2");
                    }
                    storeValue(mc.getZz(), "$t2");
                    break;
                }
                case LSSOP: {
                    get1 = loadValue(mc.getXx(), "$t0");
                    get2 = loadValue(mc.getYy(), "$t1");
                    if (get1 && get2) {
                        addMipsCode(MipsCodeType.slt, "$t0", "$t1", "$t2");
                    }
                    else  if (get1 && !get2) {
                        addMipsCode(MipsCodeType.slti, "$t0", String.valueOf(Integer.parseInt(mc.getYy())), "$t2");
                    }
                    else if (!get1 && get2) {
                        addMipsCode(MipsCodeType.sge, "$t1", String.valueOf(Integer.parseInt(mc.getXx())), "$t2");
                    }
                    else {
                        int ans = 0;
                        if (Integer.parseInt(mc.getXx()) < Integer.parseInt(mc.getYy())) {
                            ans = 1;
                        }
                        addMipsCode(MipsCodeType.li, String.valueOf(ans), null, "$t2");
                    }
                    storeValue(mc.getZz(), "$t2");
                    break;
                }
                case GEQOP: {
                    get1 = loadValue(mc.getXx(), "$t0");
                    get2 = loadValue(mc.getYy(), "$t1");
                    if (get1 && get2) {
                        addMipsCode(MipsCodeType.sge, "$t0", "$t1", "$t2");
                    }
                    else  if (get1 && !get2) {
                        addMipsCode(MipsCodeType.sge, "$t0", String.valueOf(Integer.parseInt(mc.getYy())), "$t2");
                    }
                    else if (!get1 && get2) {
                        addMipsCode(MipsCodeType.slti, "$t1", String.valueOf(Integer.parseInt(mc.getXx())), "$t2");
                    }
                    else {
                        int ans = 0;
                        if (Integer.parseInt(mc.getXx()) >= Integer.parseInt(mc.getYy())) {
                            ans = 1;
                        }
                        addMipsCode(MipsCodeType.li, String.valueOf(ans), null, "$t2");
                    }
                    storeValue(mc.getZz(), "$t2");
                    break;
                }
                case GREOP: {
                    get1 = loadValue(mc.getXx(), "$t0");
                    get2 = loadValue(mc.getYy(), "$t1");
                    if (get1 && get2) {
                        addMipsCode(MipsCodeType.sgt, "$t0", "$t1", "$t2");
                    }
                    else  if (get1 && !get2) {
                        addMipsCode(MipsCodeType.sgt, "$t0", String.valueOf(Integer.parseInt(mc.getYy())), "$t2");
                    }
                    else if (!get1 && get2) {
                        addMipsCode(MipsCodeType.sle, "$t1", String.valueOf(Integer.parseInt(mc.getXx())), "$t2");
                        addMipsCode(MipsCodeType.sub, "$0", "$t2", "$t2");
                    }
                    else {
                        int ans = 0;
                        if (Integer.parseInt(mc.getXx()) > Integer.parseInt(mc.getYy())) {
                            ans = 1;
                        }
                        addMipsCode(MipsCodeType.li, String.valueOf(ans), null, "$t2");
                    }
                    storeValue(mc.getZz(), "$t2");
                    break;
                }
                case MULTOP: {
                    storeValue(mc.getZz(), "$t2");
                    break;
                }
                case DIVOP: {
                    storeValue(mc.getZz(), "$t2");
                    break;
                }
                case MODOP: {
                    storeValue(mc.getZz(), "$t2");
                    break;
                }
                case ASSIGNOP: {
                    get1 = loadValue(mc.getXx(), "$t0");
                    if (get1) {
                        storeValue(mc.getZz(), "$t0");
                    }
                    else {
                        addMipsCode(MipsCodeType.li, mc.getXx(), null, "$t0");
                        storeValue(mc.getZz(), "$t0");
                    }
                    break;
                }
                case PUSH: {

                    break;
                }
                case CALL: {

                    break;
                }
                case RET: {
                    break;
                }
                case RETVALUE: {
                    storeValue(mc.getZz(), "$v0");
                    break;
                }
                case SCAN: {
                    storeValue(mc.getZz(), "$v0");
                    break;
                }
                case PRINT: {
                    break;
                }
                case GETARRAY: {
                    storeValue(mc.getZz(), "$t1");
                    break;
                }
                case PUTARRAY: {
                    break;
                }
                default:
            }
        }
        table.endMain();
        System.out.println("\n\n\n");
    }

    private void gen() {
        boolean get1, get2, isFirstFunc = true;
        Stack<MidCodeItem> pushStack = new Stack<>();
        FunctionItem functionItem = null;
        LinkedList<MidCodeItem> midCodeItems = midCodeList.getMidCodeItems();

        addMipsCode(MipsCodeType.addi, "$sp", String.valueOf(-200), "$fp"); // todo 傻逼写法

        for (int i = 0; i < midCodeItems.size(); i++) {
            MidCodeItem mc = midCodeItems.get(i);
            switch (mc.getType()) {
                case FUNC: {
                    if (!isFirstFunc) {
                        addMipsCode(MipsCodeType.jr, "$ra", null, null);
                    }
                    else {
                        addMipsCode(MipsCodeType.j, "main", null, null);
                    }
                    isFirstFunc = false;
                    int len = table.getFuncLen(mc.getZz());
                    functionItem = new FunctionItem(mc.getZz(), mc.getXx(), null);
                    functionItem.setLen(len);
                    table.addFunc(functionItem);
                    table.newFunc();
                    addMipsCode(MipsCodeType.label, null, null, mc.getZz());
                    if (mc.getZz().equals("main")) {

                        System.out.println("\t\t" + len);
                        addMipsCode(MipsCodeType.moveop, "$sp", null, "$fp");
                        addMipsCode(MipsCodeType.addi, "$sp", String.valueOf(-4 * len - 8), "$sp");
                    }
                    break;
                }
                case LABEL: {
                    if (mc.getYy().equals("in")) {
                        table.newBlock();
                    }
                    else if (mc.getYy().equals("out")) {
                        table.deleteBlock();
                        //todo
                    }
                    break;
                }
                case CONST: {
                    if (isFirstFunc) {
                        System.out.println(123123123);
                        IntegerItem integerItem = new IntegerItem(mc.getZz(), true, new ArrayList<>(), null);
                        integerItem.setAddr(globalAddr);
                        globalAddr++;
                        globalTable.addInteger(integerItem);
                    }
                    else {
                        table.addInteger(new IntegerItem(mc.getZz(), true, new ArrayList<>(), null));
                    }
                    if (mc.getXx() != null) {
                        System.out.println(12);
                        get1 = loadValue(mc.getXx(), "$t0");
                        if (get1) {
                            storeValue(mc.getZz(), "$t0");
                        }
                        else {
                            addMipsCode(MipsCodeType.li, mc.getXx(), null, "$t0");
                            storeValue(mc.getZz(), "$t0");
                        }
                    }
                    System.out.println(123);
                    break;
                }
                case VAR: {
                    if (isFirstFunc) {
                        System.out.println(123123123);
                        IntegerItem integerItem = new IntegerItem(mc.getZz(), false, new ArrayList<>(), null);
                        integerItem.setAddr(globalAddr);
                        globalAddr++;
                        globalTable.addInteger(integerItem);
                    }
                    else {
                        table.addInteger(new IntegerItem(mc.getZz(), false, new ArrayList<>(), null));
                    }
                    if (mc.getXx() != null) {
                        System.out.println(12);
                        get1 = loadValue(mc.getXx(), "$t0");
                        if (get1) {
                            storeValue(mc.getZz(), "$t0");
                        }
                        else {
                            addMipsCode(MipsCodeType.li, mc.getXx(), null, "$t0");
                            storeValue(mc.getZz(), "$t0");
                        }
                    }
                    break;
                }
                case ARRAY: {
                    int temp = 0;
                    ArrayList<Integer> dims = new ArrayList<>();
                    dims.add(Integer.parseInt(mc.getXx()));
                    temp = dims.get(0);
                    if (mc.getYy() != null) {
                        dims.add(Integer.parseInt(mc.getYy()));
                        temp *= dims.get(1);
                    }
                    if (isFirstFunc) {
                        IntegerItem integerItem = new IntegerItem(mc.getZz(), false, dims, null);
                        globalAddr += temp;
                        integerItem.setAddr(globalAddr - 1);
                        globalTable.addInteger(integerItem);
                    }
                    else {
                        table.addInteger(new IntegerItem(mc.getZz(), false, dims, null));
                    }
                    break;
                }
                case PARAM: {
                    functionItem.addParamNum();
                    if (mc.getXx().equals("0")) {
                        table.addInteger(new IntegerItem(mc.getZz(), false, new ArrayList<>(), null));
                    }
                    else if (mc.getXx().equals("1")){
                        ArrayList<Integer> dims = new ArrayList<>();
                        dims.add(0);
                        table.addInteger(new IntegerItem(mc.getZz(), false, dims, null, true));
                        //todo 数组传参
                    }
                    else if (mc.getXx().equals("2")){
                        ArrayList<Integer> dims = new ArrayList<>();
                        dims.add(0);
                        dims.add(Integer.parseInt(mc.getYy()));
                        table.addInteger(new IntegerItem(mc.getZz(), false, dims, null, true));
                        //todo 数组传参
                    }
                    break;
                }
                case PLUSOP: {
                    get1 = loadValue(mc.getXx(), "$t0");
                    get2 = loadValue(mc.getYy(), "$t1");
                    //System.out.println(mc.getXx() + get1);
                    //System.out.println(mc.getYy() + get2);
                    if (get1 && get2) {
                        addMipsCode(MipsCodeType.add, "$t0", "$t1", "$t2");
                    }
                    else  if (get1 && !get2) {
                        addMipsCode(MipsCodeType.addi, "$t0", String.valueOf(Integer.parseInt(mc.getYy())), "$t2");
                    }
                    else if (!get1 && get2) {
                        addMipsCode(MipsCodeType.addi, "$t1", String.valueOf(Integer.parseInt(mc.getXx())), "$t2");
                    }
                    else {
                        addMipsCode(MipsCodeType.li, String.valueOf(Integer.parseInt(mc.getXx()) + Integer.parseInt(mc.getYy())),
                                null, "$t2");
                    }
                    storeValue(mc.getZz(), "$t2");
                    break;
                }
                case MINUOP: {
                    get1 = loadValue(mc.getXx(), "$t0");
                    get2 = loadValue(mc.getYy(), "$t1");
                    if (get1 && get2) {
                        addMipsCode(MipsCodeType.sub, "$t0", "$t1", "$t2");
                    }
                    else  if (get1 && !get2) {
                        addMipsCode(MipsCodeType.addi, "$t0", String.valueOf(-Integer.parseInt(mc.getYy())), "$t2");
                    }
                    else if (!get1 && get2) {
                        addMipsCode(MipsCodeType.addi, "$t1", String.valueOf(-Integer.parseInt(mc.getXx())), "$t2");
                        addMipsCode(MipsCodeType.sub, "$0", "$t2", "$t2");
                    }
                    else {
                        addMipsCode(MipsCodeType.li, String.valueOf(Integer.parseInt(mc.getXx()) - Integer.parseInt(mc.getYy())), null, "$t2");
                    }
                    storeValue(mc.getZz(), "$t2");
                    break;
                }
                case NEQOP: {
                    get1 = loadValue(mc.getXx(), "$t0");
                    get2 = loadValue(mc.getYy(), "$t1");
                    if (get1 && get2) {
                        addMipsCode(MipsCodeType.sne, "$t0", "$t1", "$t2");
                    }
                    else  if (get1 && !get2) {
                        addMipsCode(MipsCodeType.sne, "$t0", String.valueOf(Integer.parseInt(mc.getYy())), "$t2");
                    }
                    else if (!get1 && get2) {
                        addMipsCode(MipsCodeType.sne, "$t1", String.valueOf(Integer.parseInt(mc.getXx())), "$t2");
                    }
                    else {
                        int ans = 0;
                        if (Integer.parseInt(mc.getXx()) != Integer.parseInt(mc.getYy())) {
                            ans = 1;
                        }
                        addMipsCode(MipsCodeType.li, String.valueOf(ans), null, "$t2");
                    }
                    storeValue(mc.getZz(), "$t2");
                    break;
                }
                case EQLOP: {
                    get1 = loadValue(mc.getXx(), "$t0");
                    get2 = loadValue(mc.getYy(), "$t1");
                    if (get1 && get2) {
                        addMipsCode(MipsCodeType.seq, "$t0", "$t1", "$t2");
                    }
                    else  if (get1 && !get2) {
                        addMipsCode(MipsCodeType.seq, "$t0", String.valueOf(Integer.parseInt(mc.getYy())), "$t2");
                    }
                    else if (!get1 && get2) {
                        addMipsCode(MipsCodeType.seq, "$t1", String.valueOf(Integer.parseInt(mc.getXx())), "$t2");
                    }
                    else {
                        int ans = 0;
                        if (Integer.parseInt(mc.getXx()) == Integer.parseInt(mc.getYy())) {
                            ans = 1;
                        }
                        addMipsCode(MipsCodeType.li, String.valueOf(ans), null, "$t2");
                    }
                    storeValue(mc.getZz(), "$t2");
                    break;
                }
                case LEQOP: {
                    get1 = loadValue(mc.getXx(), "$t0");
                    get2 = loadValue(mc.getYy(), "$t1");
                    if (get1 && get2) {
                        addMipsCode(MipsCodeType.sle, "$t0", "$t1", "$t2");
                    }
                    else  if (get1 && !get2) {
                        addMipsCode(MipsCodeType.sle, "$t0", String.valueOf(Integer.parseInt(mc.getYy())), "$t2");
                    }
                    else if (!get1 && get2) {
                        addMipsCode(MipsCodeType.sge, "$t1", String.valueOf(Integer.parseInt(mc.getXx())), "$t2");
                    }
                    else {
                        int ans = 0;
                        if (Integer.parseInt(mc.getXx()) <= Integer.parseInt(mc.getYy())) {
                            ans = 1;
                        }
                        addMipsCode(MipsCodeType.li, String.valueOf(ans), null, "$t2");
                    }
                    storeValue(mc.getZz(), "$t2");
                    break;
                }
                case LSSOP: {
                    get1 = loadValue(mc.getXx(), "$t0");
                    get2 = loadValue(mc.getYy(), "$t1");
                    if (get1 && get2) {
                        addMipsCode(MipsCodeType.slt, "$t0", "$t1", "$t2");
                    }
                    else  if (get1 && !get2) {
                        addMipsCode(MipsCodeType.slti, "$t0", String.valueOf(Integer.parseInt(mc.getYy())), "$t2");
                    }
                    else if (!get1 && get2) {
                        addMipsCode(MipsCodeType.sgt, "$t1", String.valueOf(Integer.parseInt(mc.getXx())), "$t2");
                    }
                    else {
                        int ans = 0;
                        if (Integer.parseInt(mc.getXx()) < Integer.parseInt(mc.getYy())) {
                            ans = 1;
                        }
                        addMipsCode(MipsCodeType.li, String.valueOf(ans), null, "$t2");
                    }
                    storeValue(mc.getZz(), "$t2");
                    break;
                }
                case GEQOP: {
                    get1 = loadValue(mc.getXx(), "$t0");
                    get2 = loadValue(mc.getYy(), "$t1");
                    if (get1 && get2) {
                        addMipsCode(MipsCodeType.sge, "$t0", "$t1", "$t2");
                    }
                    else  if (get1 && !get2) {
                        addMipsCode(MipsCodeType.sge, "$t0", String.valueOf(Integer.parseInt(mc.getYy())), "$t2");
                    }
                    else if (!get1 && get2) {
                        addMipsCode(MipsCodeType.sle, "$t1", String.valueOf(Integer.parseInt(mc.getXx())), "$t2");
                    }
                    else {
                        int ans = 0;
                        if (Integer.parseInt(mc.getXx()) >= Integer.parseInt(mc.getYy())) {
                            ans = 1;
                        }
                        addMipsCode(MipsCodeType.li, String.valueOf(ans), null, "$t2");
                    }
                    storeValue(mc.getZz(), "$t2");
                    break;
                }
                case GREOP: {
                    get1 = loadValue(mc.getXx(), "$t0");
                    get2 = loadValue(mc.getYy(), "$t1");
                    if (get1 && get2) {
                        addMipsCode(MipsCodeType.sgt, "$t0", "$t1", "$t2");
                    }
                    else  if (get1 && !get2) {
                        addMipsCode(MipsCodeType.sgt, "$t0", String.valueOf(Integer.parseInt(mc.getYy())), "$t2");
                    }
                    else if (!get1 && get2) {
                        addMipsCode(MipsCodeType.slti, "$t1", String.valueOf(Integer.parseInt(mc.getXx())), "$t2");
                    }
                    else {
                        int ans = 0;
                        if (Integer.parseInt(mc.getXx()) > Integer.parseInt(mc.getYy())) {
                            ans = 1;
                        }
                        addMipsCode(MipsCodeType.li, String.valueOf(ans), null, "$t2");
                    }
                    storeValue(mc.getZz(), "$t2");
                    break;
                }
                case BZ: {
                    get1 = loadValue(mc.getXx(), "$t0");
                    if (get1) {
                        addMipsCode(MipsCodeType.beq, "$t0", "0", mc.getZz());
                    }
                    else {
                        if (Integer.parseInt(mc.getXx()) == 0)
                        addMipsCode(MipsCodeType.j, mc.getZz(), null, null);
                    }
                    break;
                }
                case GOTO: {
                    addMipsCode(MipsCodeType.j, mc.getZz(), null, null);
                    break;
                }
                case JUMP: {
                    addMipsCode(MipsCodeType.label, null, null, mc.getZz());
                    break;
                }
                case MULTOP: {
                    get1 = loadValue(mc.getXx(), "$t0");
                    get2 = loadValue(mc.getYy(), "$t1");
                    if (get1 && get2) {
                        addMipsCode(MipsCodeType.mult, "$t0", "$t1", null);
                    }
                    else  if (get1 && !get2) {
                        addMipsCode(MipsCodeType.li, String.valueOf(Integer.parseInt(mc.getYy())), null, "$t1");
                        addMipsCode(MipsCodeType.mult, "$t0", "$t1", null);
                    }
                    else if (!get1 && get2) {
                        addMipsCode(MipsCodeType.li, String.valueOf(Integer.parseInt(mc.getXx())), null, "$t0");
                        addMipsCode(MipsCodeType.mult, "$t0", "$t1", null);
                    }
                    else {
                        addMipsCode(MipsCodeType.li, String.valueOf(Integer.parseInt(mc.getXx())), null, "$t0");
                        addMipsCode(MipsCodeType.li, String.valueOf(Integer.parseInt(mc.getYy())), null, "$t1");
                        addMipsCode(MipsCodeType.mult, "$t0", "$t1", null);
                    }
                    addMipsCode(MipsCodeType.mflo, null, null, "$t2");
                    storeValue(mc.getZz(), "$t2");
                    break;
                }
                case DIVOP: {
                    get1 = loadValue(mc.getXx(), "$t0");
                    get2 = loadValue(mc.getYy(), "$t1");
                    if (get1 && get2) {
                        addMipsCode(MipsCodeType.divop, "$t0", "$t1", null);
                    }
                    else  if (get1 && !get2) {
                        addMipsCode(MipsCodeType.li, String.valueOf(Integer.parseInt(mc.getYy())), null, "$t1");
                        addMipsCode(MipsCodeType.divop, "$t0", "$t1", null);
                    }
                    else if (!get1 && get2) {
                        addMipsCode(MipsCodeType.li, String.valueOf(Integer.parseInt(mc.getXx())), null, "$t0");
                        addMipsCode(MipsCodeType.divop, "$t0", "$t1", null);
                    }
                    else {
                        addMipsCode(MipsCodeType.li, String.valueOf(Integer.parseInt(mc.getXx())), null, "$t0");
                        addMipsCode(MipsCodeType.li, String.valueOf(Integer.parseInt(mc.getYy())), null, "$t1");
                        addMipsCode(MipsCodeType.divop, "$t0", "$t1", null);
                    }
                    addMipsCode(MipsCodeType.mflo, null, null, "$t2");
                    storeValue(mc.getZz(), "$t2");
                    break;
                }
                case MODOP: {
                    get1 = loadValue(mc.getXx(), "$t0");
                    get2 = loadValue(mc.getYy(), "$t1");
                    if (get1 && get2) {
                        addMipsCode(MipsCodeType.divop, "$t0", "$t1", null);
                    }
                    else  if (get1 && !get2) {
                        addMipsCode(MipsCodeType.li, String.valueOf(Integer.parseInt(mc.getYy())), null, "$t1");
                        addMipsCode(MipsCodeType.divop, "$t0", "$t1", null);
                    }
                    else if (!get1 && get2) {
                        addMipsCode(MipsCodeType.li, String.valueOf(Integer.parseInt(mc.getXx())), null, "$t0");
                        addMipsCode(MipsCodeType.divop, "$t0", "$t1", null);
                    }
                    else {
                        addMipsCode(MipsCodeType.li, String.valueOf(Integer.parseInt(mc.getXx())), null, "$t0");
                        addMipsCode(MipsCodeType.li, String.valueOf(Integer.parseInt(mc.getYy())), null, "$t1");
                        addMipsCode(MipsCodeType.divop, "$t0", "$t1", null);
                    }
                    addMipsCode(MipsCodeType.mfhi, null, null, "$t2");
                    storeValue(mc.getZz(), "$t2");
                    break;
                }
                case ASSIGNOP: {
                    get1 = loadValue(mc.getXx(), "$t0");
                    if (get1) {
                        storeValue(mc.getZz(), "$t0");
                    }
                    else {
                        addMipsCode(MipsCodeType.li, mc.getXx(), null, "$t0");
                        storeValue(mc.getZz(), "$t0");
                    }
                    break;
                }
                case PUSH: {
                    pushStack.push(mc);
                    break;
                }
                case CALL: {
                    int paramNum = table.getParamNum(mc.getZz());
                    while (paramNum > 0) {
                        paramNum--;
                        MidCodeItem tmpMc = pushStack.pop();
                        String[] list = tmpMc.getZz().split("@");
                        if (table.getAddr(list[0]) != -1 && table.isPointer(list[0])) {
                            loadValue(list[0], "$t0");
                            if (list.length > 1) {
                                get1 = loadValue(list[1], "$t1");
                                if (!get1) {
                                    addMipsCode(MipsCodeType.li, String.valueOf(Integer.parseInt(list[1]) * table.getDim2(list[0])), null, "$t1");
                                }
                                else {
                                    addMipsCode(MipsCodeType.mult, "$t1", String.valueOf(table.getDim2(list[0])), null);
                                    addMipsCode(MipsCodeType.mflo, null, null, "$t1");
                                }
                                addMipsCode(MipsCodeType.sll, "$t1", "2", "$t1");
                                addMipsCode(MipsCodeType.sub, "$t0", "$t1", "$t0");
                            }
                        }
                        else if (!isPointer(list[0])) { //一般变量
                            get1 = loadValue(tmpMc.getZz(), "$t0");
                            if (!get1) {
                                addMipsCode(MipsCodeType.li, tmpMc.getZz(), null, "$t0");
                            }
                        }
                        else {
                            //数组指针
                            int addr = table.getAddr(list[0]);
                            if (addr != -1) {                   //局部数组
                                addMipsCode(MipsCodeType.addi, "$fp", String.valueOf(-4 * addr), "$t0");
                                if (list.length > 1) {
                                    get1 = loadValue(list[1], "$t1");
                                    if (!get1) {
                                        addMipsCode(MipsCodeType.li, String.valueOf(Integer.parseInt(list[1]) * table.getDim2(list[0])), null, "$t1");
                                    }
                                    else {
                                        addMipsCode(MipsCodeType.mult, "$t1", String.valueOf(table.getDim2(list[0])), null);
                                        addMipsCode(MipsCodeType.mflo, null, null, "$t1");
                                    }
                                    addMipsCode(MipsCodeType.sll, "$t1", "2", "$t1");
                                    addMipsCode(MipsCodeType.sub, "$t0", "$t1", "$t0");
                                }
                            }
                            else {                              //全局数组
                                //todo
                                addr = globalTable.searchInteger(list[0]).getAddr();
                                addMipsCode(MipsCodeType.addi, "$gp", String.valueOf(4 * addr), "$t0");
                                if (list.length > 1) {
                                    get1 = loadValue(list[1], "$t1");
                                    if (!get1) {
                                        addMipsCode(MipsCodeType.li, String.valueOf(Integer.parseInt(list[1]) * globalTable.searchInteger((list[0])).getDim2()), null, "$t1");
                                    }
                                    else {
                                        addMipsCode(MipsCodeType.mult, "$t1", String.valueOf(globalTable.searchInteger((list[0])).getDim2()), null);
                                        addMipsCode(MipsCodeType.mflo, null, null, "$t1");
                                    }
                                    addMipsCode(MipsCodeType.sll, "$t1", "2", "$t1");
                                    addMipsCode(MipsCodeType.sub, "$t0", "$t1", "$t0");
                                }
                            }
                        }
                        addMipsCode(MipsCodeType.sw, "$t0", "$sp", null, -4 * paramNum);
                    }
                    addMipsCode(MipsCodeType.addi, "$sp", String.valueOf(-4 * table.getFuncLen(mc.getZz()) - 8), "$sp");
                    addMipsCode(MipsCodeType.sw, "$ra", "$sp", null, 4);
                    addMipsCode(MipsCodeType.sw, "$fp", "$sp", null, 8);
                    addMipsCode(MipsCodeType.addi, "$sp", String.valueOf(4 * table.getFuncLen(mc.getZz()) + 8), "$fp");
                    addMipsCode(MipsCodeType.jal, mc.getZz(), null, null);
                    addMipsCode(MipsCodeType.lw, "$fp", "$sp", null, 8);
                    addMipsCode(MipsCodeType.lw, "$ra", "$sp", null, 4);
                    addMipsCode(MipsCodeType.addi, "$sp", String.valueOf(4 * table.getFuncLen(mc.getZz()) + 8), "$sp");
                    break;
                }
                case RET: {
                    if (mc.getZz() != null) {
                        get1 = loadValue(mc.getZz(), "$v0");
                        if (!get1) {
                            addMipsCode(MipsCodeType.li, mc.getZz(), null, "$v0");
                        }
                    }
                    addMipsCode(MipsCodeType.jr, "$ra", null, null);
                    break;
                }
                case RETVALUE: {
                    storeValue(mc.getZz(), "$v0");
                    break;
                }
                case SCAN: {
                    addMipsCode(MipsCodeType.li, String.valueOf(5), null, "$v0");
                    addMipsCode(MipsCodeType.syscall, null, null,null);
                    storeValue(mc.getZz(), "$v0");
                    break;
                }
                case PRINT: {
                    if (mc.getZz().charAt(0) == '\"') {
                        int j;
                        String s = mc.getZz().replace("\"", "");
                        for (j = 0; j < strings.size(); j++) {
                            if (strings.get(j).equals(s)) {
                                break;
                            }
                        }
                        addMipsCode(MipsCodeType.la, "s_" + j, null, "$a0");
                        addMipsCode(MipsCodeType.li, String.valueOf(4), null, "$v0");
                        addMipsCode(MipsCodeType.syscall, null, null, null);
                    }
                    else {
                        get1 = loadValue(mc.getZz(), "$a0");
                        if (!get1) {
                            addMipsCode(MipsCodeType.li, mc.getZz(), null, "$a0");
                        }
                        addMipsCode(MipsCodeType.li, String.valueOf(1), null, "$v0");
                        addMipsCode(MipsCodeType.syscall, null, null, null);
                    }
                    break;
                }
                case GETARRAY: {
                    get1 = loadValue(mc.getXx(), "$t0");
                    int addr = table.getAddr(mc.getYy());
                    if (addr != -1) {
                        if (!table.isPointer(mc.getYy())) {
                            if (get1) {
                                addMipsCode(MipsCodeType.addi, "$fp", String.valueOf(-4 * addr), "$t1");
                                addMipsCode(MipsCodeType.sll, "$t0", "2", "$t0");
                                addMipsCode(MipsCodeType.sub, "$t1", "$t0", "$t1");
                                addMipsCode(MipsCodeType.lw, "$t1", "$t1", null, 0);
                            } else {
                                addMipsCode(MipsCodeType.lw, "$t1", "$fp", null, -4 * (addr + Integer.parseInt(mc.getXx())));
                            }
                        }
                        else {
                            loadValue(mc.getYy(), "$t1");
                            if (get1) {
                                addMipsCode(MipsCodeType.sll, "$t0", "2", "$t0");
                                addMipsCode(MipsCodeType.sub, "$t1", "$t0", "$t1");
                                addMipsCode(MipsCodeType.lw, "$t1", "$t1", null, 0);
                            } else {
                                addMipsCode(MipsCodeType.lw, "$t1", "$t1", null, -4 * (Integer.parseInt(mc.getXx())));
                            }
                        }
                    }
                    else {
                        addr = globalTable.searchInteger(mc.getYy()).getAddr();
                        if (get1) {
                            addMipsCode(MipsCodeType.addi, "$gp", String.valueOf(4 * addr), "$t1");
                            addMipsCode(MipsCodeType.sll, "$t0", "2", "$t0");
                            addMipsCode(MipsCodeType.sub, "$t1", "$t0", "$t1");
                            addMipsCode(MipsCodeType.lw, "$t1", "$t1", null, 0);
                        } else {
                            addMipsCode(MipsCodeType.lw, "$t1", "$gp", null, 4 * (addr - Integer.parseInt(mc.getXx())));
                        }
                    }
                    storeValue(mc.getZz(), "$t1");
                    break;
                }
                case PUTARRAY: {
                    get1 = loadValue(mc.getYy(), "$t0");
                    int addr = table.getAddr(mc.getZz());
                    get2 = loadValue(mc.getXx(), "$t2");
                    if (addr != -1) {
                        if (!table.isPointer(mc.getZz())) {
                            if (!get2) {
                                addMipsCode(MipsCodeType.li, mc.getXx(), null, "$t2");
                            }
                            if (get1) {
                                addMipsCode(MipsCodeType.addi, "$fp", String.valueOf(-4 * addr), "$t1");
                                addMipsCode(MipsCodeType.sll, "$t0", "2", "$t0");
                                addMipsCode(MipsCodeType.sub, "$t1", "$t0", "$t1");
                                addMipsCode(MipsCodeType.sw, "$t2", "$t1", null, 0);
                            } else {
                                addMipsCode(MipsCodeType.sw, "$t2", "$fp", null, -4 * (addr + Integer.parseInt(mc.getYy())));
                            }
                        }
                        else {
                            loadValue(mc.getZz(), "$t1");
                            if (!get2) {
                                addMipsCode(MipsCodeType.li, mc.getXx(), null, "$t2");
                            }
                            if (get1) {
                                addMipsCode(MipsCodeType.sll, "$t0", "2", "$t0");
                                addMipsCode(MipsCodeType.sub, "$t1", "$t0", "$t1");
                                addMipsCode(MipsCodeType.sw, "$t2", "$t1", null, 0);
                            } else {
                                addMipsCode(MipsCodeType.sw, "$t2", "$t1", null, -4 * (Integer.parseInt(mc.getYy())));
                            }
                        }
                    }
                    else {
                        addr = globalTable.searchInteger(mc.getZz()).getAddr();
                        if (!get2) {
                            addMipsCode(MipsCodeType.li, mc.getXx(), null, "$t2");
                        }
                        if (get1) {
                            addMipsCode(MipsCodeType.addi, "$gp", String.valueOf(4 * addr), "$t1");
                            addMipsCode(MipsCodeType.sll, "$t0", "2", "$t0");
                            addMipsCode(MipsCodeType.sub, "$t1", "$t0", "$t1");
                            addMipsCode(MipsCodeType.sw, "$t2", "$t1", null, 0);
                        } else {
                            addMipsCode(MipsCodeType.sw, "$t2", "$gp", null, 4 * (addr - Integer.parseInt(mc.getYy())));
                        }
                    }
                    break;
                }
                default:
                    throw new IllegalStateException("Unexpected value: " + mc.getType());
            }
        }
    }

    private boolean isPointer(String s) {
        if (table.getAddr(s) == -1 && globalTable.searchInteger(s) == null) {
            return false;
        }
        else if (table.getAddr(s) != -1 && (table.getDimNum(s) > 0 || table.isPointer(s))) {
            return true;
        }
        else if (globalTable.searchInteger(s) != null && (globalTable.searchInteger(s).getDims().size() > 0 || globalTable.searchInteger(s).isPointer())) {
            return true;
        }
        return false;
    }

    public void output(PrintStream p) {
        mipsCodeList.output(p);
    }

}
