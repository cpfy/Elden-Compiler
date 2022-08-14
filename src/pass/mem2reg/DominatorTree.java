package pass.mem2reg;

import llvm.Block;
import llvm.Function;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class DominatorTree {
    private Function function;
    private HashMap<Block, Block> doms = new HashMap<>();
    private HashSet<Block> walked = new HashSet<>();

    private HashMap<Block, Integer> order = new HashMap<>();
    private ArrayList<Block> postorder = new ArrayList<>();

    public DominatorTree(Function function) {
        this.function = function;
        execute();
    }

    private void execute() {
        Block root = function.getBlocklist().get(0);
        postOrderWalk(root);



        doms.put(root, root);

        boolean changed = true;
        int pp = 0;
        while (changed) {
            changed = false;
            for (int i = postorder.size() - 2; i >= 0; i--) {
                Block b = postorder.get(i);
                Block new_idom = b.getPreBlocks().get(0);
                for (int j = 1; j < b.getPreBlocks().size(); j++) {
                    Block p = b.getPreBlocks().get(j);
                    if (doms.containsKey(p)) {
                        new_idom = intersect(p, new_idom);
                    }
                }
                if (doms.get(b) != new_idom) {
                    doms.put(b, new_idom);
                    changed = true;
                }
            }
            for (int i = postorder.size() - 1; i >= 0; i--) {
                Block b = postorder.get(i);
                System.out.println(function.getBlocklist().indexOf(b));
            }
            System.out.println();
            pp++;
        }
        System.out.println("pp = " + pp);

        for (Block block: doms.keySet()) {
            block.addIDom(doms.get(block));
        }
    }

    private Block intersect(Block b1, Block b2) {
        Block finger1 = b1;
        Block finger2 = b2;
        while (finger1 != finger2) {
            while (order.get(finger1) < order.get(finger2)) {
                finger1 = doms.get(finger1);
            }
            while (order.get(finger2) < order.get(finger1)) {
                finger2 = doms.get(finger2);
            }
        }
        return finger1;
    }

    private void postOrderWalk(Block block) {
        walked.add(block);
        for (Block suc: block.getSucBlocks()) {
            if (!walked.contains(suc)) {
                postOrderWalk(suc);
            }
        }
        postorder.add(block);
        order.put(block, postorder.size() - 1);
    }
}
