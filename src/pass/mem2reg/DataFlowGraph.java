package pass.mem2reg;

import llvm.Block;
import llvm.Function;

import java.util.ArrayList;
import java.util.HashSet;

public class DataFlowGraph {
    private Function function;
    private HashSet<Block> walked = new HashSet<>();

    public DataFlowGraph(Function function) {
        this.function = function;
        execute();
    }

    private void execute() {
        addSucs();
        deleteDeadBlock();
        addPres();
    }

    private void addSucs() {
        ArrayList<Block> blocks = function.getBlocklist();
        for (Block block : blocks) {
            ArrayList<Block> sucs = block.getBrInfo();
            System.out.println();
            System.out.println("Block_" + block.getLabel() + ": sucs size = " + sucs.size());
            for (Block suc : sucs) {
                System.out.println("Block_" + block.getLabel() + " has suc " + suc.getLabel());
                block.addSucBlock(suc);
            }
        }
    }

    private void deleteDeadBlock() {
        for (Block block : function.getBlocklist()) {
            block.setDead(true);
        }
        postOrderWalk(function.getBlocklist().get(0));
        ArrayList<Block> newBlocks = new ArrayList<>();
        for (Block block : function.getBlocklist()) {
            if (!block.isDead()) {
                newBlocks.add(block);
            }
        }
        function.setBlocklist(newBlocks);
    }

    private void addPres() {
        ArrayList<Block> blocks = function.getBlocklist();
        for (Block block : blocks) {
            ArrayList<Block> sucs = block.getBrInfo();
            for (Block suc : sucs) {
                suc.addPreBlock(block);
            }
        }
    }

    private void postOrderWalk(Block block) {
        block.setDead(false);
        walked.add(block);
        for (Block suc : block.getSucBlocks()) {
            if (!walked.contains(suc)) {
                postOrderWalk(suc);
            }
        }

    }
}
