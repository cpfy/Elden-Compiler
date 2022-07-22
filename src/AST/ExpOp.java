package AST;

import word.WordType;

import java.util.ArrayList;
import java.util.HashMap;

public class ExpOp extends Exp {
    private WordType op;
    private Exp left;
    private Exp right;
    private String temp;
//    private static HashMap<WordType, MidCodeType> opMap = new HashMap<>();

    public ExpOp(WordType op, Exp left, Exp right) {
        this.op = op;
        this.left = left;
        this.right = right;
    }

    @Override
    public ArrayList<String> addCodePre() {
        addCode(left.addCodePre());
        addCode(right.addCodePre());
        setExpType();
        String l = left.getTemp();
        String r = right.getTemp();
        if (left.getType().equals("i32") && getType().equals("float")) {
            String newTmp = newTemp();
            addCode(newTmp + " = sitofp i32 " + l + " to float\n");
            l = newTmp;
        }
        if (right.getType().equals("i32") && getType().equals("float")) {
            String newTmp = newTemp();
            addCode(newTmp + " = sitofp i32 " + r + " to float\n");
            r = newTmp;
        }
        this.temp = newTemp();
        String opString = null;
        if (op == WordType.PLUS) {
            if (getType().equals("i32")) {
                opString = "add";
            }
            else if (getType().equals("float")) {
                opString = "fadd";
            }
            else {
                System.out.println("\nError in ExpOP!!!\n");
            }
            addCode(this.temp + " = " + opString + " " + getType() + " " + l + ", " + r + "\n");
        }
        else if (op == WordType.MINU) {
            if (getType().equals("i32")) {
                opString = "sub";
            }
            else if (getType().equals("float")) {
                opString = "fsub";
            }
            else {
                System.out.println("\nError in ExpOP!!!\n");
            }
            addCode(this.temp + " = " + opString + " " + getType() + " " + l + ", " + r + "\n");
        }
        else if (op == WordType.MULT) {
            if (getType().equals("i32")) {
                opString = "mul";
            }
            else if (getType().equals("float")) {
                opString = "fmul";
            }
            else {
                System.out.println("\nError in ExpOP!!!\n");
            }
            addCode(this.temp + " = " + opString + " " + getType() + " " + l + ", " + r + "\n");
        }
        else if (op == WordType.DIV) {
            if (getType().equals("i32")) {
                opString = "sdiv";
            }
            else if (getType().equals("float")) {
                opString = "fdiv";
            }
            else {
                System.out.println("\nError in ExpOP!!!\n");
            }
            addCode(this.temp + " = " + opString + " " + getType() + " " + l + ", " + r + "\n");
        }
        else if (op == WordType.MOD) {
            if (getType().equals("i32")) {
                opString = "srem";
            }
            else if (getType().equals("float")) {
                System.out.println("\nError in ExpOP!!! MODING FLOAT AND FLOAT\n");
            }
            else {
                System.out.println("\nError in ExpOP!!!\n");
            }
            addCode(this.temp + " = " + opString + " " + getType() + " " + l + ", " + r + "\n");
        }

        else if (op == WordType.NEQ) {                    // !=
            if (getType().equals("i32")) {
                opString = "icmp ne";
            }
            else if (getType().equals("float")) {
                opString = "fcmp une";
            }
            else {
                System.out.println("\nError in ExpOP!!!\n");
            }
            addCode(this.temp + " = " + opString + " " + getType() + " " + l + ", " + r + "\n");
            String t = this.temp;
            this.temp = newTemp();
            addCode(this.temp + " = zext i1 " + t + " to i32\n");
        }
        else if (op == WordType.EQL || op == WordType.NOT) {                    // ==
            if (getType().equals("i32")) {
                opString = "icmp eq";
            }
            else if (getType().equals("float")) {
                opString = "fcmp oeq";
            }
            else {
                System.out.println("\nError in ExpOP!!!\n");
            }
            addCode(this.temp + " = " + opString + " " + getType() + " " + l + ", " + r + "\n");
            String t = this.temp;
            this.temp = newTemp();
            addCode(this.temp + " = zext i1 " + t + " to i32\n");
        }
        else if (op == WordType.LEQ) {                    // <=
            if (getType().equals("i32")) {
                opString = "icmp sle";
            }
            else if (getType().equals("float")) {
                opString = "fcmp ole";
            }
            else {
                System.out.println("\nError in ExpOP!!!\n");
            }
            addCode(this.temp + " = " + opString + " " + getType() + " " + l + ", " + r + "\n");
            String t = this.temp;
            this.temp = newTemp();
            addCode(this.temp + " = zext i1 " + t + " to i32\n");
        }
        else if (op == WordType.LSS) {                    // <
            if (getType().equals("i32")) {
                opString = "icmp slt";
            }
            else if (getType().equals("float")) {
                opString = "fcmp olt";
            }
            else {
                System.out.println("\nError in ExpOP!!!\n");
            }
            addCode(this.temp + " = " + opString + " " + getType() + " " + l + ", " + r + "\n");
            String t = this.temp;
            this.temp = newTemp();
            addCode(this.temp + " = zext i1 " + t + " to i32\n");
        }
        else if (op == WordType.GEQ) {                    // >=
            if (getType().equals("i32")) {
                opString = "icmp sge";
            }
            else if (getType().equals("float")) {
                opString = "fcmp oge";
            }
            else {
                System.out.println("\nError in ExpOP!!!\n");
            }
            addCode(this.temp + " = " + opString + " " + getType() + " " + l + ", " + r + "\n");
            String t = this.temp;
            this.temp = newTemp();
            addCode(this.temp + " = zext i1 " + t + " to i32\n");
        }
        else if (op == WordType.GRE) {                    // >
            if (getType().equals("i32")) {
                opString = "icmp sgt";
            }
            else if (getType().equals("float")) {
                opString = "fcmp ogt";
            }
            else {
                System.out.println("\nError in ExpOP!!!\n");
            }
            addCode(this.temp + " = " + opString + " " + getType() + " " + l + ", " + r + "\n");
            String t = this.temp;
            this.temp = newTemp();
            addCode(this.temp + " = zext i1 " + t + " to i32\n");
        }
        else {
            System.out.println("Error at EXPOP");
        }
        return getCodes();
    }

    @Override
    public String getTemp() {
        return temp;
    }

    @Override
    public void calculate() {
        left.getValue();
        right.getValue();
        setExpType();
        if (type.equals("i32")) {
            if (op == WordType.PLUS) {                          // +
                value = left.getValue() + right.getValue();
            } else if (op == WordType.MINU) {                   // -
                value = left.getValue() - right.getValue();
            } else if (op == WordType.MULT) {                   // *
                value = left.getValue() * right.getValue();
            } else if (op == WordType.DIV) {                    // /
                value = left.getValue() / right.getValue();
            } else if (op == WordType.MOD) {                    // %
                value = left.getValue() % right.getValue();
            } else if (op == WordType.NEQ) {                    // !=  !
                if (left.getValue() != right.getValue()) {
                    value = 1;
                } else {
                    value = 0;
                }
            } else if (op == WordType.EQL || op == WordType.NOT) {                    // ==
                if (left.getValue() == right.getValue()) {
                    value = 1;
                } else {
                    value = 0;
                }
            } else if (op == WordType.LEQ) {                    // <=
                if (left.getValue() <= right.getValue()) {
                    value = 1;
                } else {
                    value = 0;
                }
            } else if (op == WordType.LSS) {                    // <
                if (left.getValue() < right.getValue()) {
                    value = 1;
                } else {
                    value = 0;
                }
            } else if (op == WordType.GEQ) {                    // >=
                if (left.getValue() >= right.getValue()) {
                    value = 1;
                } else {
                    value = 0;
                }
            } else if (op == WordType.GRE) {                    // >
                if (left.getValue() > right.getValue()) {
                    value = 1;
                } else {
                    value = 0;
                }
            } else {
                System.out.println("Error at EXPOP");
            }
            valueF = value;
        }
        else {
            if (op == WordType.PLUS) {                          // +
                valueF = left.getValueF() + right.getValueF();
            } else if (op == WordType.MINU) {                   // -
                valueF = left.getValueF() - right.getValueF();
            } else if (op == WordType.MULT) {                   // *
                valueF = left.getValueF() * right.getValueF();
            } else if (op == WordType.DIV) {                    // /
                valueF = left.getValueF() / right.getValueF();
            } else if (op == WordType.MOD) {                    // %
                valueF = left.getValueF() % right.getValueF();
            } else if (op == WordType.NEQ) {                    // !=  !
                if (left.getValueF() != right.getValueF()) {
                    value = 1;
                } else {
                    value = 0;
                }
                valueF = value;
            } else if (op == WordType.EQL || op == WordType.NOT) {                    // ==
                if (left.getValueF() == right.getValueF()) {
                    value = 1;
                } else {
                    value = 0;
                }
                valueF = value;
            } else if (op == WordType.LEQ) {                    // <=
                if (left.getValueF() <= right.getValueF()) {
                    value = 1;
                } else {
                    value = 0;
                }
                valueF = value;
            } else if (op == WordType.LSS) {                    // <
                if (left.getValueF() < right.getValueF()) {
                    value = 1;
                } else {
                    value = 0;
                }
                valueF = value;
            } else if (op == WordType.GEQ) {                    // >=
                if (left.getValueF() >= right.getValueF()) {
                    value = 1;
                } else {
                    value = 0;
                }
                valueF = value;
            } else if (op == WordType.GRE) {                    // >
                if (left.getValueF() > right.getValueF()) {
                    value = 1;
                } else {
                    value = 0;
                }
                valueF = value;
            } else {
                System.out.println("Error at EXPOP");
            }
        }
    }

    private void setExpType() {
        if ((left.getType().equals("float") || right.getType().equals("float"))
                && (op == WordType.PLUS || op == WordType.MINU || op == WordType.MULT || op == WordType.DIV)) {
            setType("float");
        }
        else {
            setType("i32");
        }
    }

    @Override
    public void addMidCode() {
        addCodePre();
        generate();
    }
}
