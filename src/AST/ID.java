package AST;

import word.RawWord;

public class ID extends Node {
    private RawWord rawWord;

    public ID(RawWord rawWord) {
        this.rawWord = rawWord;
    }

    @Override
    public void addMidCode() {

    }

    public RawWord getRawWord() {
        return rawWord;
    }
}
