package AST;

import midCode.MidCodeList;
import midCode.MidCodeType;
import word.WordType;

import java.util.HashMap;

public class ExpOp extends Exp {
    private WordType op;
    private Exp left;
    private Exp right;
    private String temp;
    private static HashMap<WordType, MidCodeType> opMap = new HashMap<>();

    public ExpOp(WordType op, Exp left, Exp right) {
        this.op = op;
        this.left = left;
        this.right = right;
    }

    @Override
    public String getTemp() {
        return temp;
    }

    @Override
    public void calculate() {
        if (op == WordType.PLUS) {                          // +
            value = left.getValue() + right.getValue();
        }
        else if (op == WordType.MINU) {                   // -
            value = left.getValue() - right.getValue();
        }
        else if (op == WordType.MULT) {                   // *
            value = left.getValue() * right.getValue();
        }
        else if (op == WordType.DIV) {                    // /
            value = left.getValue() / right.getValue();
        }
        else if (op == WordType.MOD) {                    // %
            value = left.getValue() % right.getValue();
        }
        else if (op == WordType.NEQ) {                    // !=  !
            if (left.getValue() != right.getValue()) {
                value = 1;
            }
            else {
                value = 0;
            }
        }
        else if (op == WordType.EQL || op == WordType.NOT) {                    // ==
            if (left.getValue() == right.getValue()) {
                value = 1;
            }
            else {
                value = 0;
            }
        }
        else if (op == WordType.LEQ) {                    // <=
            if (left.getValue() <= right.getValue()) {
                value = 1;
            }
            else {
                value = 0;
            }
        }
        else if (op == WordType.LSS) {                    // <
            if (left.getValue() < right.getValue()) {
                value = 1;
            }
            else {
                value = 0;
            }
        }
        else if (op == WordType.GEQ) {                    // >=
            if (left.getValue() >= right.getValue()) {
                value = 1;
            }
            else {
                value = 0;
            }
        }
        else if (op == WordType.GRE) {                    // >
            if (left.getValue() > right.getValue()) {
                value = 1;
            }
            else {
                value = 0;
            }
        }
        else {
            System.out.println("Error at EXPOP");
        }
    }

    @Override
    public void addMidCode() {
        left.addMidCode();
        right.addMidCode();
        this.temp = newTemp();
        if (op == WordType.PLUS) {
            midCodeList.addMidCodeItem(MidCodeType.PLUSOP, left.getTemp(), right.getTemp(), temp);
        }
        else if (op == WordType.MINU) {
            midCodeList.addMidCodeItem(MidCodeType.MINUOP, left.getTemp(), right.getTemp(), temp);
        }
        else if (op == WordType.MULT) {
            midCodeList.addMidCodeItem(MidCodeType.MULTOP, left.getTemp(), right.getTemp(), temp);
        }
        else if (op == WordType.DIV) {
            midCodeList.addMidCodeItem(MidCodeType.DIVOP, left.getTemp(), right.getTemp(), temp);
        }
        else if (op == WordType.MOD) {
            midCodeList.addMidCodeItem(MidCodeType.MODOP, left.getTemp(), right.getTemp(), temp);
        }
        else if (op == WordType.NEQ) {                    // !=
            midCodeList.addMidCodeItem(MidCodeType.NEQOP, left.getTemp(), right.getTemp(), temp);
        }
        else if (op == WordType.EQL || op == WordType.NOT) {                    // ==
            midCodeList.addMidCodeItem(MidCodeType.EQLOP, left.getTemp(), right.getTemp(), temp);
        }
        else if (op == WordType.LEQ) {                    // <=
            midCodeList.addMidCodeItem(MidCodeType.LEQOP, left.getTemp(), right.getTemp(), temp);
        }
        else if (op == WordType.LSS) {                    // <
            midCodeList.addMidCodeItem(MidCodeType.LSSOP, left.getTemp(), right.getTemp(), temp);
        }
        else if (op == WordType.GEQ) {                    // >=
            midCodeList.addMidCodeItem(MidCodeType.GEQOP, left.getTemp(), right.getTemp(), temp);
        }
        else if (op == WordType.GRE) {                    // >
            midCodeList.addMidCodeItem(MidCodeType.GREOP, left.getTemp(), right.getTemp(), temp);
        }
        else {
            System.out.println("Error at EXPOP");
        }
    }
}
