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
            System.out.println();
            System.out.println("suncs size = " + sucs.size());
            for (Block suc: sucs) {
                System.out.println("Block_" + block.getLabel() + " has suc " + suc.getLabel());
                block.addSucBlock(suc);
                suc.addPreBlock(block);
            }
        }
    }
}
