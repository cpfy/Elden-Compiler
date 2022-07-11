package symbolTable.items;

public class ErrorItem {
    private int line;
    private String type;

    public ErrorItem(int line, String type) {
        this.line = line;
        this.type = type;
    }

    public int getLine() {
        return line;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return line + " " + type;
    }
}
