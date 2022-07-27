package pass.mem2reg;

import llvm.Block;
import llvm.Function;

import java.util.ArrayList;

public class DataFlowGraph {
    private Function function;

    public DataFlowGraph(Function function) {
        this.function = function;
        execute();
    }

    private void execute() {
        ArrayList<Block> blocks = function.getBlocklist();

        for (Block block: blocks) {
            ArrayList<Block> sucs = block.getBrInfo();
            for (Block suc: sucs) {
                block.addSucBlock(suc);
                suc.addPreBlock(block);
            }
        }
    }
}
