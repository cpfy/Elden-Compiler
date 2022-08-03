package backend;

import llvm.Instr.Instr;

import java.util.ArrayList;

public class Instrs {
    private ArrayList<NewInstr> instrList;

    public Instrs() {
        instrList = new ArrayList<>();
    }

    public ArrayList<NewInstr> getInstrList() {
        return instrList;
    }

    public void addInstr(NewInstr instr) {
        instrList.add(instr);
    }
}
