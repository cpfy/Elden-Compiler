package llvm;

import backend.backendTable.GenerateTable;

import java.util.ArrayList;
import java.util.HashMap;

public class Function {

    private FuncHeader funcheader;  // 函数具体属性
    private ArrayList<Block> blocklist;  // 函数内所有基本块
    private int blocknum;    // 函数内基本块个数
    public boolean voidreturn;  //返回值是否为空

    public Function(FuncHeader funcheader) {
        this.funcheader = funcheader;
        this.blocklist = new ArrayList<>();
    }

    /*** add start by sujunzhe ***/
    private int funcSize = 0;       //函数需要分配的大小
    private HashMap<String, Integer> offsetTable; //记录变量名对应的偏移量

    public void addVar(String s, int n) {
        offsetTable.put(s, funcSize);
        funcSize += n;
    }

    public void initOffsetTable() {         //调用初始化列表
        offsetTable = new HashMap<>();
        new GenerateTable(this);
    }

    public int getOffsetByName(String s) {  //通过变量名获取地址
//        System.out.println("name:\t" + s + "\toffset:\t" + offsetTable.get(s));
        return offsetTable.get(s);
    }

    public int getFuncSize() {
        if (funcSize % 64 != 0) {
            funcSize = funcSize / 64 * 64 + 64;
        }
        return funcSize;
    }

    /*** add end by sujunzhe ***/


    @Override
    public String toString() {
        return "define dso_local " + funcheader.toString();
    }

    public ArrayList<Block> getBlocklist() {
        return blocklist;
    }

    public FuncHeader getFuncheader() {
        return funcheader;
    }

    public int getBlocknum() {
        return blocknum;
    }

    public void addBlock(Block block) {
        this.blocklist.add(block);
    }


    public void clear() {
        for (Block block : blocklist) {
            block.clear();
        }
    }

    //其他函数

    public int getParaNum() {
//        if (paralist == null) {
//            return 0;
//        }
//        return paralist.size();
        return 0;
    }


    public boolean varnameIsFuncPara(String name) {
//        if (paralist == null) {
//            return false;
//        }
//        for (int i = 0; i < paralist.size(); i++) {
//            if (paralist.get(i).getName().equals(name)) {
//                return true;
//            }
//        }
        return false;
    }

    public int varnameOrderInFuncPara(String name) {
//        for (int i = 0; i < paralist.size(); i++) {
//            if (paralist.get(i).getName().equals(name)) {
//                return i + 1;
//            }
//        }
//        System.err.println("Symbol / varnameOrderInFuncPara() ??? no name = " + name);
        return -10000;
    }
}
