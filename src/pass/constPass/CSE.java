package pass.constPass;

import llvm.Block;
import llvm.Ident;
import llvm.Instr.AssignInstr;
import llvm.Instr.BinaryInst;
import llvm.Instr.Instr;
import llvm.Value;

import java.util.HashMap;

public class CSE {
    private static final int MOD = 1000000007;
    
    private Block block;

    private HashMap<String, Integer> value2Number = new HashMap<>();

    public CSE(Block block) {
        this.block = block;
        execute();
    }

    private void execute() {
        for (Instr instr: block.getInblocklist()) {
            if (instr instanceof AssignInstr && ((AssignInstr)instr).getValueinstr() instanceof BinaryInst) {
                AssignInstr assignInstr = (AssignInstr) instr;
                BinaryInst binaryInst = (BinaryInst) assignInstr.getValueinstr();
                int l, r, sum;
                if (value2Number.containsKey(binaryInst.getV1().toString())) {
                    l = value2Number.get(binaryInst.getV1().toString());
                }
                else {
                    l = binaryInst.getV1().toString().hashCode();
                    value2Number.put(binaryInst.getV1().toString(), l);
                }

                if (value2Number.containsKey(binaryInst.getV2().toString())) {
                    r = value2Number.get(binaryInst.getV2().toString());
                }
                else {
                    r = binaryInst.getV2().toString().hashCode();
                    value2Number.put(binaryInst.getV2().toString(), r);
                }

                sum = computeHash(l, r, binaryInst.getOp());
                System.out.println(sum);
                String s = null;
                for (String temp: value2Number.keySet()) {
                    if (value2Number.get(temp) == sum) {
                        s = temp;
                    }
                }
                if (s == null) {
                    value2Number.put(assignInstr.getIdent().toString(), sum);
                }
                else {
                    for (Instr nt: block.getInblocklist()) {
                        nt.renameUses(new Value(new Ident(s.substring(1))), new Value(assignInstr.getIdent()));
                        System.out.println(assignInstr.getIdent().toString() + " " + s);
                    }
                }
            }
        }
    }

    private int computeHash(int l, int r , String op) {
        switch (op) {
            case "add":
            case "fadd":
                return (l + r) % MOD;
            case "sub":
            case "fsub":
                return (l - r) % MOD;
            case "mul":
            case "fmul":
                return (int) (((long)l * r) % MOD);
            case "sdiv":
            case "fdiv":
                return (l / r) % MOD;
            case "srem":
                return (l % r) % MOD;
            default:
                break;
        }
        return 0;
    }
}
