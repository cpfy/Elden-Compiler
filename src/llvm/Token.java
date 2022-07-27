package llvm;

public class Token {
    private String tokenCode;
    private String tokenValue;
    private int row;

    private boolean pointerused = false;

    public Token(String tokenCode, String tokenValue, int row) {
        this.tokenCode = tokenCode;
        this.tokenValue = tokenValue;
        this.row = row;
    }

    public String getTokenValue() {
        return tokenValue;
    }

    public String getTokenCode() {
        return tokenCode;
    }

    public int getRow() {
        return row;
    }

    public String tostring() {
        return tokenCode + " " + tokenValue;
    }

    public void setTokenValue(String tokenValue) {
        this.tokenValue = tokenValue;
    }

    public void setTokenCode(String tokenCode) {
        this.tokenCode = tokenCode;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public boolean isPointerused() {
        return pointerused;
    }

    public void setPointerused(boolean pointerused) {
        this.pointerused = pointerused;
    }
}
