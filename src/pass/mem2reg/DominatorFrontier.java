package pass.mem2reg;

import llvm.Block;
import llvm.Function;

public class DominatorFrontier {
    private Function function;

    public DominatorFrontier(Function function) {
        this.function = function;
        execute();
        for (Block block: function.getBlocklist()) {
            if (block.getDominatorFrontiers().size() == 1)
            System.err.println(block.getLabel() + " " + block.getDominatorFrontiers().get(0).getLabel());
        }
    }

    private void execute() {
        for (Block n: function.getBlocklist()) {
            if (n.getPreBlocks().size() > 1) {
                for (Block p: n.getPreBlocks()) {
                    Block runner = p;
                    while (runner != n.getIDom()) {
                        runner.addDominatorFrontier(n);
                        runner = runner.getIDom();
                    }
                }
            }
        }
    }
}
