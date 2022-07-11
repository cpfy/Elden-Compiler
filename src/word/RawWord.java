package word;

public class RawWord {
    private String name;
    private int line;
    private WordType type;

    public RawWord(String name, WordType type, int line) {
        this.name = name;
        this.type = type;
        this.line = line;
    }

    public String getName() {
        return name;
    }

    public int getLine() {
        return line;
    }

    public WordType getType() {

        return type;
    }

    public String output1() {
        return type.name() + " " + name + " " + line;
    }

}
