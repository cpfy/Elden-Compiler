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
}
