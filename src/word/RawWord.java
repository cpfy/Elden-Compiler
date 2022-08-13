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
        if (name.charAt(0) >= '0' && name.charAt(0) <= '9') {
            return name;
        }
        return name;
    }

    public int getLine() {
        return line;
    }

    public WordType getType() {

        return type;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String output1() {
        return type.name() + " " + name + " " + line;
    }

}
