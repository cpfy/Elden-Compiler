package pass.constPass;

import llvm.Instr.Instr;

import java.util.ArrayList;

public class InstrUses {
    private Instr instr;
    private ArrayList<InstrUses> usesArrayList = new ArrayList<>();
    private boolean traversed = false;

    public InstrUses(Instr instr) {
        this.instr = instr;
    }

    public void addUses(ArrayList<InstrUses> add) {
        usesArrayList.addAll(add);
    }

    public void dfs() {
        if (traversed || instr == null) {
            return;
        }
        instr.setCanDelete(false);
        traversed = true;
        for (InstrUses instrUses : usesArrayList) {
            instrUses.dfs();
        }
    }

}
