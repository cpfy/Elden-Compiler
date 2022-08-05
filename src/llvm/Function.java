package llvm;

import java.util.ArrayList;

public class Function {

    private FuncHeader funcheader;  // 函数具体属性
    private ArrayList<Block> blocklist;  // 函数内所有基本块
    private int blocknum;    // 函数内基本块个数
    public boolean voidreturn;  //返回值是否为空

    public Function(FuncHeader funcheader) {
        this.funcheader = funcheader;
        this.blocklist = new ArrayList<>();

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
        for (Block block: blocklist) {
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
