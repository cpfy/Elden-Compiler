package pass.constPass;

import llvm.Block;
import llvm.Ident;
import llvm.Instr.AssignInstr;
import llvm.Instr.BinaryInst;
import llvm.Instr.Instr;
import llvm.Value;
import tool.OutputControl;

import java.util.HashMap;
import java.util.Objects;

public class CSE {
    private static final int MOD = 1000000007;

    private Block block;

    private HashMap<String, Integer> value2Number = new HashMap<>();

    public CSE(Block block) {
        this.block = block;
        execute();
    }

    private void execute() {
        for (Instr instr : block.getInblocklist()) {
            if (instr instanceof AssignInstr && ((AssignInstr) instr).getValueinstr() instanceof BinaryInst) {
                AssignInstr assignInstr = (AssignInstr) instr;
                BinaryInst binaryInst = (BinaryInst) assignInstr.getValueinstr();
                int l, r, sum;
                if (value2Number.containsKey(binaryInst.getV1().toString())) {
                    l = value2Number.get(binaryInst.getV1().toString());
                } else {
                    l = (int) (((long) binaryInst.getV1().toString().hashCode() * binaryInst.getV1().toString().hashCode()) % MOD);
                    value2Number.put(binaryInst.getV1().toString(), l);
                }

                if (value2Number.containsKey(binaryInst.getV2().toString())) {
                    r = value2Number.get(binaryInst.getV2().toString());
                } else {
                    r = (int) (((long) binaryInst.getV2().toString().hashCode() * binaryInst.getV2().toString().hashCode()) % MOD);
                    value2Number.put(binaryInst.getV2().toString(), r);
                }

                sum = computeHash(l, r, binaryInst.getOp());
                OutputControl.printMessage(sum);
                String s = null;
                for (String temp : value2Number.keySet()) {
                    if (value2Number.get(temp) == sum) {
                        s = temp;
                    }
                }
                if (s == null) {
                    value2Number.put(assignInstr.getIdent().toString(), sum);
                } else {
                    for (Instr nt : block.getInblocklist()) {
                        nt.renameUses(new Value(new Ident(s.substring(1))), new Value(new Ident(assignInstr.getIdent().getName())));
                        OutputControl.printMessage(assignInstr.getIdent().toString() + " " + s);
                    }
                }
            }
        }
    }

    private int computeHash(int l, int r, String op) {
        switch (op) {
            case "add":
            case "fadd":
                return (int) ((long) l + r + 233) % MOD;
            case "sub":
            case "fsub":
                return (int) ((long) l * 2 - r - 10086) % MOD;
            case "mul":
            case "fmul":
                return (int) (((long) l * r) % MOD);
            case "sdiv":
            case "fdiv":
                return (int) ((long) 2 * l + r) % MOD;
            case "srem":
                return (int) ((long) l * r + 10086) % MOD;
            default:
                break;
        }
        return 0;
    }

}
