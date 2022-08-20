package backend.backendTable;

import llvm.Block;
import llvm.Function;
import llvm.Instr.Instr;

import java.util.HashMap;

public class DataFlowAnalysis {
    private Function function;

    public DataFlowAnalysis(Function function) {
        this.function = function;
        execute();
    }


    private void execute() {
        computeGenAndKill();
        computeInAndOut();
    }

    private void computeGenAndKill() {
        for (Block block: function.getBlocklist()) {
            for (Instr instr: block.getInblocklist()) {

                HashMap<String, Boolean> usesAndTypes = instr.getUsesAndTypes();
                for (String use: usesAndTypes.keySet()) {
                    if (!block.getLiveKill().containsKey(use)) {
                        block.getLiveGen().put(use, usesAndTypes.get(use));
                    }
                }

                HashMap<String, Boolean> defAndType = instr.getDefAndType();
                for (String use: defAndType.keySet()) {
                        block.getLiveKill().put(use, defAndType.get(use));
                }
            }
        }
    }

    private void computeInAndOut() {
        boolean changed = true;
        while(changed) {
            changed = false;
            for (Block block: function.getBlocklist()) {
                int liveOutSize = block.getLiveOut().size();
                int liveInSize = block.getLiveIn().size();
                block.initLiveOut();
                for (Block suc: block.getSucBlocks()) {
                    block.getLiveOut().putAll(suc.getLiveIn());
                }

                for (String key: block.getLiveKill().keySet()) {
                    block.getLiveIn().remove(key);
                }
                block.getLiveIn().putAll(block.getLiveGen());

                if (block.getLiveOut().size() != liveOutSize || block.getLiveIn().size() != liveInSize) {
                    changed = true;
                }
            }
        }
    }
}
