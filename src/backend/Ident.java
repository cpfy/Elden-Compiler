package backend;

public class Ident {

    private boolean isIdent;
    private boolean global;
    private String name;
    private int id;

    public Ident(int id) {
        this.isIdent = false;
        this.id = id;
    }

    public Ident(String name) {
        this.isIdent = true;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public boolean isIdent() {
        return isIdent;
    }

    public boolean isGlobal() {
        return global;
    }
}
